package com.awsBuilder.builder.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeLoadBalancersResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeListenersResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeRulesResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeTargetGroupsResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeTargetHealthResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Listener;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroup;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Rule;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.DBInstance;
import software.amazon.awssdk.services.rds.model.DescribeDbInstancesResponse;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.FunctionConfiguration;
import software.amazon.awssdk.services.lambda.model.ListFunctionsResponse;
import software.amazon.awssdk.services.lambda.model.GetFunctionResponse;
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.model.Cluster;
import software.amazon.awssdk.services.eks.EksClient;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.Topic;
import software.amazon.awssdk.services.sns.model.ListTopicsResponse;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.ListQueuesRequest;
import software.amazon.awssdk.services.sqs.model.ListQueuesResponse;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.User;
import software.amazon.awssdk.services.iam.model.ListUsersResponse;
import software.amazon.awssdk.services.iam.model.Role;
import software.amazon.awssdk.services.iam.model.ListRolesResponse;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;

/**
 * AWS Inventory Collector
 *
 * Single table: aws_inventory (resource_id, resource_type, region, raw_config JSONB, collected_at)
 * Stores the complete raw AWS SDK object as JSON — every field, no truncation.
 *
 * Resource types collected:
 *   VPC, SUBNET, IGW, ROUTE_TABLE, SECURITY_GROUP,
 *   EC2_INSTANCE (+ userData, termination protection, shutdown behaviour),
 *   EBS_VOLUME, S3_BUCKET,
 *   ALB, ALB_ATTRIBUTES, ALB_LISTENER, ALB_LISTENER_RULE,
 *   ALB_TARGET_GROUP, ALB_TARGET_HEALTH,
 *   RDS_INSTANCE, LAMBDA_FUNCTION,
 *   ECS_CLUSTER, EKS_CLUSTER,
 *   SNS_TOPIC, SQS_QUEUE,
 *   IAM_USER, IAM_ROLE
 */
public class AwsInventoryCollector {

    private static final Logger logger = LoggerFactory.getLogger(AwsInventoryCollector.class);
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // ─────────────────────────────────────────────────────────────────────────
    // MAIN
    // ─────────────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        logger.info("══════════════════════════════════════════");
        logger.info("  AWS Inventory Collection Starting");
        logger.info("══════════════════════════════════════════");

        String awsRegion  = getEnv("AWS_REGION",    null);
        String pgHost     = getEnv("PG_HOST",       null);
        String pgPort     = getEnv("PG_PORT",       "5432");
        String pgDatabase = getEnv("PG_DATABASE",   null);
        String pgUser     = getEnv("PG_USER",       null);
        String pgPassword = getEnv("PG_PASSWORD",   null);

        logger.info("Region: {}  |  DB: {}@{}:{}/{}", awsRegion, pgUser, pgHost, pgPort, pgDatabase);

        Region region = Region.of(awsRegion);

        Ec2Client    ec2    = Ec2Client.builder()   .region(region).credentialsProvider(DefaultCredentialsProvider.create()).build();
        S3Client     s3     = S3Client.builder()    .region(region).credentialsProvider(DefaultCredentialsProvider.create()).build();
        ElasticLoadBalancingV2Client elb = ElasticLoadBalancingV2Client.builder().region(region).credentialsProvider(DefaultCredentialsProvider.create()).build();
        RdsClient    rds    = RdsClient.builder()   .region(region).credentialsProvider(DefaultCredentialsProvider.create()).build();
        LambdaClient lambda = LambdaClient.builder().region(region).credentialsProvider(DefaultCredentialsProvider.create()).build();
        EcsClient    ecs    = EcsClient.builder()   .region(region).credentialsProvider(DefaultCredentialsProvider.create()).build();
        EksClient    eks    = EksClient.builder()   .region(region).credentialsProvider(DefaultCredentialsProvider.create()).build();
        SnsClient    sns    = SnsClient.builder()   .region(region).credentialsProvider(DefaultCredentialsProvider.create()).build();
        SqsClient    sqs    = SqsClient.builder()   .region(region).credentialsProvider(DefaultCredentialsProvider.create()).build();
        IamClient    iam    = IamClient.builder()   .region(Region.AWS_GLOBAL).credentialsProvider(DefaultCredentialsProvider.create()).build();

        String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s", pgHost, pgPort, pgDatabase);

        try (Connection conn = DriverManager.getConnection(jdbcUrl, pgUser, pgPassword)) {
            logger.info("✔ Database connected");
            createTable(conn);

            // Networking
            collectVpcs(ec2, conn, awsRegion);
            collectSubnets(ec2, conn, awsRegion);
            collectIgws(ec2, conn, awsRegion);
            collectRouteTables(ec2, conn, awsRegion);
            collectSecurityGroups(ec2, conn, awsRegion);

            // Compute
            collectEc2Instances(ec2, conn, awsRegion);
            collectEbsVolumes(ec2, conn, awsRegion);

            // Storage
            collectS3Buckets(s3, conn, awsRegion);

            // Load Balancing
            collectAlbs(elb, conn, awsRegion);
            collectAlbAttributes(elb, conn, awsRegion);
            collectListeners(elb, conn, awsRegion);
            collectListenerRules(elb, conn, awsRegion);
            collectTargetGroups(elb, conn, awsRegion);
            collectTargetHealth(elb, conn, awsRegion);

            // Database
            collectRdsInstances(rds, conn, awsRegion);

            // Serverless / Containers
            collectLambdaFunctions(lambda, conn, awsRegion);
            collectEcsClusters(ecs, conn, awsRegion);
            collectEksClusters(eks, conn, awsRegion);

            // Messaging
            collectSnsTopics(sns, conn, awsRegion);
            collectSqsQueues(sqs, conn, awsRegion);

            // IAM
            collectIamUsers(iam, conn, awsRegion);
            collectIamRoles(iam, conn, awsRegion);

            logger.info("══════════════════════════════════════════");
            logger.info("  Done in {}ms", System.currentTimeMillis() - start);
            logger.info("══════════════════════════════════════════");

        } catch (Exception e) {
            logger.error("Fatal error", e);
            System.exit(1);
        } finally {
            ec2.close(); s3.close(); elb.close(); rds.close();
            lambda.close(); ecs.close(); eks.close();
            sns.close(); sqs.close(); iam.close();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SINGLE TABLE
    // ─────────────────────────────────────────────────────────────────────────

    private static void createTable(Connection conn) throws SQLException {
        try (Statement s = conn.createStatement()) {
            s.execute("""
                CREATE TABLE IF NOT EXISTS aws_inventory (
                    id            BIGSERIAL PRIMARY KEY,
                    resource_id   TEXT NOT NULL,
                    resource_type TEXT NOT NULL,
                    region        TEXT,
                    raw_config    JSONB NOT NULL,
                    collected_at  TIMESTAMP NOT NULL,
                    UNIQUE (resource_id, resource_type)
                )
                """);
            s.execute("CREATE INDEX IF NOT EXISTS idx_inv_type   ON aws_inventory (resource_type)");
            s.execute("CREATE INDEX IF NOT EXISTS idx_inv_id     ON aws_inventory (resource_id)");
            s.execute("CREATE INDEX IF NOT EXISTS idx_inv_config ON aws_inventory USING gin (raw_config)");
        }
        logger.info("✔ Table aws_inventory ready");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UPSERT HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private static final String UPSERT_SQL = """
        INSERT INTO aws_inventory (resource_id, resource_type, region, raw_config, collected_at)
        VALUES (?, ?, ?, ?::jsonb, ?)
        ON CONFLICT (resource_id, resource_type) DO UPDATE SET
            region       = EXCLUDED.region,
            raw_config   = EXCLUDED.raw_config,
            collected_at = EXCLUDED.collected_at
        """;

    /** Upsert a list of (resourceId → object) pairs in a single batch */
    private static void upsertBatch(Connection conn, List<Map.Entry<String, Object>> items,
                                    String type, String region) throws Exception {
        if (items.isEmpty()) return;
        try (PreparedStatement ps = conn.prepareStatement(UPSERT_SQL)) {
            for (Map.Entry<String, Object> e : items) {
                ps.setString(1, e.getKey());
                ps.setString(2, type);
                ps.setString(3, region);
                ps.setString(4, MAPPER.writeValueAsString(e.getValue()));
                ps.setTimestamp(5, Timestamp.from(Instant.now()));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /** Upsert a single resource */
    private static void upsert(Connection conn, String id, String type,
                               String region, Object obj) throws Exception {
        upsertBatch(conn, List.of(Map.entry(id, obj)), type, region);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NETWORKING
    // ─────────────────────────────────────────────────────────────────────────

    private static void collectVpcs(Ec2Client ec2, Connection conn, String region) throws Exception {
        logger.info("Collecting VPCs...");
        List<Map.Entry<String, Object>> items = new ArrayList<>();
        String token = null;
        do {
            DescribeVpcsRequest.Builder req = DescribeVpcsRequest.builder();
            if (token != null) req.nextToken(token);
            DescribeVpcsResponse resp = ec2.describeVpcs(req.build());
            for (Vpc v : resp.vpcs()) items.add(Map.entry(v.vpcId(), v));
            token = resp.nextToken();
        } while (token != null);
        upsertBatch(conn, items, "VPC", region);
        logger.info("  ✔ {} VPCs", items.size());
    }

    private static void collectSubnets(Ec2Client ec2, Connection conn, String region) throws Exception {
        logger.info("Collecting Subnets...");
        List<Map.Entry<String, Object>> items = new ArrayList<>();
        String token = null;
        do {
            DescribeSubnetsRequest.Builder req = DescribeSubnetsRequest.builder();
            if (token != null) req.nextToken(token);
            DescribeSubnetsResponse resp = ec2.describeSubnets(req.build());
            for (Subnet s : resp.subnets()) items.add(Map.entry(s.subnetId(), s));
            token = resp.nextToken();
        } while (token != null);
        upsertBatch(conn, items, "SUBNET", region);
        logger.info("  ✔ {} Subnets", items.size());
    }

    private static void collectIgws(Ec2Client ec2, Connection conn, String region) throws Exception {
        logger.info("Collecting Internet Gateways...");
        List<Map.Entry<String, Object>> items = new ArrayList<>();
        String token = null;
        do {
            DescribeInternetGatewaysRequest.Builder req = DescribeInternetGatewaysRequest.builder();
            if (token != null) req.nextToken(token);
            DescribeInternetGatewaysResponse resp = ec2.describeInternetGateways(req.build());
            for (InternetGateway igw : resp.internetGateways()) items.add(Map.entry(igw.internetGatewayId(), igw));
            token = resp.nextToken();
        } while (token != null);
        upsertBatch(conn, items, "IGW", region);
        logger.info("  ✔ {} IGWs", items.size());
    }

    private static void collectRouteTables(Ec2Client ec2, Connection conn, String region) throws Exception {
        logger.info("Collecting Route Tables...");
        List<Map.Entry<String, Object>> items = new ArrayList<>();
        String token = null;
        do {
            DescribeRouteTablesRequest.Builder req = DescribeRouteTablesRequest.builder();
            if (token != null) req.nextToken(token);
            DescribeRouteTablesResponse resp = ec2.describeRouteTables(req.build());
            for (RouteTable rt : resp.routeTables()) items.add(Map.entry(rt.routeTableId(), rt));
            token = resp.nextToken();
        } while (token != null);
        upsertBatch(conn, items, "ROUTE_TABLE", region);
        logger.info("  ✔ {} Route Tables", items.size());
    }

    private static void collectSecurityGroups(Ec2Client ec2, Connection conn, String region) throws Exception {
        logger.info("Collecting Security Groups...");
        List<Map.Entry<String, Object>> items = new ArrayList<>();
        String token = null;
        do {
            DescribeSecurityGroupsRequest.Builder req = DescribeSecurityGroupsRequest.builder();
            if (token != null) req.nextToken(token);
            DescribeSecurityGroupsResponse resp = ec2.describeSecurityGroups(req.build());
            for (SecurityGroup sg : resp.securityGroups()) items.add(Map.entry(sg.groupId(), sg));
            token = resp.nextToken();
        } while (token != null);
        upsertBatch(conn, items, "SECURITY_GROUP", region);
        logger.info("  ✔ {} Security Groups", items.size());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // COMPUTE
    // ─────────────────────────────────────────────────────────────────────────

    private static void collectEc2Instances(Ec2Client ec2, Connection conn, String region) throws Exception {
        logger.info("Collecting EC2 Instances (full config)...");
        List<Instance> instances = new ArrayList<>();
        for (DescribeInstancesResponse page : ec2.describeInstancesPaginator()) {
            for (Reservation r : page.reservations()) instances.addAll(r.instances());
        }

        List<Map.Entry<String, Object>> items = new ArrayList<>();
        for (Instance i : instances) {
            // Base object has: instanceType, cpuOptions, placement, networkInterfaces,
            // blockDeviceMappings, metadataOptions, hibernationOptions, enclaveOptions,
            // monitoring, iamInstanceProfile, securityGroups, tags, etc.
            Map<String, Object> config = new LinkedHashMap<>();
            config.put("instance", i);  // full raw object — every field

            // Extra attributes not in the base DescribeInstances response
            config.put("userData", safeGet(() ->
                    ec2.describeInstanceAttribute(r -> r
                               .instanceId(i.instanceId())
                               .attribute(InstanceAttributeName.USER_DATA))
                       .userData().value(), null));  // base64 encoded user-data

            config.put("disableApiTermination", safeGet(() ->
                    ec2.describeInstanceAttribute(r -> r
                               .instanceId(i.instanceId())
                               .attribute(InstanceAttributeName.DISABLE_API_TERMINATION))
                       .disableApiTermination().value(), null));

            config.put("instanceInitiatedShutdownBehavior", safeGet(() ->
                    ec2.describeInstanceAttribute(r -> r
                               .instanceId(i.instanceId())
                               .attribute(InstanceAttributeName.INSTANCE_INITIATED_SHUTDOWN_BEHAVIOR))
                       .instanceInitiatedShutdownBehavior().value(), null));

            config.put("sriovNetSupport", safeGet(() ->
                    ec2.describeInstanceAttribute(r -> r
                               .instanceId(i.instanceId())
                               .attribute(InstanceAttributeName.SRIOV_NET_SUPPORT))
                       .sriovNetSupport().value(), null));

            items.add(Map.entry(i.instanceId(), config));
        }

        upsertBatch(conn, items, "EC2_INSTANCE", region);
        logger.info("  ✔ {} EC2 Instances", items.size());
    }

    private static void collectEbsVolumes(Ec2Client ec2, Connection conn, String region) throws Exception {
        logger.info("Collecting EBS Volumes...");
        List<Map.Entry<String, Object>> items = new ArrayList<>();
        for (DescribeVolumesResponse page : ec2.describeVolumesPaginator()) {
            for (Volume v : page.volumes()) items.add(Map.entry(v.volumeId(), v));
        }
        upsertBatch(conn, items, "EBS_VOLUME", region);
        logger.info("  ✔ {} EBS Volumes", items.size());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STORAGE
    // ─────────────────────────────────────────────────────────────────────────

    private static void collectS3Buckets(S3Client s3, Connection conn, String region) throws Exception {
        logger.info("Collecting S3 Buckets...");
        List<Bucket> buckets = s3.listBuckets().buckets();
        List<Map.Entry<String, Object>> items = new ArrayList<>();

        for (Bucket b : buckets) {
            String name = b.name();
            Map<String, Object> config = new LinkedHashMap<>();
            config.put("bucketName",        name);
            config.put("creationDate",      b.creationDate());
            config.put("region",            safeGet(() -> s3.getBucketLocation(r -> r.bucket(name)).locationConstraintAsString(), null));
            config.put("versioning",        safeGet(() -> s3.getBucketVersioning(r -> r.bucket(name)).statusAsString(), null));
            config.put("encryption",        safeGet(() -> s3.getBucketEncryption(r -> r.bucket(name)).serverSideEncryptionConfiguration(), null));
            config.put("publicAccessBlock", safeGet(() -> s3.getPublicAccessBlock(r -> r.bucket(name)).publicAccessBlockConfiguration(), null));
            config.put("acl",               safeGet(() -> s3.getBucketAcl(r -> r.bucket(name)).grants(), null));
            config.put("policy",            safeGet(() -> s3.getBucketPolicy(r -> r.bucket(name)).policy(), null));
            config.put("logging",           safeGet(() -> s3.getBucketLogging(r -> r.bucket(name)).loggingEnabled(), null));
            config.put("lifecycle",         safeGet(() -> s3.getBucketLifecycleConfiguration(r -> r.bucket(name)).rules(), null));
            config.put("replication",       safeGet(() -> s3.getBucketReplication(r -> r.bucket(name)).replicationConfiguration(), null));
            config.put("cors",              safeGet(() -> s3.getBucketCors(r -> r.bucket(name)).corsRules(), null));
            config.put("website",           safeGet(() -> s3.getBucketWebsite(r -> r.bucket(name)), null));
            config.put("tags",              safeGet(() -> s3.getBucketTagging(r -> r.bucket(name)).tagSet(), null));
            items.add(Map.entry(name, config));
        }

        upsertBatch(conn, items, "S3_BUCKET", region);
        logger.info("  ✔ {} S3 Buckets", items.size());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LOAD BALANCING
    // ─────────────────────────────────────────────────────────────────────────

    private static void collectAlbs(ElasticLoadBalancingV2Client elb, Connection conn, String region) throws Exception {
        logger.info("Collecting ALBs...");
        List<Map.Entry<String, Object>> items = new ArrayList<>();
        for (DescribeLoadBalancersResponse page : elb.describeLoadBalancersPaginator()) {
            for (LoadBalancer lb : page.loadBalancers()) {
                // Full raw object: name, dnsName, canonicalHostedZoneId, createdTime,
                // scheme, type, vpcId, state, availabilityZones, securityGroups, ipAddressType
                Map<String, Object> config = new LinkedHashMap<>();
                config.put("loadBalancer", lb);
                config.put("tags", safeGet(() -> {
                    software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeTagsResponse t =
                            elb.describeTags(r -> r.resourceArns(lb.loadBalancerArn()));
                    return t.tagDescriptions().isEmpty() ? null : t.tagDescriptions().get(0).tags();
                }, null));
                items.add(Map.entry(lb.loadBalancerArn(), config));
            }
        }
        upsertBatch(conn, items, "ALB", region);
        logger.info("  ✔ {} ALBs", items.size());
    }

    private static void collectAlbAttributes(ElasticLoadBalancingV2Client elb, Connection conn, String region) throws Exception {
        logger.info("Collecting ALB Attributes...");
        // Attributes include: idle_timeout.timeout_seconds, access_logs.s3.enabled,
        // access_logs.s3.bucket, deletion_protection.enabled,
        // routing.http2.enabled, routing.http.drop_invalid_header_fields.enabled,
        // waf.fail_open.enabled, load_balancing.cross_zone.enabled
        List<Map.Entry<String, Object>> items = new ArrayList<>();
        for (DescribeLoadBalancersResponse page : elb.describeLoadBalancersPaginator()) {
            for (LoadBalancer lb : page.loadBalancers()) {
                safeGet(() -> {
                    Map<String, Object> attrs = new LinkedHashMap<>();
                    attrs.put("loadBalancerArn", lb.loadBalancerArn());
                    elb.describeLoadBalancerAttributes(r -> r.loadBalancerArn(lb.loadBalancerArn()))
                       .attributes()
                       .forEach(a -> attrs.put(a.key(), a.value()));
                    items.add(Map.entry(lb.loadBalancerArn() + ":attrs", attrs));
                    return null;
                }, null);
            }
        }
        upsertBatch(conn, items, "ALB_ATTRIBUTES", region);
        logger.info("  ✔ {} ALB Attribute sets", items.size());
    }

    private static void collectListeners(ElasticLoadBalancingV2Client elb, Connection conn, String region) throws Exception {
        logger.info("Collecting ALB Listeners...");
        // Full listener: port, protocol, sslPolicy, certificates, defaultActions (forward/redirect/fixed-response)
        List<Map.Entry<String, Object>> items = new ArrayList<>();
        for (DescribeLoadBalancersResponse lbPage : elb.describeLoadBalancersPaginator()) {
            for (LoadBalancer lb : lbPage.loadBalancers()) {
                safeGet(() -> {
                    for (DescribeListenersResponse page : elb.describeListenersPaginator(r -> r.loadBalancerArn(lb.loadBalancerArn()))) {
                        for (Listener l : page.listeners()) {
                            // Listener already contains: port, protocol, sslPolicy,
                            // certificates, defaultActions — store the full object as-is
                            items.add(Map.entry(l.listenerArn(), (Object) l));
                        }
                    }
                    return null;
                }, null);
            }
        }
        upsertBatch(conn, items, "ALB_LISTENER", region);
        logger.info("  ✔ {} Listeners", items.size());
    }

    private static void collectListenerRules(ElasticLoadBalancingV2Client elb, Connection conn, String region) throws Exception {
        logger.info("Collecting Listener Rules...");
        // Rules: priority, conditions (path/host/header/query/method/ip), actions
        List<Map.Entry<String, Object>> items = new ArrayList<>();
        for (DescribeLoadBalancersResponse lbPage : elb.describeLoadBalancersPaginator()) {
            for (LoadBalancer lb : lbPage.loadBalancers()) {
                safeGet(() -> {
                    for (DescribeListenersResponse lPage : elb.describeListenersPaginator(r -> r.loadBalancerArn(lb.loadBalancerArn()))) {
                        for (Listener l : lPage.listeners()) {
                            safeGet(() -> {
                                DescribeRulesResponse rules = elb.describeRules(r -> r.listenerArn(l.listenerArn()));
                                for (Rule rule : rules.rules()) {
                                    Map<String, Object> config = new LinkedHashMap<>();
                                    config.put("rule", rule);
                                    config.put("listenerArn", l.listenerArn());
                                    items.add(Map.entry(rule.ruleArn(), config));
                                }
                                return null;
                            }, null);
                        }
                    }
                    return null;
                }, null);
            }
        }
        upsertBatch(conn, items, "ALB_LISTENER_RULE", region);
        logger.info("  ✔ {} Listener Rules", items.size());
    }

    private static void collectTargetGroups(ElasticLoadBalancingV2Client elb, Connection conn, String region) throws Exception {
        logger.info("Collecting Target Groups...");
        // Full TG: protocol, protocolVersion, port, targetType, healthCheck config, matcher, loadBalancerArns
        List<Map.Entry<String, Object>> items = new ArrayList<>();
        for (DescribeTargetGroupsResponse page : elb.describeTargetGroupsPaginator()) {
            for (TargetGroup tg : page.targetGroups()) {
                Map<String, Object> config = new LinkedHashMap<>();
                config.put("targetGroup", tg);
                // Attributes: stickiness, deregistration delay, slow start, LB algorithm, etc.
                config.put("attributes", safeGet(() ->
                        elb.describeTargetGroupAttributes(r -> r.targetGroupArn(tg.targetGroupArn())).attributes(), null));
                items.add(Map.entry(tg.targetGroupArn(), config));
            }
        }
        upsertBatch(conn, items, "ALB_TARGET_GROUP", region);
        logger.info("  ✔ {} Target Groups", items.size());
    }

    private static void collectTargetHealth(ElasticLoadBalancingV2Client elb, Connection conn, String region) throws Exception {
        logger.info("Collecting Target Health...");
        List<Map.Entry<String, Object>> items = new ArrayList<>();
        for (DescribeTargetGroupsResponse page : elb.describeTargetGroupsPaginator()) {
            for (TargetGroup tg : page.targetGroups()) {
                safeGet(() -> {
                    DescribeTargetHealthResponse health = elb.describeTargetHealth(r -> r.targetGroupArn(tg.targetGroupArn()));
                    Map<String, Object> config = new LinkedHashMap<>();
                    config.put("targetGroupArn", tg.targetGroupArn());
                    config.put("targets", health.targetHealthDescriptions());
                    items.add(Map.entry(tg.targetGroupArn() + ":health", config));
                    return null;
                }, null);
            }
        }
        upsertBatch(conn, items, "ALB_TARGET_HEALTH", region);
        logger.info("  ✔ {} Target Health records", items.size());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DATABASE
    // ─────────────────────────────────────────────────────────────────────────

    private static void collectRdsInstances(RdsClient rds, Connection conn, String region) throws Exception {
        logger.info("Collecting RDS Instances...");
        List<Map.Entry<String, Object>> items = new ArrayList<>();
        for (DescribeDbInstancesResponse page : rds.describeDBInstancesPaginator()) {
            for (DBInstance db : page.dbInstances()) items.add(Map.entry(db.dbInstanceIdentifier(), db));
        }
        upsertBatch(conn, items, "RDS_INSTANCE", region);
        logger.info("  ✔ {} RDS Instances", items.size());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SERVERLESS / CONTAINERS
    // ─────────────────────────────────────────────────────────────────────────

    private static void collectLambdaFunctions(LambdaClient lambda, Connection conn, String region) throws Exception {
        logger.info("Collecting Lambda Functions...");
        List<Map.Entry<String, Object>> items = new ArrayList<>();
        for (ListFunctionsResponse page : lambda.listFunctionsPaginator()) {
            for (FunctionConfiguration fn : page.functions()) {
                // getFunction = richer: code location, layers, concurrency, URL config, tags
                Map<String, Object> config = safeGet(() -> {
                    GetFunctionResponse full = lambda.getFunction(r -> r.functionName(fn.functionArn()));
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("configuration", full.configuration());
                    m.put("code",          full.code());
                    m.put("tags",          full.tags());
                    m.put("concurrency",   full.concurrency());
                    return m;
                }, Map.of("configuration", fn));
                items.add(Map.entry(fn.functionArn(), config));
            }
        }
        upsertBatch(conn, items, "LAMBDA_FUNCTION", region);
        logger.info("  ✔ {} Lambda Functions", items.size());
    }

    private static void collectEcsClusters(EcsClient ecs, Connection conn, String region) throws Exception {
        logger.info("Collecting ECS Clusters...");
        List<String> arns = new ArrayList<>();
        String token = null;
        do {
            software.amazon.awssdk.services.ecs.model.ListClustersRequest.Builder req =
                    software.amazon.awssdk.services.ecs.model.ListClustersRequest.builder();
            if (token != null) req.nextToken(token);
            software.amazon.awssdk.services.ecs.model.ListClustersResponse resp = ecs.listClusters(req.build());
            arns.addAll(resp.clusterArns());
            token = resp.nextToken();
        } while (token != null);

        List<Map.Entry<String, Object>> items = new ArrayList<>();
        for (int i = 0; i < arns.size(); i += 100) {
            List<String> batch = arns.subList(i, Math.min(i + 100, arns.size()));
            software.amazon.awssdk.services.ecs.model.DescribeClustersResponse resp =
                    ecs.describeClusters(r -> r.clusters(batch)
                                               .includeWithStrings("ATTACHMENTS", "SETTINGS", "STATISTICS", "TAGS"));
            for (Cluster c : resp.clusters()) items.add(Map.entry(c.clusterArn(), c));
        }
        upsertBatch(conn, items, "ECS_CLUSTER", region);
        logger.info("  ✔ {} ECS Clusters", items.size());
    }

    private static void collectEksClusters(EksClient eks, Connection conn, String region) throws Exception {
        logger.info("Collecting EKS Clusters...");
        List<String> names = new ArrayList<>();
        String token = null;
        do {
            software.amazon.awssdk.services.eks.model.ListClustersRequest.Builder req =
                    software.amazon.awssdk.services.eks.model.ListClustersRequest.builder();
            if (token != null) req.nextToken(token);
            software.amazon.awssdk.services.eks.model.ListClustersResponse resp = eks.listClusters(req.build());
            names.addAll(resp.clusters());
            token = resp.nextToken();
        } while (token != null);

        List<Map.Entry<String, Object>> items = new ArrayList<>();
        for (String name : names) {
            safeGet(() -> {
                software.amazon.awssdk.services.eks.model.DescribeClusterResponse resp =
                        eks.describeCluster(r -> r.name(name));
                items.add(Map.entry(resp.cluster().arn(), resp.cluster()));
                return null;
            }, null);
        }
        upsertBatch(conn, items, "EKS_CLUSTER", region);
        logger.info("  ✔ {} EKS Clusters", items.size());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MESSAGING
    // ─────────────────────────────────────────────────────────────────────────

    private static void collectSnsTopics(SnsClient sns, Connection conn, String region) throws Exception {
        logger.info("Collecting SNS Topics...");
        List<Map.Entry<String, Object>> items = new ArrayList<>();
        for (ListTopicsResponse page : sns.listTopicsPaginator()) {
            for (Topic t : page.topics()) {
                Map<String, Object> config = safeGet(() ->
                                (Map<String, Object>) (Map) sns.getTopicAttributes(r -> r.topicArn(t.topicArn())).attributes(),
                        Map.of("topicArn", t.topicArn()));
                items.add(Map.entry(t.topicArn(), config));
            }
        }
        upsertBatch(conn, items, "SNS_TOPIC", region);
        logger.info("  ✔ {} SNS Topics", items.size());
    }

    private static void collectSqsQueues(SqsClient sqs, Connection conn, String region) throws Exception {
        logger.info("Collecting SQS Queues...");
        List<String> urls = new ArrayList<>();
        String token = null;
        do {
            ListQueuesRequest.Builder req = ListQueuesRequest.builder().maxResults(1000);
            if (token != null) req.nextToken(token);
            ListQueuesResponse resp = sqs.listQueues(req.build());
            urls.addAll(resp.queueUrls());
            token = resp.nextToken();
        } while (token != null);

        List<Map.Entry<String, Object>> items = new ArrayList<>();
        for (String url : urls) {
            // QueueAttributeName.ALL fetches every attribute in one call
            Map<String, Object> config = safeGet(() ->
                            (Map<String, Object>) (Map) sqs.getQueueAttributes(r -> r
                                                                   .queueUrl(url)
                                                                   .attributeNames(QueueAttributeName.ALL))
                                                           .attributesAsStrings(),
                    Map.of("queueUrl", url));
            items.add(Map.entry(url, config));
        }
        upsertBatch(conn, items, "SQS_QUEUE", region);
        logger.info("  ✔ {} SQS Queues", items.size());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // IAM
    // ─────────────────────────────────────────────────────────────────────────

    private static void collectIamUsers(IamClient iam, Connection conn, String region) throws Exception {
        logger.info("Collecting IAM Users...");
        List<Map.Entry<String, Object>> items = new ArrayList<>();
        for (ListUsersResponse page : iam.listUsersPaginator()) {
            for (User u : page.users()) items.add(Map.entry(u.userId(), u));
        }
        upsertBatch(conn, items, "IAM_USER", region);
        logger.info("  ✔ {} IAM Users", items.size());
    }

    private static void collectIamRoles(IamClient iam, Connection conn, String region) throws Exception {
        logger.info("Collecting IAM Roles...");
        List<Map.Entry<String, Object>> items = new ArrayList<>();
        for (ListRolesResponse page : iam.listRolesPaginator()) {
            for (Role r : page.roles()) items.add(Map.entry(r.roleId(), r));
        }
        upsertBatch(conn, items, "IAM_ROLE", region);
        logger.info("  ✔ {} IAM Roles", items.size());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    @FunctionalInterface
    interface Supplier<T> { T get() throws Exception; }

    private static <T> T safeGet(Supplier<T> fn, T fallback) {
        try { return fn.get(); } catch (Exception e) { return fallback; }
    }

    private static String getEnv(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.trim().isEmpty()) {
            if (defaultValue == null) { logger.error("Required env var not set: {}", name); System.exit(1); }
            return defaultValue;
        }
        return value;
    }
}
package com.awsBuilder.builder.config.controller;

import com.awsBuilder.builder.config.model.*;
import com.awsBuilder.builder.config.service.ConfigService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * REST controller for catalog API endpoints.
 * Provides /api/config/* endpoints for fetching AWS resource catalog data.
 */
@RestController
@RequestMapping("/api/config")
public class ConfigController {
    private static final Logger logger = LoggerFactory.getLogger(ConfigController.class);
    private static final String CACHE_CONTROL_HEADER = "max-age=300, public";

    private final ConfigService configService;

    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    // EC2 Instances
    @GetMapping("/ec2-instances")
    public ResponseEntity<List<Ec2Instance>> getEc2Instances(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String amiId) {
        logger.info("GET /api/config/ec2-instances with region: {}, amiId: {}", region, amiId);
        List<Ec2Instance> instances = configService.getEc2Instances(region, amiId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL_HEADER)
                .body(instances);
    }

    // EBS Volumes
    @GetMapping("/ebs-volumes")
    public ResponseEntity<List<EbsVolume>> getEbsVolumes(
            @RequestParam(required = false) String instanceId,
            @RequestParam(required = false) String region) {
        logger.info("GET /api/config/ebs-volumes with instanceId: {}, region: {}", instanceId, region);
        List<EbsVolume> volumes = configService.getEbsVolumes(instanceId, region);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL_HEADER)
                .body(volumes);
    }

    // S3 Buckets
    @GetMapping("/s3-buckets")
    public ResponseEntity<List<S3Bucket>> getS3Buckets(
            @RequestParam(required = false) String region) {
        logger.info("GET /api/config/s3-buckets with region: {}", region);
        List<S3Bucket> buckets = configService.getS3Buckets(region);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL_HEADER)
                .body(buckets);
    }

    // VPCs
    @GetMapping("/vpcs")
    public ResponseEntity<List<Vpc>> getVpcs(
            @RequestParam(required = false) String region) {
        logger.info("GET /api/config/vpcs with region: {}", region);
        List<Vpc> vpcs = configService.getVpcs(region);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL_HEADER)
                .body(vpcs);
    }

    // Subnets
    @GetMapping("/subnets")
    public ResponseEntity<List<Subnet>> getSubnets(
            @RequestParam(required = false) String vpcId,
            @RequestParam(required = false) String region) {
        logger.info("GET /api/config/subnets with vpcId: {}, region: {}", vpcId, region);
        List<Subnet> subnets = configService.getSubnets(vpcId, region);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL_HEADER)
                .body(subnets);
    }

    // Internet Gateways
    @GetMapping("/internet-gateways")
    public ResponseEntity<List<InternetGateway>> getInternetGateways(
            @RequestParam(required = false) String vpcId,
            @RequestParam(required = false) String region) {
        logger.info("GET /api/config/internet-gateways with vpcId: {}, region: {}", vpcId, region);
        List<InternetGateway> gateways = configService.getInternetGateways(vpcId, region);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL_HEADER)
                .body(gateways);
    }

    // Application Load Balancers
    @GetMapping("/albs")
    public ResponseEntity<List<Alb>> getAlbs(
            @RequestParam(required = false) String vpcId,
            @RequestParam(required = false) String region) {
        logger.info("GET /api/config/albs with vpcId: {}, region: {}", vpcId, region);
        List<Alb> albs = configService.getAlbs(vpcId, region);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL_HEADER)
                .body(albs);
    }

    // ALB Listeners
    @GetMapping("/alb-listeners")
    public ResponseEntity<List<AlbListener>> getAlbListeners(
            @RequestParam(required = false) String albArn) {
        logger.info("GET /api/config/alb-listeners with albArn: {}", albArn);
        List<AlbListener> listeners = configService.getAlbListeners(albArn);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL_HEADER)
                .body(listeners);
    }

    // ALB Target Groups
    @GetMapping("/alb-target-groups")
    public ResponseEntity<List<AlbTargetGroup>> getAlbTargetGroups(
            @RequestParam(required = false) String vpcId) {
        logger.info("GET /api/config/alb-target-groups with vpcId: {}", vpcId);
        List<AlbTargetGroup> targetGroups = configService.getAlbTargetGroups(vpcId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL_HEADER)
                .body(targetGroups);
    }

    // Lambda Functions
    @GetMapping("/lambda-functions")
    public ResponseEntity<List<LambdaFunction>> getLambdaFunctions(
            @RequestParam(required = false) String region) {
        logger.info("GET /api/config/lambda-functions with region: {}", region);
        List<LambdaFunction> functions = configService.getLambdaFunctions(region);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL_HEADER)
                .body(functions);
    }

    // DynamoDB Tables
    @GetMapping("/dynamodb-tables")
    public ResponseEntity<List<DynamoDbTable>> getDynamoDbTables(
            @RequestParam(required = false) String region) {
        logger.info("GET /api/config/dynamodb-tables with region: {}", region);
        List<DynamoDbTable> tables = configService.getDynamoDbTables(region);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL_HEADER)
                .body(tables);
    }

    // API Gateways
    @GetMapping("/api-gateways")
    public ResponseEntity<List<ApiGateway>> getApiGateways(
            @RequestParam(required = false) String region) {
        logger.info("GET /api/config/api-gateways with region: {}", region);
        List<ApiGateway> gateways = configService.getApiGateways(region);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL_HEADER)
                .body(gateways);
    }

    // SQS Queues
    @GetMapping("/sqs-queues")
    public ResponseEntity<List<SqsQueue>> getSqsQueues(
            @RequestParam(required = false) String region) {
        logger.info("GET /api/config/sqs-queues with region: {}", region);
        List<SqsQueue> queues = configService.getSqsQueues(region);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL_HEADER)
                .body(queues);
    }

    // SNS Topics
    @GetMapping("/sns-topics")
    public ResponseEntity<List<SnsTopic>> getSnsTopics(
            @RequestParam(required = false) String region) {
        logger.info("GET /api/config/sns-topics with region: {}", region);
        List<SnsTopic> topics = configService.getSnsTopics(region);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL_HEADER)
                .body(topics);
    }

    // Kinesis Streams
    @GetMapping("/kinesis-streams")
    public ResponseEntity<List<KinesisStream>> getKinesisStreams(
            @RequestParam(required = false) String region) {
        logger.info("GET /api/config/kinesis-streams with region: {}", region);
        List<KinesisStream> streams = configService.getKinesisStreams(region);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL_HEADER)
                .body(streams);
    }

    // CloudWatch Alarms
    @GetMapping("/cloudwatch-alarms")
    public ResponseEntity<List<CloudWatchAlarm>> getCloudWatchAlarms(
            @RequestParam(required = false) String region) {
        logger.info("GET /api/config/cloudwatch-alarms with region: {}", region);
        List<CloudWatchAlarm> alarms = configService.getCloudWatchAlarms(region);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL_HEADER)
                .body(alarms);
    }

    // X-Ray Sampling Rules
    @GetMapping("/xray-sampling-rules")
    public ResponseEntity<List<XRaySamplingRule>> getXRaySamplingRules(
            @RequestParam(required = false) String region) {
        logger.info("GET /api/config/xray-sampling-rules with region: {}", region);
        List<XRaySamplingRule> rules = configService.getXRaySamplingRules(region);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL_HEADER)
                .body(rules);
    }

    // CloudTrail Trails
    @GetMapping("/cloudtrail-trails")
    public ResponseEntity<List<CloudTrailTrail>> getCloudTrailTrails(
            @RequestParam(required = false) String region) {
        logger.info("GET /api/config/cloudtrail-trails with region: {}", region);
        List<CloudTrailTrail> trails = configService.getCloudTrailTrails(region);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL_HEADER)
                .body(trails);
    }

    // ECR Repositories
    @GetMapping("/ecr-repositories")
    public ResponseEntity<List<EcrRepository>> getEcrRepositories(
            @RequestParam(required = false) String region) {
        logger.info("GET /api/config/ecr-repositories with region: {}", region);
        List<EcrRepository> repos = configService.getEcrRepositories(region);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL_HEADER)
                .body(repos);
    }

    // ECS Clusters
    @GetMapping("/ecs-clusters")
    public ResponseEntity<List<EcsCluster>> getEcsClusters(
            @RequestParam(required = false) String region) {
        logger.info("GET /api/config/ecs-clusters with region: {}", region);
        List<EcsCluster> clusters = configService.getEcsClusters(region);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL_HEADER)
                .body(clusters);
    }

    // ECS Task Definitions
    @GetMapping("/ecs-task-definitions")
    public ResponseEntity<List<EcsTaskDefinition>> getEcsTaskDefinitions(
            @RequestParam(required = false) String region) {
        logger.info("GET /api/config/ecs-task-definitions with region: {}", region);
        List<EcsTaskDefinition> taskDefs = configService.getEcsTaskDefinitions(region);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL_HEADER)
                .body(taskDefs);
    }

    // ElastiCache Clusters
    @GetMapping("/elasticache-clusters")
    public ResponseEntity<List<ElastiCacheCluster>> getElastiCacheClusters(
            @RequestParam(required = false) String region) {
        logger.info("GET /api/config/elasticache-clusters with region: {}", region);
        List<ElastiCacheCluster> clusters = configService.getElastiCacheClusters(region);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL_HEADER)
                .body(clusters);
    }

    // EventBridge Rules
    @GetMapping("/eventbridge-rules")
    public ResponseEntity<List<EventBridgeRule>> getEventBridgeRules(
            @RequestParam(required = false) String region) {
        logger.info("GET /api/config/eventbridge-rules with region: {}", region);
        List<EventBridgeRule> rules = configService.getEventBridgeRules(region);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL_HEADER)
                .body(rules);
    }

    // Cache Invalidation
    @PostMapping("/cache/invalidate")
    public ResponseEntity<Void> invalidateCache() {
        logger.info("POST /api/config/cache/invalidate");
        configService.invalidateCache();
        return ResponseEntity.noContent().build();
    }
}

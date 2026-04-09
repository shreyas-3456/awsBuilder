package com.awsBuilder.builder.config.service;

import com.awsBuilder.builder.config.model.*;
import com.awsBuilder.builder.config.repository.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Service layer for catalog operations.
 * Handles business logic and caching for AWS resource catalog data.
 */
@Service
public class ConfigService {
    private static final Logger logger = LoggerFactory.getLogger(ConfigService.class);

    private final Ec2InstanceRepository ec2InstanceRepository;
    private final EbsVolumeRepository ebsVolumeRepository;
    private final S3BucketRepository s3BucketRepository;
    private final VpcRepository vpcRepository;
    private final SubnetRepository subnetRepository;
    private final InternetGatewayRepository internetGatewayRepository;
    private final AlbRepository albRepository;
    private final AlbListenerRepository albListenerRepository;
    private final AlbTargetGroupRepository albTargetGroupRepository;
    private final LambdaFunctionRepository lambdaFunctionRepository;
    private final DynamoDbTableRepository dynamoDbTableRepository;
    private final ApiGatewayRepository apiGatewayRepository;
    private final SqsQueueRepository sqsQueueRepository;
    private final SnsTopicRepository snsTopicRepository;
    private final KinesisStreamRepository kinesisStreamRepository;
    private final CloudWatchAlarmRepository cloudWatchAlarmRepository;
    private final XRaySamplingRuleRepository xRaySamplingRuleRepository;
    private final CloudTrailTrailRepository cloudTrailTrailRepository;
    private final EcrRepositoryRepository ecrRepositoryRepository;
    private final EcsClusterRepository ecsClusterRepository;
    private final EcsTaskDefinitionRepository ecsTaskDefinitionRepository;
    private final ElastiCacheClusterRepository elastiCacheClusterRepository;
    private final EventBridgeRuleRepository eventBridgeRuleRepository;

    public ConfigService(
            Ec2InstanceRepository ec2InstanceRepository,
            EbsVolumeRepository ebsVolumeRepository,
            S3BucketRepository s3BucketRepository,
            VpcRepository vpcRepository,
            SubnetRepository subnetRepository,
            InternetGatewayRepository internetGatewayRepository,
            AlbRepository albRepository,
            AlbListenerRepository albListenerRepository,
            AlbTargetGroupRepository albTargetGroupRepository,
            LambdaFunctionRepository lambdaFunctionRepository,
            DynamoDbTableRepository dynamoDbTableRepository,
            ApiGatewayRepository apiGatewayRepository,
            SqsQueueRepository sqsQueueRepository,
            SnsTopicRepository snsTopicRepository,
            KinesisStreamRepository kinesisStreamRepository,
            CloudWatchAlarmRepository cloudWatchAlarmRepository,
            XRaySamplingRuleRepository xRaySamplingRuleRepository,
            CloudTrailTrailRepository cloudTrailTrailRepository,
            EcrRepositoryRepository ecrRepositoryRepository,
            EcsClusterRepository ecsClusterRepository,
            EcsTaskDefinitionRepository ecsTaskDefinitionRepository,
            ElastiCacheClusterRepository elastiCacheClusterRepository,
            EventBridgeRuleRepository eventBridgeRuleRepository) {
        this.ec2InstanceRepository = ec2InstanceRepository;
        this.ebsVolumeRepository = ebsVolumeRepository;
        this.s3BucketRepository = s3BucketRepository;
        this.vpcRepository = vpcRepository;
        this.subnetRepository = subnetRepository;
        this.internetGatewayRepository = internetGatewayRepository;
        this.albRepository = albRepository;
        this.albListenerRepository = albListenerRepository;
        this.albTargetGroupRepository = albTargetGroupRepository;
        this.lambdaFunctionRepository = lambdaFunctionRepository;
        this.dynamoDbTableRepository = dynamoDbTableRepository;
        this.apiGatewayRepository = apiGatewayRepository;
        this.sqsQueueRepository = sqsQueueRepository;
        this.snsTopicRepository = snsTopicRepository;
        this.kinesisStreamRepository = kinesisStreamRepository;
        this.cloudWatchAlarmRepository = cloudWatchAlarmRepository;
        this.xRaySamplingRuleRepository = xRaySamplingRuleRepository;
        this.cloudTrailTrailRepository = cloudTrailTrailRepository;
        this.ecrRepositoryRepository = ecrRepositoryRepository;
        this.ecsClusterRepository = ecsClusterRepository;
        this.ecsTaskDefinitionRepository = ecsTaskDefinitionRepository;
        this.elastiCacheClusterRepository = elastiCacheClusterRepository;
        this.eventBridgeRuleRepository = eventBridgeRuleRepository;
    }

    // EC2 Instances
    @Cacheable(value = "catalogData", key = "'ec2:' + (#region ?: 'all') + ':' + (#amiId ?: 'all')")
    public List<Ec2Instance> getEc2Instances(String region, String amiId) {
        logger.info("Fetching EC2 instances with region: {}, amiId: {}", region, amiId);
        try {
            boolean hasRegion = region != null && !region.isEmpty();
            boolean hasAmi = amiId != null && !amiId.isEmpty();

            if (hasRegion && hasAmi) {
                return ec2InstanceRepository.findByRegionAndAmiIdContainingIgnoreCase(region, amiId);
            } else if (hasRegion) {
                return ec2InstanceRepository.findByRegion(region);
            } else if (hasAmi) {
                return ec2InstanceRepository.findByAmiIdContainingIgnoreCase(amiId);
            } else {
                return ec2InstanceRepository.findAll();
            }
        } catch (Exception e) {
            logger.error("Error fetching EC2 instances: {}", e.getMessage(), e);
            throw new DatabaseException("Failed to retrieve EC2 instances from database", e);
        }
    }

    // EBS Volumes
    @Cacheable(value = "catalogData", key = "'ebs:' + (#instanceId ?: 'all') + ':' + (#region ?: 'all')")
    public List<EbsVolume> getEbsVolumes(String instanceId, String region) {
        logger.info("Fetching EBS volumes with instance: {}, region: {}", instanceId, region);
        try {
            if (instanceId != null && !instanceId.isEmpty()) {
                return ebsVolumeRepository.findByInstanceId(instanceId);
            } else if (region != null && !region.isEmpty()) {
                return ebsVolumeRepository.findByRegion(region);
            } else {
                return ebsVolumeRepository.findAll();
            }
        } catch (Exception e) {
            logger.error("Error fetching EBS volumes: {}", e.getMessage(), e);
            throw new DatabaseException("Failed to retrieve EBS volumes from database", e);
        }
    }

    // S3 Buckets
    @Cacheable(value = "catalogData", key = "'s3:' + (#region ?: 'all')")
    public List<S3Bucket> getS3Buckets(String region) {
        logger.info("Fetching S3 buckets with region filter: {}", region);
        try {
            if (region != null && !region.isEmpty()) {
                return s3BucketRepository.findByRegion(region);
            } else {
                return s3BucketRepository.findAll();
            }
        } catch (Exception e) {
            logger.error("Error fetching S3 buckets: {}", e.getMessage(), e);
            throw new DatabaseException("Failed to retrieve S3 buckets from database", e);
        }
    }

    // VPCs
    @Cacheable(value = "catalogData", key = "'vpc:' + (#region ?: 'all')")
    public List<Vpc> getVpcs(String region) {
        logger.info("Fetching VPCs with region filter: {}", region);
        try {
            if (region != null && !region.isEmpty()) {
                return vpcRepository.findByRegion(region);
            } else {
                return vpcRepository.findAll();
            }
        } catch (Exception e) {
            logger.error("Error fetching VPCs: {}", e.getMessage(), e);
            throw new DatabaseException("Failed to retrieve VPCs from database", e);
        }
    }

    // Subnets
    @Cacheable(value = "catalogData", key = "'subnet:' + (#vpcId ?: 'all') + ':' + (#region ?: 'all')")
    public List<Subnet> getSubnets(String vpcId, String region) {
        logger.info("Fetching subnets with vpcId: {}, region: {}", vpcId, region);
        try {
            if (vpcId != null && !vpcId.isEmpty() && region != null && !region.isEmpty()) {
                return subnetRepository.findByVpcIdAndRegion(vpcId, region);
            } else if (vpcId != null && !vpcId.isEmpty()) {
                return subnetRepository.findByVpcId(vpcId);
            } else if (region != null && !region.isEmpty()) {
                return subnetRepository.findByRegion(region);
            } else {
                return subnetRepository.findAll();
            }
        } catch (Exception e) {
            logger.error("Error fetching subnets: {}", e.getMessage(), e);
            throw new DatabaseException("Failed to retrieve subnets from database", e);
        }
    }

    // Internet Gateways
    @Cacheable(value = "catalogData", key = "'igw:' + (#vpcId ?: 'all') + ':' + (#region ?: 'all')")
    public List<InternetGateway> getInternetGateways(String vpcId, String region) {
        logger.info("Fetching internet gateways with vpcId: {}, region: {}", vpcId, region);
        try {
            if (vpcId != null && !vpcId.isEmpty()) {
                return internetGatewayRepository.findByVpcId(vpcId);
            } else if (region != null && !region.isEmpty()) {
                return internetGatewayRepository.findByRegion(region);
            } else {
                return internetGatewayRepository.findAll();
            }
        } catch (Exception e) {
            logger.error("Error fetching internet gateways: {}", e.getMessage(), e);
            throw new DatabaseException("Failed to retrieve internet gateways from database", e);
        }
    }

    // Application Load Balancers
    @Cacheable(value = "catalogData", key = "'alb:' + (#vpcId ?: 'all') + ':' + (#region ?: 'all')")
    public List<Alb> getAlbs(String vpcId, String region) {
        logger.info("Fetching ALBs with vpcId: {}, region: {}", vpcId, region);
        try {
            if (vpcId != null && !vpcId.isEmpty() && region != null && !region.isEmpty()) {
                return albRepository.findByVpcIdAndRegion(vpcId, region);
            } else if (vpcId != null && !vpcId.isEmpty()) {
                return albRepository.findByVpcId(vpcId);
            } else if (region != null && !region.isEmpty()) {
                return albRepository.findByRegion(region);
            } else {
                return albRepository.findAll();
            }
        } catch (Exception e) {
            logger.error("Error fetching ALBs: {}", e.getMessage(), e);
            throw new DatabaseException("Failed to retrieve ALBs from database", e);
        }
    }

    // ALB Listeners
    @Cacheable(value = "catalogData", key = "'listener:' + (#albArn ?: 'all')")
    public List<AlbListener> getAlbListeners(String albArn) {
        logger.info("Fetching ALB listeners with albArn: {}", albArn);
        try {
            if (albArn != null && !albArn.isEmpty()) {
                return albListenerRepository.findByAlbArn(albArn);
            } else {
                return albListenerRepository.findAll();
            }
        } catch (Exception e) {
            logger.error("Error fetching ALB listeners: {}", e.getMessage(), e);
            throw new DatabaseException("Failed to retrieve ALB listeners from database", e);
        }
    }

    // ALB Target Groups
    @Cacheable(value = "catalogData", key = "'tg:' + (#vpcId ?: 'all')")
    public List<AlbTargetGroup> getAlbTargetGroups(String vpcId) {
        logger.info("Fetching ALB target groups with vpcId: {}", vpcId);
        try {
            if (vpcId != null && !vpcId.isEmpty()) {
                return albTargetGroupRepository.findByVpcId(vpcId);
            } else {
                return albTargetGroupRepository.findAll();
            }
        } catch (Exception e) {
            logger.error("Error fetching ALB target groups: {}", e.getMessage(), e);
            throw new DatabaseException("Failed to retrieve ALB target groups from database", e);
        }
    }

    // Lambda Functions
    @Cacheable(value = "catalogData", key = "'lambda:' + (#region ?: 'all')")
    public List<LambdaFunction> getLambdaFunctions(String region) {
        logger.info("Fetching Lambda functions with region: {}", region);
        try {
            if (region != null && !region.isEmpty()) {
                return lambdaFunctionRepository.findByRegion(region);
            } else {
                return lambdaFunctionRepository.findAll();
            }
        } catch (Exception e) {
            logger.error("Error fetching Lambda functions: {}", e.getMessage(), e);
            throw new DatabaseException("Failed to retrieve Lambda functions from database", e);
        }
    }

    // DynamoDB Tables
    @Cacheable(value = "catalogData", key = "'dynamodb:' + (#region ?: 'all')")
    public List<DynamoDbTable> getDynamoDbTables(String region) {
        logger.info("Fetching DynamoDB tables with region: {}", region);
        try {
            if (region != null && !region.isEmpty()) {
                return dynamoDbTableRepository.findByRegion(region);
            } else {
                return dynamoDbTableRepository.findAll();
            }
        } catch (Exception e) {
            logger.error("Error fetching DynamoDB tables: {}", e.getMessage(), e);
            throw new DatabaseException("Failed to retrieve DynamoDB tables from database", e);
        }
    }

    // API Gateways
    @Cacheable(value = "catalogData", key = "'apigw:' + (#region ?: 'all')")
    public List<ApiGateway> getApiGateways(String region) {
        logger.info("Fetching API Gateways with region: {}", region);
        try {
            if (region != null && !region.isEmpty()) {
                return apiGatewayRepository.findByRegion(region);
            } else {
                return apiGatewayRepository.findAll();
            }
        } catch (Exception e) {
            logger.error("Error fetching API Gateways: {}", e.getMessage(), e);
            throw new DatabaseException("Failed to retrieve API Gateways from database", e);
        }
    }

    // SQS Queues
    @Cacheable(value = "catalogData", key = "'sqs:' + (#region ?: 'all')")
    public List<SqsQueue> getSqsQueues(String region) {
        logger.info("Fetching SQS queues with region: {}", region);
        try {
            if (region != null && !region.isEmpty()) {
                return sqsQueueRepository.findByRegion(region);
            } else {
                return sqsQueueRepository.findAll();
            }
        } catch (Exception e) {
            logger.error("Error fetching SQS queues: {}", e.getMessage(), e);
            throw new DatabaseException("Failed to retrieve SQS queues from database", e);
        }
    }

    // SNS Topics
    @Cacheable(value = "catalogData", key = "'sns:' + (#region ?: 'all')")
    public List<SnsTopic> getSnsTopics(String region) {
        logger.info("Fetching SNS topics with region: {}", region);
        try {
            if (region != null && !region.isEmpty()) {
                return snsTopicRepository.findByRegion(region);
            } else {
                return snsTopicRepository.findAll();
            }
        } catch (Exception e) {
            logger.error("Error fetching SNS topics: {}", e.getMessage(), e);
            throw new DatabaseException("Failed to retrieve SNS topics from database", e);
        }
    }

    // Kinesis Streams
    @Cacheable(value = "catalogData", key = "'kinesis:' + (#region ?: 'all')")
    public List<KinesisStream> getKinesisStreams(String region) {
        logger.info("Fetching Kinesis streams with region: {}", region);
        try {
            if (region != null && !region.isEmpty()) {
                return kinesisStreamRepository.findByRegion(region);
            } else {
                return kinesisStreamRepository.findAll();
            }
        } catch (Exception e) {
            logger.error("Error fetching Kinesis streams: {}", e.getMessage(), e);
            throw new DatabaseException("Failed to retrieve Kinesis streams from database", e);
        }
    }

    // CloudWatch Alarms
    @Cacheable(value = "catalogData", key = "'cloudwatch:' + (#region ?: 'all')")
    public List<CloudWatchAlarm> getCloudWatchAlarms(String region) {
        logger.info("Fetching CloudWatch alarms with region: {}", region);
        try {
            if (region != null && !region.isEmpty()) {
                return cloudWatchAlarmRepository.findByRegion(region);
            } else {
                return cloudWatchAlarmRepository.findAll();
            }
        } catch (Exception e) {
            logger.error("Error fetching CloudWatch alarms: {}", e.getMessage(), e);
            throw new DatabaseException("Failed to retrieve CloudWatch alarms from database", e);
        }
    }

    // X-Ray Sampling Rules
    @Cacheable(value = "catalogData", key = "'xray:' + (#region ?: 'all')")
    public List<XRaySamplingRule> getXRaySamplingRules(String region) {
        logger.info("Fetching X-Ray sampling rules with region: {}", region);
        try {
            if (region != null && !region.isEmpty()) {
                return xRaySamplingRuleRepository.findByRegion(region);
            } else {
                return xRaySamplingRuleRepository.findAll();
            }
        } catch (Exception e) {
            logger.error("Error fetching X-Ray sampling rules: {}", e.getMessage(), e);
            throw new DatabaseException("Failed to retrieve X-Ray sampling rules from database", e);
        }
    }

    // CloudTrail Trails
    @Cacheable(value = "catalogData", key = "'cloudtrail:' + (#region ?: 'all')")
    public List<CloudTrailTrail> getCloudTrailTrails(String region) {
        logger.info("Fetching CloudTrail trails with region: {}", region);
        try {
            if (region != null && !region.isEmpty()) {
                return cloudTrailTrailRepository.findByRegion(region);
            } else {
                return cloudTrailTrailRepository.findAll();
            }
        } catch (Exception e) {
            logger.error("Error fetching CloudTrail trails: {}", e.getMessage(), e);
            throw new DatabaseException("Failed to retrieve CloudTrail trails from database", e);
        }
    }

    // ECR Repositories
    @Cacheable(value = "catalogData", key = "'ecr:' + (#region ?: 'all')")
    public List<EcrRepository> getEcrRepositories(String region) {
        logger.info("Fetching ECR repositories with region: {}", region);
        try {
            if (region != null && !region.isEmpty()) {
                return ecrRepositoryRepository.findByRegion(region);
            } else {
                return ecrRepositoryRepository.findAll();
            }
        } catch (Exception e) {
            logger.error("Error fetching ECR repositories: {}", e.getMessage(), e);
            throw new DatabaseException("Failed to retrieve ECR repositories from database", e);
        }
    }

    // ECS Clusters
    @Cacheable(value = "catalogData", key = "'ecs:' + (#region ?: 'all')")
    public List<EcsCluster> getEcsClusters(String region) {
        logger.info("Fetching ECS clusters with region: {}", region);
        try {
            if (region != null && !region.isEmpty()) {
                return ecsClusterRepository.findByRegion(region);
            } else {
                return ecsClusterRepository.findAll();
            }
        } catch (Exception e) {
            logger.error("Error fetching ECS clusters: {}", e.getMessage(), e);
            throw new DatabaseException("Failed to retrieve ECS clusters from database", e);
        }
    }

    // ECS Task Definitions
    @Cacheable(value = "catalogData", key = "'ecstask:' + (#region ?: 'all')")
    public List<EcsTaskDefinition> getEcsTaskDefinitions(String region) {
        logger.info("Fetching ECS task definitions with region: {}", region);
        try {
            if (region != null && !region.isEmpty()) {
                return ecsTaskDefinitionRepository.findByRegion(region);
            } else {
                return ecsTaskDefinitionRepository.findAll();
            }
        } catch (Exception e) {
            logger.error("Error fetching ECS task definitions: {}", e.getMessage(), e);
            throw new DatabaseException("Failed to retrieve ECS task definitions from database", e);
        }
    }

    // ElastiCache Clusters
    @Cacheable(value = "catalogData", key = "'elasticache:' + (#region ?: 'all')")
    public List<ElastiCacheCluster> getElastiCacheClusters(String region) {
        logger.info("Fetching ElastiCache clusters with region: {}", region);
        try {
            if (region != null && !region.isEmpty()) {
                return elastiCacheClusterRepository.findByRegion(region);
            } else {
                return elastiCacheClusterRepository.findAll();
            }
        } catch (Exception e) {
            logger.error("Error fetching ElastiCache clusters: {}", e.getMessage(), e);
            throw new DatabaseException("Failed to retrieve ElastiCache clusters from database", e);
        }
    }

    // EventBridge Rules
    @Cacheable(value = "catalogData", key = "'eventbridge:' + (#region ?: 'all')")
    public List<EventBridgeRule> getEventBridgeRules(String region) {
        logger.info("Fetching EventBridge rules with region: {}", region);
        try {
            if (region != null && !region.isEmpty()) {
                return eventBridgeRuleRepository.findByRegion(region);
            } else {
                return eventBridgeRuleRepository.findAll();
            }
        } catch (Exception e) {
            logger.error("Error fetching EventBridge rules: {}", e.getMessage(), e);
            throw new DatabaseException("Failed to retrieve EventBridge rules from database", e);
        }
    }

    // Cache Invalidation
    @CacheEvict(value = "catalogData", allEntries = true)
    public void invalidateCache() {
        logger.info("Invalidating all catalog cache entries");
    }
}

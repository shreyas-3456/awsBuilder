/**
 * Frontend service for AWS catalog API integration.
 * Provides TypeScript interfaces and API client methods for catalog endpoints.
 */

// Catalog resource type definitions
export interface Ec2Instance {
  id: number;
  instanceId: string;
  instanceType: string;
  name: string;
  state: string;
  region: string;
  vpcId: string;
  subnetId: string;
  availabilityZone: string;
  privateIp: string;
  publicIp: string;
  amiId: string;
  keyName: string;
  vcpuCount: number;
  memoryGib: number;
  tags: Record<string, any>;
  collectedAt: string;
}

export interface EbsVolume {
  id: number;
  volumeId: string;
  volumeType: string;
  sizeGb: number;
  state: string;
  encrypted: boolean;
  region: string;
  availabilityZone: string;
  instanceId: string;
  deviceName: string;
  iops: number;
  throughput: number;
  tags: Record<string, any>;
  collectedAt: string;
}

export interface S3Bucket {
  id: number;
  bucketName: string;
  region: string;
  creationDate: string;
  versioning: string;
  encryptionRules: Array<Record<string, any>>;
  publicAccessBlock: Record<string, any>;
  tags: Record<string, any>;
  collectedAt: string;
}

export interface Vpc {
  id: number;
  vpcId: string;
  region: string;
  cidrBlock: string;
  state: string;
  isDefault: boolean;
  instanceTenancy: string;
  tags: Record<string, any>;
  collectedAt: string;
}

export interface Subnet {
  id: number;
  subnetId: string;
  vpcId: string;
  region: string;
  availabilityZone: string;
  cidrBlock: string;
  state: string;
  mapPublicIpOnLaunch: boolean;
  availableIpAddressCount: number;
  tags: Record<string, any>;
  collectedAt: string;
}

export interface InternetGateway {
  id: number;
  igwId: string;
  region: string;
  state: string;
  vpcId: string;
  tags: Record<string, any>;
  collectedAt: string;
}

export interface Alb {
  id: number;
  albArn: string;
  albName: string;
  region: string;
  dnsName: string;
  scheme: string;
  state: string;
  vpcId: string;
  type: string;
  securityGroups: string[];
  availabilityZones: Array<Record<string, any>>;
  tags: Record<string, any>;
  collectedAt: string;
}

export interface AlbListener {
  id: number;
  listenerArn: string;
  albArn: string;
  region: string;
  port: number;
  protocol: string;
  sslPolicy: string;
  certificates: Array<Record<string, any>>;
  defaultActions: Array<Record<string, any>>;
  tags: Record<string, any>;
  collectedAt: string;
}

export interface AlbTargetGroup {
  id: number;
  tgArn: string;
  tgName: string;
  region: string;
  protocol: string;
  port: number;
  vpcId: string;
  targetType: string;
  healthCheckEnabled: boolean;
  healthCheckProtocol: string;
  healthCheckPath: string;
  tags: Record<string, any>;
  collectedAt: string;
}

export interface LambdaFunction {
  functionArn: string;
  functionName: string;
  region: string;
  runtime: string;
  handler: string;
  memoryMb: number;
  memoryMinMb: number;
  memoryMaxMb: number;
  timeoutSeconds: number;
  timeoutMaxSeconds: number;
  ephemeralStorageMaxMb: number;
  concurrencyLimit: number;
  language: string;
  status: string;
  description: string;
  supportedArchitectures: Record<string, any>;
  tags: Record<string, any>;
  source: string;
  collectedAt: string;
}

export interface DynamoDbTable {
  tableArn: string;
  tableName: string;
  region: string;
  tableStatus: string;
  billingMode: string;
  readCapacityUnits: number;
  writeCapacityUnits: number;
  tableClass: string;
  streamEnabled: boolean;
  encryptionType: string;
  pointInTimeRecovery: boolean;
  ttlEnabled: boolean;
  description: string;
  useCase: string;
  tags: Record<string, any>;
  source: string;
  collectedAt: string;
}

export interface ApiGateway {
  apiId: string;
  apiName: string;
  region: string;
  protocolType: string;
  endpointType: string;
  description: string;
  useCase: string;
  status: string;
  apiKeySource: string;
  routeSelectionExpression: string;
  authTypes: Record<string, any>;
  corsConfiguration: Record<string, any>;
  stages: Record<string, any>;
  tags: Record<string, any>;
  source: string;
  collectedAt: string;
}

export interface SqsQueue {
  queueUrl: string;
  queueName: string;
  region: string;
  fifoQueue: boolean;
  delaySeconds: number;
  maxMessageSize: number;
  messageRetentionSeconds: number;
  visibilityTimeout: number;
  receiveWaitTimeSeconds: number;
  contentBasedDedup: boolean;
  kmsMasterKeyId: string;
  tags: Record<string, any>;
  source: string;
  collectedAt: string;
}

export interface SnsTopic {
  topicArn: string;
  topicName: string;
  region: string;
  displayName: string;
  fifoTopic: boolean;
  contentBasedDedup: boolean;
  kmsMasterKeyId: string;
  subscriptionsConfirmed: number;
  subscriptionsPending: number;
  subscriptionsDeleted: number;
  tags: Record<string, any>;
  source: string;
  collectedAt: string;
}

export interface KinesisStream {
  streamArn: string;
  streamName: string;
  region: string;
  streamStatus: string;
  streamMode: string;
  shardCount: number;
  retentionPeriodHours: number;
  encryptionType: string;
  kmsKeyId: string;
  hasEnhancedMonitoring: boolean;
  tags: Record<string, any>;
  source: string;
  collectedAt: string;
}

export interface CloudWatchAlarm {
  alarmArn: string;
  alarmName: string;
  region: string;
  metricName: string;
  namespace: string;
  statistic: string;
  period: number;
  evaluationPeriods: number;
  threshold: number;
  comparisonOperator: string;
  alarmDescription: string;
  tags: Record<string, any>;
  source: string;
  collectedAt: string;
}

export interface XRaySamplingRule {
  ruleArn: string;
  ruleName: string;
  region: string;
  priority: number;
  fixedRate: number;
  reservoirSize: number;
  serviceName: string;
  serviceType: string;
  host: string;
  httpMethod: string;
  urlPath: string;
  resourceArn: string;
  version: number;
  tags: Record<string, any>;
  source: string;
  collectedAt: string;
}

export interface CloudTrailTrail {
  trailArn: string;
  trailName: string;
  region: string;
  s3BucketName: string;
  s3KeyPrefix: string;
  isMultiRegion: boolean;
  logFileValidation: boolean;
  includeGlobalEvents: boolean;
  enableLogging: boolean;
  kmsKeyId: string;
  snsTopicArn: string;
  cloudWatchLogsGroup: string;
  tags: Record<string, any>;
  source: string;
  collectedAt: string;
}

export interface EcrRepositoryCatalog {
  repositoryArn: string;
  repositoryName: string;
  region: string;
  registryId: string;
  repositoryUri: string;
  imageTagMutability: string;
  scanOnPush: boolean;
  encryptionType: string;
  tags: Record<string, any>;
  source: string;
  collectedAt: string;
}

export interface EcsClusterCatalog {
  clusterArn: string;
  clusterName: string;
  region: string;
  status: string;
  containerInsightsEnabled: boolean;
  registeredContainerInstances: number;
  activeServicesCount: number;
  runningTasksCount: number;
  tags: Record<string, any>;
  source: string;
  collectedAt: string;
}

export interface EcsTaskDefinitionCatalog {
  taskDefinitionArn: string;
  family: string;
  region: string;
  revision: number;
  cpu: string;
  memory: string;
  networkMode: string;
  executionRoleArn: string;
  taskRoleArn: string;
  tags: Record<string, any>;
  source: string;
  collectedAt: string;
}

export type CatalogItem = 
  | Ec2Instance 
  | EbsVolume 
  | S3Bucket 
  | Vpc 
  | Subnet 
  | InternetGateway 
  | Alb 
  | AlbListener 
  | AlbTargetGroup
  | LambdaFunction
  | DynamoDbTable
  | ApiGateway
  | SqsQueue
  | SnsTopic
  | KinesisStream
  | CloudWatchAlarm
  | XRaySamplingRule
  | CloudTrailTrail
  | EcrRepositoryCatalog
  | EcsClusterCatalog
  | EcsTaskDefinitionCatalog;

/**
 * ConfigService interface for catalog API operations.
 */
export interface IConfigService {
  getEc2Instances(region?: string, amiId?: string): Promise<Ec2Instance[]>;
  getEbsVolumes(instanceId?: string, region?: string): Promise<EbsVolume[]>;
  getS3Buckets(region?: string): Promise<S3Bucket[]>;
  getVpcs(region?: string): Promise<Vpc[]>;
  getSubnets(vpcId?: string, region?: string): Promise<Subnet[]>;
  getInternetGateways(vpcId?: string, region?: string): Promise<InternetGateway[]>;
  getAlbs(vpcId?: string, region?: string): Promise<Alb[]>;
  getAlbListeners(albArn?: string): Promise<AlbListener[]>;
  getAlbTargetGroups(vpcId?: string): Promise<AlbTargetGroup[]>;
  getLambdaFunctions(region?: string): Promise<LambdaFunction[]>;
  getDynamoDbTables(region?: string): Promise<DynamoDbTable[]>;
  getApiGateways(region?: string): Promise<ApiGateway[]>;
  getSqsQueues(region?: string): Promise<SqsQueue[]>;
  getSnsTopics(region?: string): Promise<SnsTopic[]>;
  getKinesisStreams(region?: string): Promise<KinesisStream[]>;
  getCloudWatchAlarms(region?: string): Promise<CloudWatchAlarm[]>;
  getXRaySamplingRules(region?: string): Promise<XRaySamplingRule[]>;
  getCloudTrailTrails(region?: string): Promise<CloudTrailTrail[]>;
  getEcrRepositories(region?: string): Promise<EcrRepositoryCatalog[]>;
  getEcsClusters(region?: string): Promise<EcsClusterCatalog[]>;
  getEcsTaskDefinitions(region?: string): Promise<EcsTaskDefinitionCatalog[]>;
}

/**
 * ConfigService implementation for fetching AWS catalog data from backend API.
 */
class ConfigService implements IConfigService {
  private baseUrl = '/api/config';

  /**
   * Fetch EC2 instances with optional region and AMI ID filters.
   */
  async getEc2Instances(region?: string, amiId?: string): Promise<Ec2Instance[]> {
    const params = new URLSearchParams();
    if (region) params.append('region', region);
    if (amiId) params.append('amiId', amiId);
    
    const response = await fetch(`${this.baseUrl}/ec2-instances?${params}`);
    if (!response.ok) {
      throw new Error(`Failed to fetch EC2 instances: ${response.statusText}`);
    }
    
    return response.json();
  }

  /**
   * Fetch EBS volumes with optional instance ID and region filters.
   */
  async getEbsVolumes(instanceId?: string, region?: string): Promise<EbsVolume[]> {
    const params = new URLSearchParams();
    if (instanceId) params.append('instanceId', instanceId);
    if (region) params.append('region', region);
    
    const response = await fetch(`${this.baseUrl}/ebs-volumes?${params}`);
    if (!response.ok) {
      throw new Error(`Failed to fetch EBS volumes: ${response.statusText}`);
    }
    
    return response.json();
  }

  /**
   * Fetch S3 buckets with optional region filter.
   */
  async getS3Buckets(region?: string): Promise<S3Bucket[]> {
    const params = new URLSearchParams();
    if (region) params.append('region', region);
    
    const response = await fetch(`${this.baseUrl}/s3-buckets?${params}`);
    if (!response.ok) {
      throw new Error(`Failed to fetch S3 buckets: ${response.statusText}`);
    }
    
    return response.json();
  }

  /**
   * Fetch VPCs with optional region filter.
   */
  async getVpcs(region?: string): Promise<Vpc[]> {
    const params = new URLSearchParams();
    if (region) params.append('region', region);
    
    const response = await fetch(`${this.baseUrl}/vpcs?${params}`);
    if (!response.ok) {
      throw new Error(`Failed to fetch VPCs: ${response.statusText}`);
    }
    
    return response.json();
  }

  /**
   * Fetch subnets with optional VPC ID and region filters.
   */
  async getSubnets(vpcId?: string, region?: string): Promise<Subnet[]> {
    const params = new URLSearchParams();
    if (vpcId) params.append('vpcId', vpcId);
    if (region) params.append('region', region);
    
    const response = await fetch(`${this.baseUrl}/subnets?${params}`);
    if (!response.ok) {
      throw new Error(`Failed to fetch subnets: ${response.statusText}`);
    }
    
    return response.json();
  }

  /**
   * Fetch internet gateways with optional VPC ID and region filters.
   */
  async getInternetGateways(vpcId?: string, region?: string): Promise<InternetGateway[]> {
    const params = new URLSearchParams();
    if (vpcId) params.append('vpcId', vpcId);
    if (region) params.append('region', region);
    
    const response = await fetch(`${this.baseUrl}/internet-gateways?${params}`);
    if (!response.ok) {
      throw new Error(`Failed to fetch internet gateways: ${response.statusText}`);
    }
    
    return response.json();
  }

  /**
   * Fetch ALBs with optional VPC ID and region filters.
   */
  async getAlbs(vpcId?: string, region?: string): Promise<Alb[]> {
    const params = new URLSearchParams();
    if (vpcId) params.append('vpcId', vpcId);
    if (region) params.append('region', region);
    
    const response = await fetch(`${this.baseUrl}/albs?${params}`);
    if (!response.ok) {
      throw new Error(`Failed to fetch ALBs: ${response.statusText}`);
    }
    
    return response.json();
  }

  /**
   * Fetch ALB listeners with optional ALB ARN filter.
   */
  async getAlbListeners(albArn?: string): Promise<AlbListener[]> {
    const params = new URLSearchParams();
    if (albArn) params.append('albArn', albArn);
    
    const response = await fetch(`${this.baseUrl}/alb-listeners?${params}`);
    if (!response.ok) {
      throw new Error(`Failed to fetch ALB listeners: ${response.statusText}`);
    }
    
    return response.json();
  }

  /**
   * Fetch ALB target groups with optional VPC ID filter.
   */
  async getAlbTargetGroups(vpcId?: string): Promise<AlbTargetGroup[]> {
    const params = new URLSearchParams();
    if (vpcId) params.append('vpcId', vpcId);
    
    const response = await fetch(`${this.baseUrl}/alb-target-groups?${params}`);
    if (!response.ok) {
      throw new Error(`Failed to fetch ALB target groups: ${response.statusText}`);
    }
    
    return response.json();
  }

  /**
   * Fetch Lambda functions with optional region filter.
   */
  async getLambdaFunctions(region?: string): Promise<LambdaFunction[]> {
    const params = new URLSearchParams();
    if (region) params.append('region', region);
    
    const response = await fetch(`${this.baseUrl}/lambda-functions?${params}`);
    if (!response.ok) {
      throw new Error(`Failed to fetch Lambda functions: ${response.statusText}`);
    }
    
    return response.json();
  }

  /**
   * Fetch DynamoDB tables with optional region filter.
   */
  async getDynamoDbTables(region?: string): Promise<DynamoDbTable[]> {
    const params = new URLSearchParams();
    if (region) params.append('region', region);
    
    const response = await fetch(`${this.baseUrl}/dynamodb-tables?${params}`);
    if (!response.ok) {
      throw new Error(`Failed to fetch DynamoDB tables: ${response.statusText}`);
    }
    
    return response.json();
  }

  /**
   * Fetch API Gateways with optional region filter.
   */
  async getApiGateways(region?: string): Promise<ApiGateway[]> {
    const params = new URLSearchParams();
    if (region) params.append('region', region);
    
    const response = await fetch(`${this.baseUrl}/api-gateways?${params}`);
    if (!response.ok) {
      throw new Error(`Failed to fetch API Gateways: ${response.statusText}`);
    }
    
    return response.json();
  }

  /**
   * Fetch SQS queues with optional region filter.
   */
  async getSqsQueues(region?: string): Promise<SqsQueue[]> {
    const params = new URLSearchParams();
    if (region) params.append('region', region);
    
    const response = await fetch(`${this.baseUrl}/sqs-queues?${params}`);
    if (!response.ok) {
      throw new Error(`Failed to fetch SQS queues: ${response.statusText}`);
    }
    
    return response.json();
  }

  /**
   * Fetch SNS topics with optional region filter.
   */
  async getSnsTopics(region?: string): Promise<SnsTopic[]> {
    const params = new URLSearchParams();
    if (region) params.append('region', region);
    
    const response = await fetch(`${this.baseUrl}/sns-topics?${params}`);
    if (!response.ok) {
      throw new Error(`Failed to fetch SNS topics: ${response.statusText}`);
    }
    
    return response.json();
  }

  /**
   * Fetch Kinesis streams with optional region filter.
   */
  async getKinesisStreams(region?: string): Promise<KinesisStream[]> {
    const params = new URLSearchParams();
    if (region) params.append('region', region);
    
    const response = await fetch(`${this.baseUrl}/kinesis-streams?${params}`);
    if (!response.ok) {
      throw new Error(`Failed to fetch Kinesis streams: ${response.statusText}`);
    }
    
    return response.json();
  }

  /**
   * Fetch CloudWatch alarms with optional region filter.
   */
  async getCloudWatchAlarms(region?: string): Promise<CloudWatchAlarm[]> {
    const params = new URLSearchParams();
    if (region) params.append('region', region);

    const response = await fetch(`${this.baseUrl}/cloudwatch-alarms?${params}`);
    if (!response.ok) {
      throw new Error(`Failed to fetch CloudWatch alarms: ${response.statusText}`);
    }

    return response.json();
  }

  /**
   * Fetch X-Ray sampling rules with optional region filter.
   */
  async getXRaySamplingRules(region?: string): Promise<XRaySamplingRule[]> {
    const params = new URLSearchParams();
    if (region) params.append('region', region);

    const response = await fetch(`${this.baseUrl}/xray-sampling-rules?${params}`);
    if (!response.ok) {
      throw new Error(`Failed to fetch X-Ray sampling rules: ${response.statusText}`);
    }

    return response.json();
  }

  /**
   * Fetch CloudTrail trails with optional region filter.
   */
  async getCloudTrailTrails(region?: string): Promise<CloudTrailTrail[]> {
    const params = new URLSearchParams();
    if (region) params.append('region', region);

    const response = await fetch(`${this.baseUrl}/cloudtrail-trails?${params}`);
    if (!response.ok) {
      throw new Error(`Failed to fetch CloudTrail trails: ${response.statusText}`);
    }

    return response.json();
  }

  /**
   * Fetch ECR repositories with optional region filter.
   */
  async getEcrRepositories(region?: string): Promise<EcrRepositoryCatalog[]> {
    const params = new URLSearchParams();
    if (region) params.append('region', region);
    
    const response = await fetch(`${this.baseUrl}/ecr-repositories?${params}`);
    if (!response.ok) {
      throw new Error(`Failed to fetch ECR repositories: ${response.statusText}`);
    }
    
    return response.json();
  }

  /**
   * Fetch ECS clusters with optional region filter.
   */
  async getEcsClusters(region?: string): Promise<EcsClusterCatalog[]> {
    const params = new URLSearchParams();
    if (region) params.append('region', region);
    
    const response = await fetch(`${this.baseUrl}/ecs-clusters?${params}`);
    if (!response.ok) {
      throw new Error(`Failed to fetch ECS clusters: ${response.statusText}`);
    }
    
    return response.json();
  }

  /**
   * Fetch ECS task definitions with optional region filter.
   */
  async getEcsTaskDefinitions(region?: string): Promise<EcsTaskDefinitionCatalog[]> {
    const params = new URLSearchParams();
    if (region) params.append('region', region);
    
    const response = await fetch(`${this.baseUrl}/ecs-task-definitions?${params}`);
    if (!response.ok) {
      throw new Error(`Failed to fetch ECS task definitions: ${response.statusText}`);
    }
    
    return response.json();
  }
}

// Export singleton instance
export const configService = new ConfigService();

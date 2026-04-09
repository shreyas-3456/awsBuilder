import { ResourceProperties } from './diagram';

export interface VpcProperties extends ResourceProperties {
  cidr_block: string;
  enable_dns_hostnames?: boolean;
  enable_dns_support?: boolean;
  instance_tenancy?: 'default' | 'dedicated';
  region?: string;
  tags?: string;
}

export interface SubnetProperties extends ResourceProperties {
  vpc_id?: string;
  cidr_block: string;
  availability_zone: string;
  map_public_ip_on_launch?: boolean;
  region?: string;
  tags?: string;
}

export interface Ec2Properties extends ResourceProperties {
  ami: string;
  instance_type: string;
  key_name?: string;
  subnet_id?: string;
  associate_public_ip_address?: boolean;
  user_data?: string;
  region?: string;
  tags?: string;
}

export interface S3Properties extends ResourceProperties {
  bucket_name: string;
  acl?: 'private' | 'public-read' | 'public-read-write' | 'authenticated-read';
  versioning?: 'Enabled' | 'Disabled' | 'Suspended';
  force_destroy?: boolean;
  region?: string;
  tags?: string;
}

export interface RdsProperties extends ResourceProperties {
  engine: 'mysql' | 'postgres' | 'mariadb' | 'oracle-ee' | 'sqlserver-ex';
  engine_version?: string;
  instance_class: string;
  allocated_storage: string;
  storage_type?: 'standard' | 'gp2' | 'io1';
  db_name: string;
  username: string;
  password: string;
  publicly_accessible?: boolean;
  skip_final_snapshot?: boolean;
  region?: string;
  tags?: string;
}

export interface InternetGatewayProperties extends ResourceProperties {
  vpc_id?: string;
  region?: string;
  tags?: string;
}

export interface LoadBalancerProperties extends ResourceProperties {
  name?: string;
  load_balancer_type: 'application' | 'network' | 'gateway';
  internal?: boolean;
  security_groups?: string;
  subnets?: string;
  enable_deletion_protection?: boolean;
  region?: string;
  tags?: string;
}

export interface LambdaProperties extends ResourceProperties {
  function_name: string;
  runtime: string;
  handler: string;
  region?: string;
  tags?: string;
}

export interface DynamoDbProperties extends ResourceProperties {
  table_name: string;
  billing_mode: 'PAY_PER_REQUEST' | 'PROVISIONED';
  hash_key: string;
  region?: string;
  tags?: string;
}

export interface ApiGatewayProperties extends ResourceProperties {
  name: string;
  region?: string;
  tags?: string;
}

export interface SqsProperties extends ResourceProperties {
  queue_name: string;
  delay_seconds?: string;
  max_message_size?: string;
  message_retention_seconds?: string;
  visibility_timeout?: string;
  fifo_queue?: string;
  region?: string;
  tags?: string;
}

export interface SnsProperties extends ResourceProperties {
  topic_name: string;
  display_name?: string;
  fifo_topic?: string;
  region?: string;
  tags?: string;
}

export interface KinesisProperties extends ResourceProperties {
  stream_name: string;
  shard_count: string;
  retention_period?: string;
  stream_mode?: string;
  region?: string;
  tags?: string;
}

export interface CloudWatchProperties extends ResourceProperties {
  alarm_name: string;
  metric_name: string;
  namespace?: string;
  statistic?: string;
  period?: string;
  evaluation_periods?: string;
  threshold?: string;
  comparison_operator?: string;
  region?: string;
  tags?: string;
}

export interface XRayProperties extends ResourceProperties {
  rule_name: string;
  priority: string;
  fixed_rate: string;
  reservoir_size?: string;
  service_name?: string;
  service_type?: string;
  host?: string;
  http_method?: string;
  url_path?: string;
  region?: string;
  tags?: string;
}

export interface CloudTrailProperties extends ResourceProperties {
  trail_name: string;
  s3_bucket_name: string;
  is_multi_region_trail?: string;
  enable_log_file_validation?: string;
  include_global_service_events?: string;
  enable_logging?: string;
  region?: string;
  tags?: string;
}

export interface EcrProperties extends ResourceProperties {
  repository_name: string;
  image_tag_mutability?: string;
  scan_on_push?: string;
  encryption_type?: string;
  force_delete?: string;
  region?: string;
  tags?: string;
}

export interface EcsProperties extends ResourceProperties {
  cluster_name: string;
  container_insights?: string;
  region?: string;
  tags?: string;
}

export interface FargateProperties extends ResourceProperties {
  family: string;
  cpu: string;
  memory: string;
  container_name: string;
  container_image: string;
  container_port?: string;
  region?: string;
  tags?: string;
}

export interface ElasticacheProperties extends ResourceProperties {
  cluster_id: string;
  engine: 'redis' | 'memcached';
  node_type: string;
  num_cache_nodes: string;
  engine_version?: string;
  port?: string;
  region?: string;
  tags?: string;
}

export interface EventbridgeProperties extends ResourceProperties {
  rule_name: string;
  event_bus_name?: string;
  description?: string;
  schedule_expression?: string;
  event_pattern?: string;
  state?: string;
  region?: string;
  tags?: string;
}

export const DEFAULT_PROPERTIES: Record<string, ResourceProperties> = {
  VPC: {
    cidr_block: '10.0.0.0/16',
    enable_dns_hostnames: true,
    enable_dns_support: true,
    instance_tenancy: 'default',
    tags: 'Name=my-vpc'
  },
  SUBNET: {
    cidr_block: '10.0.1.0/24',
    map_public_ip_on_launch: false,
    tags: 'Name=my-subnet'
  },
  EC2: {
    ami: 'ami-0c55b159cbfafe1f0',
    instance_type: 't2.micro',
    associate_public_ip_address: true,
    tags: 'Name=my-instance'
  },
  S3: {
    bucket_name: 'my-terraform-bucket-' + Math.floor(Math.random() * 10000),
    acl: 'private',
    versioning: 'Disabled',
    force_destroy: false,
    tags: 'Environment=Dev'
  },
  RDS: {
    engine: 'postgres',
    engine_version: '15.3',
    instance_class: 'db.t3.micro',
    allocated_storage: '20',
    storage_type: 'gp2',
    db_name: 'mydb',
    username: 'admin',
    password: 'password123',
    publicly_accessible: false,
    skip_final_snapshot: true,
    tags: 'Name=my-db'
  },
  INTERNET_GATEWAY: {
    tags: 'Name=my-igw'
  },
  LOAD_BALANCER: {
    name: 'my-alb',
    load_balancer_type: 'application',
    internal: false,
    enable_deletion_protection: false,
    tags: 'Name=my-alb'
  },
  LAMBDA: {
    function_name: 'my-function',
    runtime: 'nodejs18.x',
    handler: 'index.handler',
    tags: 'Name=my-function'
  },
  DYNAMODB: {
    table_name: 'my-table',
    billing_mode: 'PAY_PER_REQUEST',
    hash_key: 'id',
    tags: 'Name=my-table'
  },
  API_GATEWAY: {
    name: 'my-api',
    tags: 'Name=my-api'
  },
  SQS: {
    queue_name: 'my-queue',
    delay_seconds: '0',
    visibility_timeout: '30',
    message_retention_seconds: '345600',
    fifo_queue: 'false',
    tags: 'Name=my-queue'
  },
  SNS: {
    topic_name: 'my-topic',
    display_name: 'My Topic',
    fifo_topic: 'false',
    tags: 'Name=my-topic'
  },
  KINESIS: {
    stream_name: 'my-stream',
    shard_count: '1',
    retention_period: '24',
    stream_mode: 'PROVISIONED',
    tags: 'Name=my-stream'
  },
  CLOUDWATCH: {
    alarm_name: 'my-alarm',
    metric_name: 'CPUUtilization',
    namespace: 'AWS/EC2',
    statistic: 'Average',
    period: '300',
    evaluation_periods: '2',
    threshold: '80',
    comparison_operator: 'GreaterThanThreshold',
    tags: 'Name=my-alarm'
  },
  XRAY: {
    rule_name: 'my-sampling-rule',
    priority: '1000',
    fixed_rate: '0.05',
    reservoir_size: '1',
    service_name: '*',
    service_type: '*',
    host: '*',
    http_method: '*',
    url_path: '*',
    tags: 'Name=my-sampling-rule'
  },
  CLOUDTRAIL: {
    trail_name: 'my-trail',
    s3_bucket_name: 'my-cloudtrail-logs',
    is_multi_region_trail: 'true',
    enable_log_file_validation: 'true',
    include_global_service_events: 'true',
    enable_logging: 'true',
    tags: 'Name=my-trail'
  },
  ECR: {
    repository_name: 'my-repo',
    image_tag_mutability: 'MUTABLE',
    scan_on_push: 'true',
    encryption_type: 'AES256',
    force_delete: 'false',
    tags: 'Name=my-repo'
  },
  ECS: {
    cluster_name: 'my-cluster',
    container_insights: 'enabled',
    tags: 'Name=my-cluster'
  },
  FARGATE: {
    family: 'my-task',
    cpu: '256',
    memory: '512',
    container_name: 'app',
    container_image: 'nginx:latest',
    container_port: '80',
    tags: 'Name=my-task'
  },
  ELASTICACHE: {
    cluster_id: 'my-cache-cluster',
    engine: 'redis',
    node_type: 'cache.t3.micro',
    num_cache_nodes: '1',
    engine_version: '7.0',
    port: '6379',
    tags: 'Name=my-cache-cluster'
  },
  EVENTBRIDGE: {
    rule_name: 'my-event-rule',
    event_bus_name: 'default',
    description: 'My EventBridge rule',
    schedule_expression: 'rate(1 hour)',
    state: 'ENABLED',
    tags: 'Name=my-event-rule'
  }
};

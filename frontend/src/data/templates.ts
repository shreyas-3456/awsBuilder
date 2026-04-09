import { Node, Edge } from 'reactflow';
import { ResourceType } from '../types/diagram';
import { DEFAULT_PROPERTIES } from '../types/resources';

export interface Template {
  id: string;
  name: string;
  description: string;
  nodes: Node[];
  edges: Edge[];
}

export const TEMPLATES: Template[] = [
  {
    id: 'starter-vpc',
    name: 'Starter VPC',
    description: 'Basic networking setup with a single public subnet and Internet Gateway.',
    nodes: [
      {
        id: 'vpc-1',
        type: 'resourceNode',
        position: { x: 300, y: 100 },
        data: { label: 'VPC', type: 'VPC' as ResourceType, properties: { ...DEFAULT_PROPERTIES['VPC'] } }
      },
      {
        id: 'igw-1',
        type: 'resourceNode',
        position: { x: 300, y: 0 },
        data: { label: 'IGW', type: 'INTERNET_GATEWAY' as ResourceType, properties: { ...DEFAULT_PROPERTIES['INTERNET_GATEWAY'] } }
      },
      {
        id: 'subnet-1',
        type: 'resourceNode',
        position: { x: 300, y: 250 },
        data: { label: 'Public Subnet', type: 'SUBNET' as ResourceType, properties: { ...DEFAULT_PROPERTIES['SUBNET'], map_public_ip_on_launch: true } }
      }
    ],
    edges: [
      { id: 'e-igw-vpc', source: 'igw-1', target: 'vpc-1', animated: true, type: 'smoothstep' },
      { id: 'e-sub-vpc', source: 'subnet-1', target: 'vpc-1', animated: true, type: 'smoothstep' }
    ]
  },
  {
    id: 'web-app',
    name: 'Single Instance Web App',
    description: 'Public EC2 instance inside a VPC with internet access.',
    nodes: [
      {
        id: 'vpc-1',
        type: 'resourceNode',
        position: { x: 400, y: 100 },
        data: { label: 'VPC', type: 'VPC' as ResourceType, properties: { ...DEFAULT_PROPERTIES['VPC'] } }
      },
      {
        id: 'igw-1',
        type: 'resourceNode',
        position: { x: 400, y: 0 },
        data: { label: 'IGW', type: 'INTERNET_GATEWAY' as ResourceType, properties: { ...DEFAULT_PROPERTIES['INTERNET_GATEWAY'] } }
      },
      {
        id: 'subnet-1',
        type: 'resourceNode',
        position: { x: 400, y: 250 },
        data: { label: 'Public Subnet', type: 'SUBNET' as ResourceType, properties: { ...DEFAULT_PROPERTIES['SUBNET'], map_public_ip_on_launch: true } }
      },
      {
        id: 'ec2-1',
        type: 'resourceNode',
        position: { x: 400, y: 400 },
        data: { label: 'Web Server', type: 'EC2' as ResourceType, properties: { ...DEFAULT_PROPERTIES['EC2'], tags: 'Name=web-server' } }
      }
    ],
    edges: [
      { id: 'e-igw-vpc', source: 'igw-1', target: 'vpc-1', animated: true, type: 'smoothstep' },
      { id: 'e-sub-vpc', source: 'subnet-1', target: 'vpc-1', animated: true, type: 'smoothstep' },
      { id: 'e-ec2-sub', source: 'ec2-1', target: 'subnet-1', animated: true, type: 'smoothstep' }
    ]
  },
  {
    id: 'ha-cluster',
    name: 'High-Availability Cluster',
    description: 'Load Balanced EC2 instances across multiple subnets.',
    nodes: [
      {
        id: 'vpc-1',
        type: 'resourceNode',
        position: { x: 500, y: 100 },
        data: { label: 'VPC', type: 'VPC' as ResourceType, properties: { ...DEFAULT_PROPERTIES['VPC'] } }
      },
      {
        id: 'igw-1',
        type: 'resourceNode',
        position: { x: 500, y: 0 },
        data: { label: 'IGW', type: 'INTERNET_GATEWAY' as ResourceType, properties: { ...DEFAULT_PROPERTIES['INTERNET_GATEWAY'] } }
      },
      {
        id: 'alb-1',
        type: 'resourceNode',
        position: { x: 500, y: 250 },
        data: { label: 'Public ALB', type: 'LOAD_BALANCER' as ResourceType, properties: { ...DEFAULT_PROPERTIES['LOAD_BALANCER'] } }
      },
      {
        id: 'sub-1',
        type: 'resourceNode',
        position: { x: 300, y: 400 },
        data: { label: 'Subnet A', type: 'SUBNET' as ResourceType, properties: { ...DEFAULT_PROPERTIES['SUBNET'], availability_zone: 'us-east-1a' } }
      },
      {
        id: 'sub-2',
        type: 'resourceNode',
        position: { x: 700, y: 400 },
        data: { label: 'Subnet B', type: 'SUBNET' as ResourceType, properties: { ...DEFAULT_PROPERTIES['SUBNET'], availability_zone: 'us-east-1b', cidr_block: '10.0.2.0/24' } }
      },
      {
        id: 'ec2-1',
        type: 'resourceNode',
        position: { x: 300, y: 550 },
        data: { label: 'Web Node 1', type: 'EC2' as ResourceType, properties: { ...DEFAULT_PROPERTIES['EC2'] } }
      },
      {
        id: 'ec2-2',
        type: 'resourceNode',
        position: { x: 700, y: 550 },
        data: { label: 'Web Node 2', type: 'EC2' as ResourceType, properties: { ...DEFAULT_PROPERTIES['EC2'] } }
      }
    ],
    edges: [
      { id: 'e-igw-vpc', source: 'igw-1', target: 'vpc-1', animated: true, type: 'smoothstep' },
      { id: 'e-alb-sub1', source: 'alb-1', target: 'sub-1', animated: true, type: 'smoothstep' },
      { id: 'e-sub1-vpc', source: 'sub-1', target: 'vpc-1', animated: true, type: 'smoothstep' },
      { id: 'e-sub2-vpc', source: 'sub-2', target: 'vpc-1', animated: true, type: 'smoothstep' },
      { id: 'e-ec21-sub1', source: 'ec2-1', target: 'sub-1', animated: true, type: 'smoothstep' },
      { id: 'e-ec22-sub2', source: 'ec2-2', target: 'sub-2', animated: true, type: 'smoothstep' },
      { id: 'e-alb-ec21', source: 'alb-1', target: 'ec2-1', animated: true, type: 'smoothstep' },
      { id: 'e-alb-ec22', source: 'alb-1', target: 'ec2-2', animated: true, type: 'smoothstep' }
    ]
  },
  {
    id: 'full-stack',
    name: 'Full Stack App',
    description: 'Load Balancer + App Server + Managed Database (RDS) with ElastiCache for low-latency reads.',
    nodes: [
      {
        id: 'vpc-1',
        type: 'resourceNode',
        position: { x: 500, y: 100 },
        data: { label: 'VPC', type: 'VPC' as ResourceType, properties: { ...DEFAULT_PROPERTIES['VPC'] } }
      },
      {
        id: 'alb-1',
        type: 'resourceNode',
        position: { x: 500, y: 250 },
        data: { label: 'ALB', type: 'LOAD_BALANCER' as ResourceType, properties: { ...DEFAULT_PROPERTIES['LOAD_BALANCER'] } }
      },
      {
        id: 'sub-pub',
        type: 'resourceNode',
        position: { x: 300, y: 350 },
        data: { label: 'Public Subnet', type: 'SUBNET' as ResourceType, properties: { ...DEFAULT_PROPERTIES['SUBNET'] } }
      },
      {
        id: 'sub-priv',
        type: 'resourceNode',
        position: { x: 700, y: 350 },
        data: { label: 'Private Subnet', type: 'SUBNET' as ResourceType, properties: { ...DEFAULT_PROPERTIES['SUBNET'], cidr_block: '10.0.10.0/24' } }
      },
      {
        id: 'ec2-1',
        type: 'resourceNode',
        position: { x: 300, y: 500 },
        data: { label: 'App Server', type: 'EC2' as ResourceType, properties: { ...DEFAULT_PROPERTIES['EC2'] } }
      },
      {
        id: 'rds-1',
        type: 'resourceNode',
        position: { x: 700, y: 500 },
        data: { label: 'Database', type: 'RDS' as ResourceType, properties: { ...DEFAULT_PROPERTIES['RDS'] } }
      },
      {
        id: 'cache-1',
        type: 'resourceNode',
        position: { x: 700, y: 650 },
        data: {
          label: 'Redis Cache',
          type: 'ELASTICACHE' as ResourceType,
          properties: {
            ...DEFAULT_PROPERTIES['ELASTICACHE'],
            cluster_id: 'full-stack-cache',
            tags: 'Name=full-stack-cache'
          }
        }
      }
    ],
    edges: [
      { id: 'e-alb-subp', source: 'alb-1', target: 'sub-pub', animated: true, type: 'smoothstep' },
      { id: 'e-subp-vpc', source: 'sub-pub', target: 'vpc-1', animated: true, type: 'smoothstep' },
      { id: 'e-subpr-vpc', source: 'sub-priv', target: 'vpc-1', animated: true, type: 'smoothstep' },
      { id: 'e-ec2-subp', source: 'ec2-1', target: 'sub-pub', animated: true, type: 'smoothstep' },
      { id: 'e-rds-subpr', source: 'rds-1', target: 'sub-priv', animated: true, type: 'smoothstep' },
      { id: 'e-cache-subpr', source: 'cache-1', target: 'sub-priv', animated: true, type: 'smoothstep' },
      { id: 'e-alb-ec2', source: 'alb-1', target: 'ec2-1', animated: true, type: 'smoothstep' },
      { id: 'e-ec2-rds', source: 'ec2-1', target: 'rds-1', animated: true, type: 'smoothstep' },
      { id: 'e-ec2-cache', source: 'ec2-1', target: 'cache-1', animated: true, type: 'smoothstep' }
    ]
  },
  {
    id: 'serverless-api',
    name: 'Serverless API',
    description: 'Modern Serverless backend with API Gateway, Lambda, and DynamoDB.',
    nodes: [
      {
        id: 'api-1',
        type: 'resourceNode',
        position: { x: 500, y: 100 },
        data: { label: 'API Gateway', type: 'API_GATEWAY' as ResourceType, properties: { ...DEFAULT_PROPERTIES['API_GATEWAY'] } }
      },
      {
        id: 'lambda-1',
        type: 'resourceNode',
        position: { x: 500, y: 300 },
        data: { label: 'Lambda Function', type: 'LAMBDA' as ResourceType, properties: { ...DEFAULT_PROPERTIES['LAMBDA'] } }
      },
      {
        id: 'db-1',
        type: 'resourceNode',
        position: { x: 500, y: 500 },
        data: { label: 'DynamoDB Table', type: 'DYNAMODB' as ResourceType, properties: { ...DEFAULT_PROPERTIES['DYNAMODB'] } }
      }
    ],
    edges: [
      { id: 'e-api-lambda', source: 'api-1', target: 'lambda-1', animated: true, type: 'smoothstep' },
      { id: 'e-lambda-db', source: 'lambda-1', target: 'db-1', animated: true, type: 'smoothstep' }
    ]
  },
  {
    id: 'secure-bastion',
    name: 'Secure Bastion Setup',
    description: 'Bastion host in public subnet with application server, private database, and ElastiCache in private subnets.',
    nodes: [
      {
        id: 'vpc-1',
        type: 'resourceNode',
        position: { x: 500, y: 100 },
        data: { label: 'VPC', type: 'VPC' as ResourceType, properties: { ...DEFAULT_PROPERTIES['VPC'] } }
      },
      {
        id: 'sub-pub',
        type: 'resourceNode',
        position: { x: 300, y: 250 },
        data: { label: 'Public Subnet', type: 'SUBNET' as ResourceType, properties: { ...DEFAULT_PROPERTIES['SUBNET'], map_public_ip_on_launch: true } }
      },
      {
        id: 'sub-priv',
        type: 'resourceNode',
        position: { x: 700, y: 250 },
        data: { label: 'Private Subnet', type: 'SUBNET' as ResourceType, properties: { ...DEFAULT_PROPERTIES['SUBNET'], cidr_block: '10.0.2.0/24' } }
      },
      {
        id: 'bastion',
        type: 'resourceNode',
        position: { x: 300, y: 400 },
        data: { label: 'Bastion Host', type: 'EC2' as ResourceType, properties: { ...DEFAULT_PROPERTIES['EC2'], tags: 'Name=bastion' } }
      },
      {
        id: 'app-server',
        type: 'resourceNode',
        position: { x: 700, y: 400 },
        data: { label: 'App Server', type: 'EC2' as ResourceType, properties: { ...DEFAULT_PROPERTIES['EC2'], tags: 'Name=app-server' } }
      },
      {
        id: 'rds-1',
        type: 'resourceNode',
        position: { x: 700, y: 550 },
        data: { label: 'Private DB', type: 'RDS' as ResourceType, properties: { ...DEFAULT_PROPERTIES['RDS'] } }
      },
      {
        id: 'cache-1',
        type: 'resourceNode',
        position: { x: 500, y: 550 },
        data: {
          label: 'Session Cache',
          type: 'ELASTICACHE' as ResourceType,
          properties: {
            ...DEFAULT_PROPERTIES['ELASTICACHE'],
            cluster_id: 'secure-session-cache',
            tags: 'Name=secure-session-cache'
          }
        }
      }
    ],
    edges: [
      { id: 'e-subp-vpc', source: 'sub-pub', target: 'vpc-1', animated: true, type: 'smoothstep' },
      { id: 'e-subpr-vpc', source: 'sub-priv', target: 'vpc-1', animated: true, type: 'smoothstep' },
      { id: 'e-bastion-subp', source: 'bastion', target: 'sub-pub', animated: true, type: 'smoothstep' },
      { id: 'e-app-subpr', source: 'app-server', target: 'sub-priv', animated: true, type: 'smoothstep' },
      { id: 'e-cache-subpr', source: 'cache-1', target: 'sub-priv', animated: true, type: 'smoothstep' },
      { id: 'e-rds-subpr', source: 'rds-1', target: 'sub-priv', animated: true, type: 'smoothstep' },
      { id: 'e-app-rds', source: 'app-server', target: 'rds-1', animated: true, type: 'smoothstep' },
      { id: 'e-app-cache', source: 'app-server', target: 'cache-1', animated: true, type: 'smoothstep' }
    ]
  },
  {
    id: 'event-driven-messaging',
    name: 'Event-Driven Messaging',
    description: 'SNS fan-out pattern with multiple SQS queues for decoupled microservices.',
    nodes: [
      {
        id: 'sns-1',
        type: 'resourceNode',
        position: { x: 500, y: 100 },
        data: { label: 'Notification Topic', type: 'SNS' as ResourceType, properties: { ...DEFAULT_PROPERTIES['SNS'], topic_name: 'order-events', display_name: 'Order Events' } }
      },
      {
        id: 'sqs-1',
        type: 'resourceNode',
        position: { x: 250, y: 300 },
        data: { label: 'Email Queue', type: 'SQS' as ResourceType, properties: { ...DEFAULT_PROPERTIES['SQS'], queue_name: 'email-notifications', visibility_timeout: '60' } }
      },
      {
        id: 'sqs-2',
        type: 'resourceNode',
        position: { x: 750, y: 300 },
        data: { label: 'Analytics Queue', type: 'SQS' as ResourceType, properties: { ...DEFAULT_PROPERTIES['SQS'], queue_name: 'analytics-events', visibility_timeout: '120' } }
      },
      {
        id: 'lambda-1',
        type: 'resourceNode',
        position: { x: 250, y: 500 },
        data: { label: 'Email Sender', type: 'LAMBDA' as ResourceType, properties: { ...DEFAULT_PROPERTIES['LAMBDA'], function_name: 'send-email', runtime: 'python3.11' } }
      },
      {
        id: 'lambda-2',
        type: 'resourceNode',
        position: { x: 750, y: 500 },
        data: { label: 'Analytics Processor', type: 'LAMBDA' as ResourceType, properties: { ...DEFAULT_PROPERTIES['LAMBDA'], function_name: 'process-analytics', runtime: 'python3.11' } }
      }
    ],
    edges: [
      { id: 'e-sns-sqs1', source: 'sns-1', target: 'sqs-1', animated: true, type: 'smoothstep' },
      { id: 'e-sns-sqs2', source: 'sns-1', target: 'sqs-2', animated: true, type: 'smoothstep' },
      { id: 'e-lam1-sqs1', source: 'lambda-1', target: 'sqs-1', animated: true, type: 'smoothstep' },
      { id: 'e-lam2-sqs2', source: 'lambda-2', target: 'sqs-2', animated: true, type: 'smoothstep' }
    ]
  },
  {
    id: 'serverless-event-pipeline',
    name: 'Serverless Event Pipeline',
    description: 'API Gateway triggers Lambda which publishes to SNS, fanning out to SQS consumers.',
    nodes: [
      {
        id: 'api-1',
        type: 'resourceNode',
        position: { x: 500, y: 50 },
        data: { label: 'API Gateway', type: 'API_GATEWAY' as ResourceType, properties: { ...DEFAULT_PROPERTIES['API_GATEWAY'], name: 'event-api' } }
      },
      {
        id: 'lambda-1',
        type: 'resourceNode',
        position: { x: 500, y: 200 },
        data: { label: 'Event Producer', type: 'LAMBDA' as ResourceType, properties: { ...DEFAULT_PROPERTIES['LAMBDA'], function_name: 'event-producer', runtime: 'nodejs20.x' } }
      },
      {
        id: 'sns-1',
        type: 'resourceNode',
        position: { x: 500, y: 350 },
        data: { label: 'Events Topic', type: 'SNS' as ResourceType, properties: { ...DEFAULT_PROPERTIES['SNS'], topic_name: 'app-events' } }
      },
      {
        id: 'sqs-1',
        type: 'resourceNode',
        position: { x: 300, y: 500 },
        data: { label: 'Processing Queue', type: 'SQS' as ResourceType, properties: { ...DEFAULT_PROPERTIES['SQS'], queue_name: 'event-processing' } }
      },
      {
        id: 'sqs-2',
        type: 'resourceNode',
        position: { x: 700, y: 500 },
        data: { label: 'Audit Queue', type: 'SQS' as ResourceType, properties: { ...DEFAULT_PROPERTIES['SQS'], queue_name: 'event-audit', message_retention_seconds: '1209600' } }
      },
      {
        id: 'db-1',
        type: 'resourceNode',
        position: { x: 500, y: 650 },
        data: { label: 'Events Store', type: 'DYNAMODB' as ResourceType, properties: { ...DEFAULT_PROPERTIES['DYNAMODB'], table_name: 'event-store' } }
      }
    ],
    edges: [
      { id: 'e-api-lam', source: 'api-1', target: 'lambda-1', animated: true, type: 'smoothstep' },
      { id: 'e-lam-sns', source: 'lambda-1', target: 'sns-1', animated: true, type: 'smoothstep' },
      { id: 'e-sns-sqs1', source: 'sns-1', target: 'sqs-1', animated: true, type: 'smoothstep' },
      { id: 'e-sns-sqs2', source: 'sns-1', target: 'sqs-2', animated: true, type: 'smoothstep' },
      { id: 'e-lam-db', source: 'lambda-1', target: 'db-1', animated: true, type: 'smoothstep' }
    ]
  },
  {
    id: 'realtime-data-pipeline',
    name: 'Real-Time Data Pipeline',
    description: 'Kinesis stream for real-time data ingestion with Lambda processing and DynamoDB storage.',
    nodes: [
      {
        id: 'kinesis-1',
        type: 'resourceNode',
        position: { x: 500, y: 100 },
        data: { label: 'Data Stream', type: 'KINESIS' as ResourceType, properties: { ...DEFAULT_PROPERTIES['KINESIS'], stream_name: 'clickstream-data', shard_count: '2', retention_period: '48' } }
      },
      {
        id: 'lambda-1',
        type: 'resourceNode',
        position: { x: 300, y: 300 },
        data: { label: 'Stream Processor', type: 'LAMBDA' as ResourceType, properties: { ...DEFAULT_PROPERTIES['LAMBDA'], function_name: 'stream-processor', runtime: 'python3.11' } }
      },
      {
        id: 'lambda-2',
        type: 'resourceNode',
        position: { x: 700, y: 300 },
        data: { label: 'Anomaly Detector', type: 'LAMBDA' as ResourceType, properties: { ...DEFAULT_PROPERTIES['LAMBDA'], function_name: 'anomaly-detector', runtime: 'python3.11' } }
      },
      {
        id: 'db-1',
        type: 'resourceNode',
        position: { x: 300, y: 500 },
        data: { label: 'Processed Data', type: 'DYNAMODB' as ResourceType, properties: { ...DEFAULT_PROPERTIES['DYNAMODB'], table_name: 'processed-events' } }
      },
      {
        id: 'sns-1',
        type: 'resourceNode',
        position: { x: 700, y: 500 },
        data: { label: 'Alert Topic', type: 'SNS' as ResourceType, properties: { ...DEFAULT_PROPERTIES['SNS'], topic_name: 'anomaly-alerts', display_name: 'Anomaly Alerts' } }
      }
    ],
    edges: [
      { id: 'e-lam1-kin', source: 'lambda-1', target: 'kinesis-1', animated: true, type: 'smoothstep' },
      { id: 'e-lam2-kin', source: 'lambda-2', target: 'kinesis-1', animated: true, type: 'smoothstep' },
      { id: 'e-lam1-db', source: 'lambda-1', target: 'db-1', animated: true, type: 'smoothstep' },
      { id: 'e-lam2-sns', source: 'lambda-2', target: 'sns-1', animated: true, type: 'smoothstep' }
    ]
  },
  {
    id: 'full-event-architecture',
    name: 'Full Event-Driven Architecture',
    description: 'Complete event-driven system with API Gateway, Lambda, SNS fan-out, SQS queues, Kinesis streaming, and DynamoDB.',
    nodes: [
      {
        id: 'api-1',
        type: 'resourceNode',
        position: { x: 500, y: 50 },
        data: { label: 'API Gateway', type: 'API_GATEWAY' as ResourceType, properties: { ...DEFAULT_PROPERTIES['API_GATEWAY'], name: 'platform-api' } }
      },
      {
        id: 'lambda-api',
        type: 'resourceNode',
        position: { x: 500, y: 200 },
        data: { label: 'API Handler', type: 'LAMBDA' as ResourceType, properties: { ...DEFAULT_PROPERTIES['LAMBDA'], function_name: 'api-handler', runtime: 'nodejs20.x' } }
      },
      {
        id: 'sns-1',
        type: 'resourceNode',
        position: { x: 300, y: 350 },
        data: { label: 'Event Bus', type: 'SNS' as ResourceType, properties: { ...DEFAULT_PROPERTIES['SNS'], topic_name: 'event-bus' } }
      },
      {
        id: 'kinesis-1',
        type: 'resourceNode',
        position: { x: 700, y: 350 },
        data: { label: 'Activity Stream', type: 'KINESIS' as ResourceType, properties: { ...DEFAULT_PROPERTIES['KINESIS'], stream_name: 'activity-stream', shard_count: '2' } }
      },
      {
        id: 'sqs-1',
        type: 'resourceNode',
        position: { x: 150, y: 500 },
        data: { label: 'Order Queue', type: 'SQS' as ResourceType, properties: { ...DEFAULT_PROPERTIES['SQS'], queue_name: 'order-processing' } }
      },
      {
        id: 'sqs-2',
        type: 'resourceNode',
        position: { x: 450, y: 500 },
        data: { label: 'Notification Queue', type: 'SQS' as ResourceType, properties: { ...DEFAULT_PROPERTIES['SQS'], queue_name: 'notifications' } }
      },
      {
        id: 'lambda-stream',
        type: 'resourceNode',
        position: { x: 700, y: 500 },
        data: { label: 'Stream Processor', type: 'LAMBDA' as ResourceType, properties: { ...DEFAULT_PROPERTIES['LAMBDA'], function_name: 'stream-processor', runtime: 'python3.11' } }
      },
      {
        id: 'db-1',
        type: 'resourceNode',
        position: { x: 500, y: 680 },
        data: { label: 'Data Store', type: 'DYNAMODB' as ResourceType, properties: { ...DEFAULT_PROPERTIES['DYNAMODB'], table_name: 'platform-data' } }
      },
      {
        id: 's3-1',
        type: 'resourceNode',
        position: { x: 800, y: 680 },
        data: { label: 'Data Lake', type: 'S3' as ResourceType, properties: { ...DEFAULT_PROPERTIES['S3'], bucket_name: 'activity-data-lake' } }
      }
    ],
    edges: [
      { id: 'e-api-lam', source: 'api-1', target: 'lambda-api', animated: true, type: 'smoothstep' },
      { id: 'e-lam-sns', source: 'lambda-api', target: 'sns-1', animated: true, type: 'smoothstep' },
      { id: 'e-lam-kin', source: 'lambda-api', target: 'kinesis-1', animated: true, type: 'smoothstep' },
      { id: 'e-sns-sqs1', source: 'sns-1', target: 'sqs-1', animated: true, type: 'smoothstep' },
      { id: 'e-sns-sqs2', source: 'sns-1', target: 'sqs-2', animated: true, type: 'smoothstep' },
      { id: 'e-lams-kin', source: 'lambda-stream', target: 'kinesis-1', animated: true, type: 'smoothstep' },
      { id: 'e-lams-db', source: 'lambda-stream', target: 'db-1', animated: true, type: 'smoothstep' },
      { id: 'e-lams-s3', source: 'lambda-stream', target: 's3-1', animated: true, type: 'smoothstep' },
      { id: 'e-lam-db', source: 'lambda-api', target: 'db-1', animated: true, type: 'smoothstep' }
    ]
  },
  {
    id: 'observability-stack',
    name: 'Observability Stack',
    description: 'CloudWatch alarm monitoring Lambda with X-Ray tracing on API Gateway and SNS alerts.',
    nodes: [
      {
        id: 'api-1',
        type: 'resourceNode',
        position: { x: 500, y: 50 },
        data: { label: 'API Gateway', type: 'API_GATEWAY' as ResourceType, properties: { ...DEFAULT_PROPERTIES['API_GATEWAY'], name: 'traced-api' } }
      },
      {
        id: 'xray-1',
        type: 'resourceNode',
        position: { x: 800, y: 50 },
        data: { label: 'API Tracing', type: 'XRAY' as ResourceType, properties: { ...DEFAULT_PROPERTIES['XRAY'], rule_name: 'api-tracing', fixed_rate: '0.1', service_type: 'AWS::ApiGateway::Stage' } }
      },
      {
        id: 'lambda-1',
        type: 'resourceNode',
        position: { x: 500, y: 220 },
        data: { label: 'API Handler', type: 'LAMBDA' as ResourceType, properties: { ...DEFAULT_PROPERTIES['LAMBDA'], function_name: 'api-handler', runtime: 'nodejs20.x' } }
      },
      {
        id: 'xray-2',
        type: 'resourceNode',
        position: { x: 800, y: 220 },
        data: { label: 'Lambda Tracing', type: 'XRAY' as ResourceType, properties: { ...DEFAULT_PROPERTIES['XRAY'], rule_name: 'lambda-tracing', fixed_rate: '0.25', service_type: 'AWS::Lambda::Function' } }
      },
      {
        id: 'cw-1',
        type: 'resourceNode',
        position: { x: 300, y: 400 },
        data: { label: 'Error Alarm', type: 'CLOUDWATCH' as ResourceType, properties: { ...DEFAULT_PROPERTIES['CLOUDWATCH'], alarm_name: 'lambda-errors', metric_name: 'Errors', namespace: 'AWS/Lambda', threshold: '5', comparison_operator: 'GreaterThanThreshold' } }
      },
      {
        id: 'sns-1',
        type: 'resourceNode',
        position: { x: 300, y: 570 },
        data: { label: 'Alert Notifications', type: 'SNS' as ResourceType, properties: { ...DEFAULT_PROPERTIES['SNS'], topic_name: 'ops-alerts', display_name: 'Ops Alerts' } }
      },
      {
        id: 'db-1',
        type: 'resourceNode',
        position: { x: 650, y: 400 },
        data: { label: 'Data Store', type: 'DYNAMODB' as ResourceType, properties: { ...DEFAULT_PROPERTIES['DYNAMODB'], table_name: 'app-data' } }
      }
    ],
    edges: [
      { id: 'e-api-lam', source: 'api-1', target: 'lambda-1', animated: true, type: 'smoothstep' },
      { id: 'e-xray1-api', source: 'xray-1', target: 'api-1', animated: true, type: 'smoothstep' },
      { id: 'e-xray2-lam', source: 'xray-2', target: 'lambda-1', animated: true, type: 'smoothstep' },
      { id: 'e-cw-lam', source: 'cw-1', target: 'lambda-1', animated: true, type: 'smoothstep' },
      { id: 'e-cw-sns', source: 'cw-1', target: 'sns-1', animated: true, type: 'smoothstep' },
      { id: 'e-lam-db', source: 'lambda-1', target: 'db-1', animated: true, type: 'smoothstep' }
    ]
  },
  {
    id: 'compliance-audit',
    name: 'Compliance & Audit Trail',
    description: 'CloudTrail logging to S3 with CloudWatch alarm on API activity and SNS notifications.',
    nodes: [
      {
        id: 'trail-1',
        type: 'resourceNode',
        position: { x: 500, y: 100 },
        data: { label: 'Audit Trail', type: 'CLOUDTRAIL' as ResourceType, properties: { ...DEFAULT_PROPERTIES['CLOUDTRAIL'], trail_name: 'compliance-trail', s3_bucket_name: 'audit-logs-bucket' } }
      },
      {
        id: 's3-1',
        type: 'resourceNode',
        position: { x: 300, y: 300 },
        data: { label: 'Log Storage', type: 'S3' as ResourceType, properties: { ...DEFAULT_PROPERTIES['S3'], bucket_name: 'audit-logs-bucket', versioning: 'Enabled' } }
      },
      {
        id: 'cw-1',
        type: 'resourceNode',
        position: { x: 700, y: 300 },
        data: { label: 'Unauthorized Alarm', type: 'CLOUDWATCH' as ResourceType, properties: { ...DEFAULT_PROPERTIES['CLOUDWATCH'], alarm_name: 'unauthorized-api-calls', metric_name: 'UnauthorizedAttempts', namespace: 'CloudTrail', threshold: '1' } }
      },
      {
        id: 'sns-1',
        type: 'resourceNode',
        position: { x: 500, y: 480 },
        data: { label: 'Security Alerts', type: 'SNS' as ResourceType, properties: { ...DEFAULT_PROPERTIES['SNS'], topic_name: 'security-alerts', display_name: 'Security Alerts' } }
      }
    ],
    edges: [
      { id: 'e-trail-s3', source: 'trail-1', target: 's3-1', animated: true, type: 'smoothstep' },
      { id: 'e-trail-cw', source: 'trail-1', target: 'cw-1', animated: true, type: 'smoothstep' },
      { id: 'e-cw-sns', source: 'cw-1', target: 'sns-1', animated: true, type: 'smoothstep' },
      { id: 'e-trail-sns', source: 'trail-1', target: 'sns-1', animated: true, type: 'smoothstep' }
    ]
  },
  {
    id: 'full-monitoring-pipeline',
    name: 'Full Monitoring Pipeline',
    description: 'API Gateway with X-Ray tracing, Lambda processing, DynamoDB storage, CloudWatch monitoring, CloudTrail auditing, and SNS alerting.',
    nodes: [
      {
        id: 'api-1',
        type: 'resourceNode',
        position: { x: 500, y: 50 },
        data: { label: 'API Gateway', type: 'API_GATEWAY' as ResourceType, properties: { ...DEFAULT_PROPERTIES['API_GATEWAY'], name: 'monitored-api' } }
      },
      {
        id: 'xray-1',
        type: 'resourceNode',
        position: { x: 800, y: 50 },
        data: { label: 'API Tracer', type: 'XRAY' as ResourceType, properties: { ...DEFAULT_PROPERTIES['XRAY'], rule_name: 'api-tracer', fixed_rate: '0.5' } }
      },
      {
        id: 'lambda-1',
        type: 'resourceNode',
        position: { x: 500, y: 200 },
        data: { label: 'Business Logic', type: 'LAMBDA' as ResourceType, properties: { ...DEFAULT_PROPERTIES['LAMBDA'], function_name: 'business-logic', runtime: 'python3.11' } }
      },
      {
        id: 'db-1',
        type: 'resourceNode',
        position: { x: 300, y: 380 },
        data: { label: 'App Data', type: 'DYNAMODB' as ResourceType, properties: { ...DEFAULT_PROPERTIES['DYNAMODB'], table_name: 'app-records' } }
      },
      {
        id: 'cw-1',
        type: 'resourceNode',
        position: { x: 700, y: 380 },
        data: { label: 'Error Monitor', type: 'CLOUDWATCH' as ResourceType, properties: { ...DEFAULT_PROPERTIES['CLOUDWATCH'], alarm_name: 'high-error-rate', metric_name: 'Errors', namespace: 'AWS/Lambda', threshold: '10' } }
      },
      {
        id: 'trail-1',
        type: 'resourceNode',
        position: { x: 200, y: 550 },
        data: { label: 'Audit Trail', type: 'CLOUDTRAIL' as ResourceType, properties: { ...DEFAULT_PROPERTIES['CLOUDTRAIL'], trail_name: 'app-audit', s3_bucket_name: 'app-audit-logs' } }
      },
      {
        id: 's3-1',
        type: 'resourceNode',
        position: { x: 200, y: 720 },
        data: { label: 'Audit Logs', type: 'S3' as ResourceType, properties: { ...DEFAULT_PROPERTIES['S3'], bucket_name: 'app-audit-logs' } }
      },
      {
        id: 'sns-1',
        type: 'resourceNode',
        position: { x: 700, y: 550 },
        data: { label: 'Ops Alerts', type: 'SNS' as ResourceType, properties: { ...DEFAULT_PROPERTIES['SNS'], topic_name: 'ops-notifications' } }
      }
    ],
    edges: [
      { id: 'e-api-lam', source: 'api-1', target: 'lambda-1', animated: true, type: 'smoothstep' },
      { id: 'e-xray-api', source: 'xray-1', target: 'api-1', animated: true, type: 'smoothstep' },
      { id: 'e-lam-db', source: 'lambda-1', target: 'db-1', animated: true, type: 'smoothstep' },
      { id: 'e-cw-lam', source: 'cw-1', target: 'lambda-1', animated: true, type: 'smoothstep' },
      { id: 'e-cw-sns', source: 'cw-1', target: 'sns-1', animated: true, type: 'smoothstep' },
      { id: 'e-trail-s3', source: 'trail-1', target: 's3-1', animated: true, type: 'smoothstep' },
      { id: 'e-trail-cw', source: 'trail-1', target: 'cw-1', animated: true, type: 'smoothstep' }
    ]
  },
  {
    id: 'containerized-microservice',
    name: 'Containerized Microservice',
    description: 'ECR registry with ECS cluster running Fargate tasks behind a Load Balancer in a VPC.',
    nodes: [
      {
        id: 'vpc-1',
        type: 'resourceNode',
        position: { x: 500, y: 50 },
        data: { label: 'VPC', type: 'VPC' as ResourceType, properties: { ...DEFAULT_PROPERTIES['VPC'] } }
      },
      {
        id: 'sub-1',
        type: 'resourceNode',
        position: { x: 300, y: 200 },
        data: { label: 'Subnet A', type: 'SUBNET' as ResourceType, properties: { ...DEFAULT_PROPERTIES['SUBNET'], availability_zone: 'us-east-1a' } }
      },
      {
        id: 'sub-2',
        type: 'resourceNode',
        position: { x: 700, y: 200 },
        data: { label: 'Subnet B', type: 'SUBNET' as ResourceType, properties: { ...DEFAULT_PROPERTIES['SUBNET'], cidr_block: '10.0.2.0/24', availability_zone: 'us-east-1b' } }
      },
      {
        id: 'alb-1',
        type: 'resourceNode',
        position: { x: 500, y: 350 },
        data: { label: 'App ALB', type: 'LOAD_BALANCER' as ResourceType, properties: { ...DEFAULT_PROPERTIES['LOAD_BALANCER'], name: 'app-alb' } }
      },
      {
        id: 'ecr-1',
        type: 'resourceNode',
        position: { x: 150, y: 500 },
        data: { label: 'App Registry', type: 'ECR' as ResourceType, properties: { ...DEFAULT_PROPERTIES['ECR'], repository_name: 'app-service' } }
      },
      {
        id: 'ecs-1',
        type: 'resourceNode',
        position: { x: 500, y: 500 },
        data: { label: 'App Cluster', type: 'ECS' as ResourceType, properties: { ...DEFAULT_PROPERTIES['ECS'], cluster_name: 'app-cluster' } }
      },
      {
        id: 'fargate-1',
        type: 'resourceNode',
        position: { x: 500, y: 670 },
        data: { label: 'App Task', type: 'FARGATE' as ResourceType, properties: { ...DEFAULT_PROPERTIES['FARGATE'], family: 'app-task', container_name: 'app', container_image: '123456789.dkr.ecr.us-east-1.amazonaws.com/app-service:latest', container_port: '8080' } }
      }
    ],
    edges: [
      { id: 'e-sub1-vpc', source: 'sub-1', target: 'vpc-1', animated: true, type: 'smoothstep' },
      { id: 'e-sub2-vpc', source: 'sub-2', target: 'vpc-1', animated: true, type: 'smoothstep' },
      { id: 'e-alb-sub1', source: 'alb-1', target: 'sub-1', animated: true, type: 'smoothstep' },
      { id: 'e-ecs-sub1', source: 'ecs-1', target: 'sub-1', animated: true, type: 'smoothstep' },
      { id: 'e-ecs-ecr', source: 'ecs-1', target: 'ecr-1', animated: true, type: 'smoothstep' },
      { id: 'e-ecs-alb', source: 'ecs-1', target: 'alb-1', animated: true, type: 'smoothstep' },
      { id: 'e-fg-ecs', source: 'fargate-1', target: 'ecs-1', animated: true, type: 'smoothstep' },
      { id: 'e-fg-ecr', source: 'fargate-1', target: 'ecr-1', animated: true, type: 'smoothstep' },
      { id: 'e-fg-alb', source: 'fargate-1', target: 'alb-1', animated: true, type: 'smoothstep' }
    ]
  },
  {
    id: 'full-container-pipeline',
    name: 'Full Container Pipeline',
    description: 'Production container setup: ECR + ECS Fargate behind ALB in VPC with CloudWatch monitoring and SNS alerts.',
    nodes: [
      {
        id: 'vpc-1',
        type: 'resourceNode',
        position: { x: 500, y: 50 },
        data: { label: 'VPC', type: 'VPC' as ResourceType, properties: { ...DEFAULT_PROPERTIES['VPC'] } }
      },
      {
        id: 'igw-1',
        type: 'resourceNode',
        position: { x: 800, y: 50 },
        data: { label: 'IGW', type: 'INTERNET_GATEWAY' as ResourceType, properties: { ...DEFAULT_PROPERTIES['INTERNET_GATEWAY'] } }
      },
      {
        id: 'sub-pub',
        type: 'resourceNode',
        position: { x: 300, y: 200 },
        data: { label: 'Public Subnet', type: 'SUBNET' as ResourceType, properties: { ...DEFAULT_PROPERTIES['SUBNET'], map_public_ip_on_launch: true } }
      },
      {
        id: 'sub-priv',
        type: 'resourceNode',
        position: { x: 700, y: 200 },
        data: { label: 'Private Subnet', type: 'SUBNET' as ResourceType, properties: { ...DEFAULT_PROPERTIES['SUBNET'], cidr_block: '10.0.10.0/24' } }
      },
      {
        id: 'alb-1',
        type: 'resourceNode',
        position: { x: 300, y: 370 },
        data: { label: 'Public ALB', type: 'LOAD_BALANCER' as ResourceType, properties: { ...DEFAULT_PROPERTIES['LOAD_BALANCER'], name: 'prod-alb' } }
      },
      {
        id: 'ecr-1',
        type: 'resourceNode',
        position: { x: 100, y: 520 },
        data: { label: 'Container Registry', type: 'ECR' as ResourceType, properties: { ...DEFAULT_PROPERTIES['ECR'], repository_name: 'prod-api', scan_on_push: 'true', image_tag_mutability: 'IMMUTABLE' } }
      },
      {
        id: 'ecs-1',
        type: 'resourceNode',
        position: { x: 500, y: 520 },
        data: { label: 'Production Cluster', type: 'ECS' as ResourceType, properties: { ...DEFAULT_PROPERTIES['ECS'], cluster_name: 'prod-cluster', container_insights: 'enabled' } }
      },
      {
        id: 'fargate-1',
        type: 'resourceNode',
        position: { x: 500, y: 700 },
        data: { label: 'API Service', type: 'FARGATE' as ResourceType, properties: { ...DEFAULT_PROPERTIES['FARGATE'], family: 'prod-api', cpu: '512', memory: '1024', container_name: 'api', container_image: 'prod-api:latest', container_port: '8080' } }
      },
      {
        id: 'cw-1',
        type: 'resourceNode',
        position: { x: 800, y: 520 },
        data: { label: 'CPU Monitor', type: 'CLOUDWATCH' as ResourceType, properties: { ...DEFAULT_PROPERTIES['CLOUDWATCH'], alarm_name: 'ecs-high-cpu', metric_name: 'CPUUtilization', namespace: 'AWS/ECS', threshold: '80' } }
      },
      {
        id: 'sns-1',
        type: 'resourceNode',
        position: { x: 800, y: 700 },
        data: { label: 'Ops Alerts', type: 'SNS' as ResourceType, properties: { ...DEFAULT_PROPERTIES['SNS'], topic_name: 'ecs-alerts', display_name: 'ECS Alerts' } }
      }
    ],
    edges: [
      { id: 'e-igw-vpc', source: 'igw-1', target: 'vpc-1', animated: true, type: 'smoothstep' },
      { id: 'e-subp-vpc', source: 'sub-pub', target: 'vpc-1', animated: true, type: 'smoothstep' },
      { id: 'e-subpr-vpc', source: 'sub-priv', target: 'vpc-1', animated: true, type: 'smoothstep' },
      { id: 'e-alb-subp', source: 'alb-1', target: 'sub-pub', animated: true, type: 'smoothstep' },
      { id: 'e-ecs-subpr', source: 'ecs-1', target: 'sub-priv', animated: true, type: 'smoothstep' },
      { id: 'e-ecs-ecr', source: 'ecs-1', target: 'ecr-1', animated: true, type: 'smoothstep' },
      { id: 'e-ecs-alb', source: 'ecs-1', target: 'alb-1', animated: true, type: 'smoothstep' },
      { id: 'e-fg-ecs', source: 'fargate-1', target: 'ecs-1', animated: true, type: 'smoothstep' },
      { id: 'e-fg-ecr', source: 'fargate-1', target: 'ecr-1', animated: true, type: 'smoothstep' },
      { id: 'e-fg-alb', source: 'fargate-1', target: 'alb-1', animated: true, type: 'smoothstep' },
      { id: 'e-cw-ecs', source: 'cw-1', target: 'ecs-1', animated: true, type: 'smoothstep' },
      { id: 'e-cw-sns', source: 'cw-1', target: 'sns-1', animated: true, type: 'smoothstep' }
    ]
  },
  {
    id: 'eventbridge-scheduled-automation',
    name: 'Scheduled Automation',
    description: 'EventBridge scheduled rules triggering Lambda functions for automated tasks with SNS notifications.',
    nodes: [
      {
        id: 'eb-1',
        type: 'resourceNode',
        position: { x: 500, y: 50 },
        data: {
          label: 'Hourly Cleanup',
          type: 'EVENTBRIDGE' as ResourceType,
          properties: {
            ...DEFAULT_PROPERTIES['EVENTBRIDGE'],
            rule_name: 'hourly-cleanup',
            schedule_expression: 'rate(1 hour)',
            description: 'Hourly cleanup of stale resources'
          }
        }
      },
      {
        id: 'eb-2',
        type: 'resourceNode',
        position: { x: 200, y: 50 },
        data: {
          label: 'Daily Report',
          type: 'EVENTBRIDGE' as ResourceType,
          properties: {
            ...DEFAULT_PROPERTIES['EVENTBRIDGE'],
            rule_name: 'daily-report',
            schedule_expression: 'cron(0 8 * * ? *)',
            description: 'Daily report generation at 8am'
          }
        }
      },
      {
        id: 'lambda-1',
        type: 'resourceNode',
        position: { x: 500, y: 250 },
        data: { label: 'Cleanup Function', type: 'LAMBDA' as ResourceType, properties: { ...DEFAULT_PROPERTIES['LAMBDA'], function_name: 'resource-cleanup', runtime: 'python3.11' } }
      },
      {
        id: 'lambda-2',
        type: 'resourceNode',
        position: { x: 200, y: 250 },
        data: { label: 'Report Generator', type: 'LAMBDA' as ResourceType, properties: { ...DEFAULT_PROPERTIES['LAMBDA'], function_name: 'report-generator', runtime: 'python3.11' } }
      },
      {
        id: 'sns-1',
        type: 'resourceNode',
        position: { x: 350, y: 450 },
        data: { label: 'Notifications', type: 'SNS' as ResourceType, properties: { ...DEFAULT_PROPERTIES['SNS'], topic_name: 'automation-notifications', display_name: 'Automation Notifications' } }
      }
    ],
    edges: [
      { id: 'e-eb1-lam1', source: 'eb-1', target: 'lambda-1', animated: true, type: 'smoothstep' },
      { id: 'e-eb2-lam2', source: 'eb-2', target: 'lambda-2', animated: true, type: 'smoothstep' },
      { id: 'e-eb1-sns', source: 'eb-1', target: 'sns-1', animated: true, type: 'smoothstep' },
      { id: 'e-eb2-sns', source: 'eb-2', target: 'sns-1', animated: true, type: 'smoothstep' }
    ]
  },
  {
    id: 'eventbridge-event-router',
    name: 'Event-Driven Router',
    description: 'EventBridge as a central event router — capturing EC2 state changes and custom app events, routing to Lambda, SQS, and Kinesis targets.',
    nodes: [
      {
        id: 'eb-infra',
        type: 'resourceNode',
        position: { x: 500, y: 50 },
        data: {
          label: 'EC2 State Change',
          type: 'EVENTBRIDGE' as ResourceType,
          properties: {
            ...DEFAULT_PROPERTIES['EVENTBRIDGE'],
            rule_name: 'ec2-state-monitor',
            event_bus_name: 'default',
            event_pattern: '{"source":["aws.ec2"],"detail-type":["EC2 Instance State-change Notification"]}',
            description: 'Monitors EC2 instance state changes',
            schedule_expression: ''
          }
        }
      },
      {
        id: 'eb-app',
        type: 'resourceNode',
        position: { x: 200, y: 50 },
        data: {
          label: 'App Events',
          type: 'EVENTBRIDGE' as ResourceType,
          properties: {
            ...DEFAULT_PROPERTIES['EVENTBRIDGE'],
            rule_name: 'app-order-events',
            event_bus_name: 'custom-app-bus',
            event_pattern: '{"source":["com.myapp"],"detail-type":["OrderCreated"]}',
            description: 'Custom application order events',
            schedule_expression: ''
          }
        }
      },
      {
        id: 'lambda-1',
        type: 'resourceNode',
        position: { x: 500, y: 250 },
        data: { label: 'Infra Handler', type: 'LAMBDA' as ResourceType, properties: { ...DEFAULT_PROPERTIES['LAMBDA'], function_name: 'infra-event-handler', runtime: 'python3.11' } }
      },
      {
        id: 'sqs-1',
        type: 'resourceNode',
        position: { x: 200, y: 250 },
        data: { label: 'Order Processing', type: 'SQS' as ResourceType, properties: { ...DEFAULT_PROPERTIES['SQS'], queue_name: 'order-processing' } }
      },
      {
        id: 'kinesis-1',
        type: 'resourceNode',
        position: { x: 800, y: 250 },
        data: { label: 'Event Stream', type: 'KINESIS' as ResourceType, properties: { ...DEFAULT_PROPERTIES['KINESIS'], stream_name: 'event-archive-stream', shard_count: '1' } }
      },
      {
        id: 'sns-1',
        type: 'resourceNode',
        position: { x: 500, y: 450 },
        data: { label: 'Alert Topic', type: 'SNS' as ResourceType, properties: { ...DEFAULT_PROPERTIES['SNS'], topic_name: 'infra-alerts', display_name: 'Infrastructure Alerts' } }
      }
    ],
    edges: [
      { id: 'e-eb1-lam', source: 'eb-infra', target: 'lambda-1', animated: true, type: 'smoothstep' },
      { id: 'e-eb2-sqs', source: 'eb-app', target: 'sqs-1', animated: true, type: 'smoothstep' },
      { id: 'e-eb1-kin', source: 'eb-infra', target: 'kinesis-1', animated: true, type: 'smoothstep' },
      { id: 'e-eb2-kin', source: 'eb-app', target: 'kinesis-1', animated: true, type: 'smoothstep' },
      { id: 'e-eb1-sns', source: 'eb-infra', target: 'sns-1', animated: true, type: 'smoothstep' }
    ]
  }
];

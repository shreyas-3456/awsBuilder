import React from 'react';
import { Network } from 'lucide-react';

interface AwsIconProps {
  className?: string;
  alt?: string;
}

export const VpcIcon: React.FC<AwsIconProps> = ({ className, alt = 'VPC' }) => <img src="/icons/aws/vpc.svg" className={className} alt={alt} />;
export const SubnetIcon: React.FC<AwsIconProps> = ({ className, alt }) => <Network className={className} />;
export const Ec2Icon: React.FC<AwsIconProps> = ({ className, alt = 'EC2' }) => <img src="/icons/aws/ec2.svg" className={className} alt={alt} />;
export const S3Icon: React.FC<AwsIconProps> = ({ className, alt = 'S3' }) => <img src="/icons/aws/s3.svg" className={className} alt={alt} />;
export const RdsIcon: React.FC<AwsIconProps> = ({ className, alt = 'RDS' }) => <img src="/icons/aws/rds.svg" className={className} alt={alt} />;
export const InternetGatewayIcon: React.FC<AwsIconProps> = ({ className, alt = 'Internet Gateway' }) => <img src="/icons/aws/internet-gateway.svg" className={className} alt={alt} />;
export const LoadBalancerIcon: React.FC<AwsIconProps> = ({ className, alt = 'Load Balancer' }) => <img src="/icons/aws/load-balancer.svg" className={className} alt={alt} />;
export const LambdaIcon: React.FC<AwsIconProps> = ({ className, alt = 'Lambda' }) => <img src="/icons/aws/lambda.svg" className={className} alt={alt} />;
export const DynamoDbIcon: React.FC<AwsIconProps> = ({ className, alt = 'DynamoDB' }) => <img src="/icons/aws/dynamodb.svg" className={className} alt={alt} />;
export const ApiGatewayIcon: React.FC<AwsIconProps> = ({ className, alt = 'API Gateway' }) => <img src="/icons/aws/api-gateway.svg" className={className} alt={alt} />;
export const SqsIcon: React.FC<AwsIconProps> = ({ className, alt = 'SQS' }) => <img src="/icons/aws/sqs.svg" className={className} alt={alt} />;
export const SnsIcon: React.FC<AwsIconProps> = ({ className, alt = 'SNS' }) => <img src="/icons/aws/sns.svg" className={className} alt={alt} />;
export const KinesisIcon: React.FC<AwsIconProps> = ({ className, alt = 'Kinesis' }) => <img src="/icons/aws/kinesis.svg" className={className} alt={alt} />;
export const CloudWatchIcon: React.FC<AwsIconProps> = ({ className, alt = 'CloudWatch' }) => <img src="/icons/aws/cloudwatch.svg" className={className} alt={alt} />;
export const XRayIcon: React.FC<AwsIconProps> = ({ className, alt = 'X-Ray' }) => <img src="/icons/aws/x-ray.svg" className={className} alt={alt} />;
export const CloudTrailIcon: React.FC<AwsIconProps> = ({ className, alt = 'CloudTrail' }) => <img src="/icons/aws/cloudtrail.svg" className={className} alt={alt} />;
export const EcrIcon: React.FC<AwsIconProps> = ({ className, alt = 'ECR' }) => <img src="/icons/aws/ecr.svg" className={className} alt={alt} />;
export const EcsIcon: React.FC<AwsIconProps> = ({ className, alt = 'ECS' }) => <img src="/icons/aws/ecs.svg" className={className} alt={alt} />;
export const FargateIcon: React.FC<AwsIconProps> = ({ className, alt = 'Fargate' }) => <img src="/icons/aws/fargate.svg" className={className} alt={alt} />;
export const ElastiCacheIcon: React.FC<AwsIconProps> = ({ className, alt = 'ElastiCache' }) => <img src="/icons/aws/elasticache.svg" className={className} alt={alt} />;
export const EventBridgeIcon: React.FC<AwsIconProps> = ({ className, alt = 'EventBridge' }) => <img src="/icons/aws/eventbridge.svg" className={className} alt={alt} />;

// Fallback logic in case subnet doesn't exist
export const AwsIcon: React.FC<AwsIconProps & { type: keyof typeof ICONS }> = ({ type, className, alt }) => {
  const IconComponent = ICONS[type];
  return IconComponent ? <IconComponent className={className} alt={alt} /> : null;
};

const ICONS = {
  VPC: VpcIcon,
  SUBNET: SubnetIcon,
  EC2: Ec2Icon,
  S3: S3Icon,
  RDS: RdsIcon,
  INTERNET_GATEWAY: InternetGatewayIcon,
  LOAD_BALANCER: LoadBalancerIcon,
  LAMBDA: LambdaIcon,
  DYNAMODB: DynamoDbIcon,
  API_GATEWAY: ApiGatewayIcon,
  SQS: SqsIcon,
  SNS: SnsIcon,
  KINESIS: KinesisIcon,
  CLOUDWATCH: CloudWatchIcon,
  XRAY: XRayIcon,
  CLOUDTRAIL: CloudTrailIcon,
  ECR: EcrIcon,
  ECS: EcsIcon,
  FARGATE: FargateIcon,
  ELASTICACHE: ElastiCacheIcon,
  EVENTBRIDGE: EventBridgeIcon,
};

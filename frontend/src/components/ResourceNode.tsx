import { memo } from 'react';
import type { ReactNode } from 'react';
import { Handle, Position, NodeResizer } from 'reactflow';
import {
  VpcIcon, SubnetIcon, Ec2Icon, S3Icon, RdsIcon, InternetGatewayIcon,
  LoadBalancerIcon, LambdaIcon, DynamoDbIcon, ApiGatewayIcon, SqsIcon,
  SnsIcon, KinesisIcon, CloudWatchIcon, XRayIcon, CloudTrailIcon,
  EcrIcon, EcsIcon, FargateIcon, ElastiCacheIcon, EventBridgeIcon
} from './AwsIcons';

import { ResourceType } from '../types/diagram';

interface ResourceNodeProps {
  id: string;
  data: {
    label: string;
    type: ResourceType;
    properties?: Record<string, any>;
  };
  selected: boolean;
  onResize?: (nodeId: string, width: number, height: number) => void;
}

const RESOURCE_CONFIG: Record<ResourceType, {
  icon: ReactNode;
  gradient: string;
  accentColor: string;
  borderColor: string;
  bgGlow: string;
}> = {
  VPC: {
    icon: <VpcIcon className="w-7 h-7" />,
    gradient: 'from-blue-500 to-blue-600',
    accentColor: '#3b82f6',
    borderColor: 'border-blue-500/30',
    bgGlow: 'shadow-blue-500/20',
  },
  SUBNET: {
    icon: <SubnetIcon className="w-7 h-7" />,
    gradient: 'from-emerald-500 to-emerald-600',
    accentColor: '#22c55e',
    borderColor: 'border-emerald-500/30',
    bgGlow: 'shadow-emerald-500/20',
  },
  EC2: {
    icon: <Ec2Icon className="w-7 h-7" />,
    gradient: 'from-orange-500 to-orange-600',
    accentColor: '#FF9900',
    borderColor: 'border-orange-500/30',
    bgGlow: 'shadow-orange-500/20',
  },
  S3: {
    icon: <S3Icon className="w-7 h-7" />,
    gradient: 'from-amber-500 to-yellow-500',
    accentColor: '#eab308',
    borderColor: 'border-amber-500/30',
    bgGlow: 'shadow-amber-500/20',
  },
  RDS: {
    icon: <RdsIcon className="w-7 h-7" />,
    gradient: 'from-purple-500 to-violet-600',
    accentColor: '#a855f7',
    borderColor: 'border-purple-500/30',
    bgGlow: 'shadow-purple-500/20',
  },
  INTERNET_GATEWAY: {
    icon: <InternetGatewayIcon className="w-7 h-7" />,
    gradient: 'from-cyan-500 to-teal-500',
    accentColor: '#06b6d4',
    borderColor: 'border-cyan-500/30',
    bgGlow: 'shadow-cyan-500/20',
  },
  LOAD_BALANCER: {
    icon: <LoadBalancerIcon className="w-7 h-7" />,
    gradient: 'from-pink-500 to-rose-500',
    accentColor: '#ec4899',
    borderColor: 'border-pink-500/30',
    bgGlow: 'shadow-pink-500/20',
  },
  LAMBDA: {
    icon: <LambdaIcon className="w-7 h-7" />,
    gradient: 'from-orange-400 to-amber-600',
    accentColor: '#FF9900',
    borderColor: 'border-orange-400/30',
    bgGlow: 'shadow-orange-400/20',
  },
  DYNAMODB: {
    icon: <DynamoDbIcon className="w-7 h-7" />,
    gradient: 'from-indigo-500 to-blue-700',
    accentColor: '#6366f1',
    borderColor: 'border-indigo-500/30',
    bgGlow: 'shadow-indigo-500/20',
  },
  API_GATEWAY: {
    icon: <ApiGatewayIcon className="w-7 h-7" />,
    gradient: 'from-fuchsia-500 to-purple-600',
    accentColor: '#d946ef',
    borderColor: 'border-fuchsia-500/30',
    bgGlow: 'shadow-fuchsia-500/20',
  },
  SQS: {
    icon: <SqsIcon className="w-7 h-7" />,
    gradient: 'from-red-500 to-rose-600',
    accentColor: '#ef4444',
    borderColor: 'border-red-500/30',
    bgGlow: 'shadow-red-500/20',
  },
  SNS: {
    icon: <SnsIcon className="w-7 h-7" />,
    gradient: 'from-violet-500 to-purple-700',
    accentColor: '#8b5cf6',
    borderColor: 'border-violet-500/30',
    bgGlow: 'shadow-violet-500/20',
  },
  KINESIS: {
    icon: <KinesisIcon className="w-7 h-7" />,
    gradient: 'from-sky-500 to-blue-600',
    accentColor: '#0ea5e9',
    borderColor: 'border-sky-500/30',
    bgGlow: 'shadow-sky-500/20',
  },
  CLOUDWATCH: {
    icon: <CloudWatchIcon className="w-7 h-7" />,
    gradient: 'from-rose-500 to-red-700',
    accentColor: '#f43f5e',
    borderColor: 'border-rose-500/30',
    bgGlow: 'shadow-rose-500/20',
  },
  XRAY: {
    icon: <XRayIcon className="w-7 h-7" />,
    gradient: 'from-amber-500 to-orange-600',
    accentColor: '#f59e0b',
    borderColor: 'border-amber-500/30',
    bgGlow: 'shadow-amber-500/20',
  },
  CLOUDTRAIL: {
    icon: <CloudTrailIcon className="w-7 h-7" />,
    gradient: 'from-lime-500 to-green-600',
    accentColor: '#84cc16',
    borderColor: 'border-lime-500/30',
    bgGlow: 'shadow-lime-500/20',
  },
  ECR: {
    icon: <EcrIcon className="w-7 h-7" />,
    gradient: 'from-teal-500 to-cyan-600',
    accentColor: '#14b8a6',
    borderColor: 'border-teal-500/30',
    bgGlow: 'shadow-teal-500/20',
  },
  ECS: {
    icon: <EcsIcon className="w-7 h-7" />,
    gradient: 'from-orange-500 to-red-500',
    accentColor: '#f97316',
    borderColor: 'border-orange-500/30',
    bgGlow: 'shadow-orange-500/20',
  },
  FARGATE: {
    icon: <FargateIcon className="w-7 h-7" />,
    gradient: 'from-violet-500 to-indigo-600',
    accentColor: '#8b5cf6',
    borderColor: 'border-violet-500/30',
    bgGlow: 'shadow-violet-500/20',
  },
  ELASTICACHE: {
    icon: <ElastiCacheIcon className="w-7 h-7" />,
    gradient: 'from-cyan-500 to-blue-600',
    accentColor: '#0ea5e9',
    borderColor: 'border-cyan-500/30',
    bgGlow: 'shadow-cyan-500/20',
  },
  EVENTBRIDGE: {
    icon: <EventBridgeIcon className="w-7 h-7" />,
    gradient: 'from-pink-500 to-fuchsia-600',
    accentColor: '#FF4F8B',
    borderColor: 'border-pink-500/30',
    bgGlow: 'shadow-pink-500/20',
  },
};

const RESOURCE_LABELS: Record<ResourceType, string> = {
  VPC: 'Virtual Private Cloud',
  SUBNET: 'Subnet',
  EC2: 'EC2 Instance',
  S3: 'S3 Bucket',
  RDS: 'RDS Database',
  INTERNET_GATEWAY: 'Internet Gateway',
  LOAD_BALANCER: 'Load Balancer',
  LAMBDA: 'Lambda Function',
  DYNAMODB: 'DynamoDB Table',
  API_GATEWAY: 'API Gateway',
  SQS: 'SQS Queue',
  SNS: 'SNS Topic',
  KINESIS: 'Kinesis Stream',
  CLOUDWATCH: 'CloudWatch Alarm',
  XRAY: 'X-Ray Sampling Rule',
  CLOUDTRAIL: 'CloudTrail Trail',
  ECR: 'ECR Registry',
  ECS: 'ECS Cluster',
  FARGATE: 'Fargate Task',
  ELASTICACHE: 'ElastiCache Cluster',
  EVENTBRIDGE: 'EventBridge Rule',
};

export const ResourceNode = memo(({ data, selected }: ResourceNodeProps) => {
  const config = RESOURCE_CONFIG[data.type];
  const friendlyName = RESOURCE_LABELS[data.type];

  return (
    <>
      <NodeResizer
        isVisible={selected}
        minWidth={160}
        minHeight={80}
        lineClassName="!border-indigo-400/50"
        handleClassName="!w-2.5 !h-2.5 !bg-indigo-400 !border-indigo-300 !rounded-sm"
      />

      <Handle
        type="target"
        position={Position.Top}
        className="resource-handle"
        style={{ background: config.accentColor }}
      />

      <div
        className={`
          resource-node-card
          ${config.borderColor}
          ${selected ? `ring-2 ring-indigo-400/60 ${config.bgGlow} shadow-xl` : 'shadow-lg'}
        `}
        style={{ width: '100%', height: '100%', minWidth: 160, minHeight: 80 }}
      >
        {/* Accent bar at top */}
        <div className={`absolute top-0 left-0 right-0 h-1 rounded-t-xl bg-gradient-to-r ${config.gradient}`} />

        <div className="flex items-center gap-3 p-3 pt-4">
          {/* Icon container */}
          <div
            className={`
              flex items-center justify-center w-10 h-10 rounded-lg
              bg-gradient-to-br ${config.gradient}
              text-white shadow-md
            `}
            style={{ boxShadow: `0 4px 14px ${config.accentColor}33` }}
          >
            {config.icon}
          </div>

          {/* Label */}
          <div className="flex-1 min-w-0">
            <div className="text-xs font-bold text-slate-200 tracking-wide uppercase">
              {data.type.replace('_', ' ')}
            </div>
            <div className="text-[10px] text-slate-400 truncate mt-0.5" title={friendlyName}>
              {friendlyName}
            </div>
          </div>
        </div>

        {/* Properties preview */}
        {data.properties && Object.keys(data.properties).length > 0 && (
          <div className="px-3 pb-2">
            <div className="flex flex-wrap gap-1">
              {Object.entries(data.properties).slice(0, 2).map(([key, value]) => (
                value ? (
                  <span
                    key={key}
                    className="inline-flex items-center px-1.5 py-0.5 rounded text-[9px]
                               bg-slate-700/60 text-slate-300 border border-slate-600/30"
                    title={`${key}: ${value}`}
                  >
                    {String(value).slice(0, 16)}
                  </span>
                ) : null
              ))}
            </div>
          </div>
        )}
      </div>

      <Handle
        type="source"
        position={Position.Bottom}
        className="resource-handle"
        style={{ background: config.accentColor }}
      />
    </>
  );
});

ResourceNode.displayName = 'ResourceNode';

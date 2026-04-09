import {
  LayoutGrid,
  Play,
  Sparkles,
  Trash2,
  Globe,
} from 'lucide-react';
import {
  VpcIcon, SubnetIcon, Ec2Icon, S3Icon, RdsIcon, InternetGatewayIcon,
  LoadBalancerIcon, LambdaIcon, DynamoDbIcon, ApiGatewayIcon, SqsIcon,
  SnsIcon, KinesisIcon, CloudWatchIcon, XRayIcon, CloudTrailIcon,
  EcrIcon, EcsIcon, FargateIcon, ElastiCacheIcon, EventBridgeIcon
} from './AwsIcons';
import type { ReactNode } from 'react';
import { ResourceType } from '../types/diagram';

interface ToolbarProps {
  onAddResource: (type: ResourceType) => void;
  onGenerate: () => void;
  onClearDiagrams: () => void;
  isGenerating: boolean;
  hasDiagrams: boolean;
  selectedRegion: string;
  onRegionChange: (region: string) => void;
  onToggleTemplates: () => void;
  showTemplates: boolean;
}

export const REGIONS = [
  { value: 'us-east-1', label: 'US East (N. Virginia)' },
  { value: 'us-east-2', label: 'US East (Ohio)' },
  { value: 'us-west-1', label: 'US West (N. California)' },
  { value: 'us-west-2', label: 'US West (Oregon)' },
  { value: 'eu-west-1', label: 'Europe (Ireland)' },
  { value: 'eu-central-1', label: 'Europe (Frankfurt)' },
  { value: 'ap-southeast-1', label: 'Asia Pacific (Singapore)' },
  { value: 'ap-northeast-1', label: 'Asia Pacific (Tokyo)' },
];

const RESOURCES: Array<{ type: ResourceType; icon: ReactNode; label: string; gradient: string }> = [
  { type: 'VPC', icon: <VpcIcon className="h-4 w-4" />, label: 'VPC', gradient: 'from-blue-500/20 to-blue-600/20 hover:from-blue-500/30 hover:to-blue-600/30 text-blue-300 border-blue-500/20 hover:border-blue-400/40' },
  { type: 'SUBNET', icon: <SubnetIcon className="h-4 w-4" />, label: 'Subnet', gradient: 'from-emerald-500/20 to-emerald-600/20 hover:from-emerald-500/30 hover:to-emerald-600/30 text-emerald-300 border-emerald-500/20 hover:border-emerald-400/40' },
  { type: 'EC2', icon: <Ec2Icon className="h-4 w-4" />, label: 'EC2', gradient: 'from-orange-500/20 to-orange-600/20 hover:from-orange-500/30 hover:to-orange-600/30 text-orange-300 border-[#FF9900]/20 hover:border-[#FF9900]/40' },
  { type: 'S3', icon: <S3Icon className="h-4 w-4" />, label: 'S3', gradient: 'from-amber-500/20 to-yellow-500/20 hover:from-amber-500/30 hover:to-yellow-500/30 text-amber-300 border-amber-500/20 hover:border-amber-400/40' },
  { type: 'RDS', icon: <RdsIcon className="h-4 w-4" />, label: 'RDS', gradient: 'from-purple-500/20 to-violet-500/20 hover:from-purple-500/30 hover:to-violet-500/30 text-purple-300 border-purple-500/20 hover:border-purple-400/40' },
  { type: 'INTERNET_GATEWAY', icon: <InternetGatewayIcon className="h-4 w-4" />, label: 'IGW', gradient: 'from-cyan-500/20 to-teal-500/20 hover:from-cyan-500/30 hover:to-teal-500/30 text-cyan-300 border-cyan-500/20 hover:border-cyan-400/40' },
  { type: 'LOAD_BALANCER', icon: <LoadBalancerIcon className="h-4 w-4" />, label: 'ALB', gradient: 'from-pink-500/20 to-rose-500/20 hover:from-pink-500/30 hover:to-rose-500/30 text-pink-300 border-pink-500/20 hover:border-pink-400/40' },
  { type: 'LAMBDA', icon: <LambdaIcon className="h-4 w-4" />, label: 'Lambda', gradient: 'from-orange-400/20 to-amber-600/20 hover:from-orange-400/30 hover:to-amber-600/30 text-orange-200 border-[#FF9900]/20 hover:border-[#FF9900]/40' },
  { type: 'DYNAMODB', icon: <DynamoDbIcon className="h-4 w-4" />, label: 'Dynamo', gradient: 'from-indigo-500/20 to-blue-700/20 hover:from-indigo-500/30 hover:to-blue-700/30 text-indigo-200 border-indigo-500/20 hover:border-indigo-400/40' },
  { type: 'API_GATEWAY', icon: <ApiGatewayIcon className="h-4 w-4" />, label: 'API GW', gradient: 'from-fuchsia-500/20 to-purple-600/20 hover:from-fuchsia-500/30 hover:to-purple-600/30 text-fuchsia-300 border-fuchsia-500/20 hover:border-fuchsia-400/40' },
  { type: 'SQS', icon: <SqsIcon className="h-4 w-4" />, label: 'SQS', gradient: 'from-red-500/20 to-rose-600/20 hover:from-red-500/30 hover:to-rose-600/30 text-red-300 border-red-500/20 hover:border-red-400/40' },
  { type: 'SNS', icon: <SnsIcon className="h-4 w-4" />, label: 'SNS', gradient: 'from-violet-500/20 to-purple-700/20 hover:from-violet-500/30 hover:to-purple-700/30 text-violet-300 border-violet-500/20 hover:border-violet-400/40' },
  { type: 'KINESIS', icon: <KinesisIcon className="h-4 w-4" />, label: 'Kinesis', gradient: 'from-sky-500/20 to-blue-600/20 hover:from-sky-500/30 hover:to-blue-600/30 text-sky-300 border-sky-500/20 hover:border-sky-400/40' },
  { type: 'CLOUDWATCH', icon: <CloudWatchIcon className="h-4 w-4" />, label: 'CW', gradient: 'from-rose-500/20 to-red-700/20 hover:from-rose-500/30 hover:to-red-700/30 text-rose-300 border-rose-500/20 hover:border-rose-400/40' },
  { type: 'XRAY', icon: <XRayIcon className="h-4 w-4" />, label: 'X-Ray', gradient: 'from-amber-500/20 to-orange-600/20 hover:from-amber-500/30 hover:to-orange-600/30 text-amber-300 border-amber-500/20 hover:border-amber-400/40' },
  { type: 'CLOUDTRAIL', icon: <CloudTrailIcon className="h-4 w-4" />, label: 'Trail', gradient: 'from-lime-500/20 to-green-600/20 hover:from-lime-500/30 hover:to-green-600/30 text-lime-300 border-lime-500/20 hover:border-lime-400/40' },
  { type: 'ECR', icon: <EcrIcon className="h-4 w-4" />, label: 'ECR', gradient: 'from-teal-500/20 to-cyan-600/20 hover:from-teal-500/30 hover:to-cyan-600/30 text-teal-300 border-teal-500/20 hover:border-teal-400/40' },
  { type: 'ECS', icon: <EcsIcon className="h-4 w-4" />, label: 'ECS', gradient: 'from-orange-500/20 to-red-500/20 hover:from-orange-500/30 hover:to-red-500/30 text-orange-300 border-orange-500/20 hover:border-orange-400/40' },
  { type: 'FARGATE', icon: <FargateIcon className="h-4 w-4" />, label: 'Fargate', gradient: 'from-violet-500/20 to-indigo-600/20 hover:from-violet-500/30 hover:to-indigo-600/30 text-violet-300 border-violet-500/20 hover:border-violet-400/40' },
  { type: 'ELASTICACHE', icon: <ElastiCacheIcon className="h-4 w-4" />, label: 'ElastiCache', gradient: 'from-cyan-500/20 to-blue-600/20 hover:from-cyan-500/30 hover:to-blue-600/30 text-cyan-300 border-cyan-500/20 hover:border-cyan-400/40' },
  { type: 'EVENTBRIDGE', icon: <EventBridgeIcon className="h-4 w-4" />, label: 'EventBridge', gradient: 'from-pink-500/20 to-fuchsia-600/20 hover:from-pink-500/30 hover:to-fuchsia-600/30 text-pink-300 border-[#FF4F8B]/20 hover:border-[#FF4F8B]/40' },
];

export function Toolbar({
  onAddResource,
  onGenerate,
  onClearDiagrams,
  isGenerating,
  hasDiagrams,
  selectedRegion,
  onRegionChange,
  onToggleTemplates,
  showTemplates,
}: ToolbarProps) {
  return (
    <div className="sticky top-0 z-30 border-b border-slate-800/80 bg-slate-950/95 px-4 py-3 backdrop-blur-xl sm:px-5">
      <div className="mx-auto flex max-w-[1800px] flex-col gap-3">
        <div className="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
          <div className="flex flex-wrap items-center gap-2.5 sm:gap-3">
            <div className="flex items-center gap-2.5 pr-1 sm:pr-3">
              <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl bg-gradient-to-br from-indigo-500 to-violet-600 shadow-lg shadow-indigo-500/20">
                <Sparkles className="h-4 w-4 text-white" />
              </div>
              <div className="min-w-0">
                <h1 className="text-base font-semibold leading-tight text-white sm:text-lg">
                  Terraform Builder
                </h1>
              </div>
            </div>

            <div className="flex items-center gap-2 rounded-xl border border-slate-800 bg-slate-900/80 px-3 py-2">
              <Globe className="h-4 w-4 text-slate-400" />
              <select
                value={selectedRegion}
                onChange={(e) => onRegionChange(e.target.value)}
                className="min-w-0 cursor-pointer bg-transparent text-sm font-medium text-slate-100 outline-none"
              >
                {REGIONS.map((region) => (
                  <option key={region.value} value={region.value} className="bg-slate-900">
                    {region.label}
                  </option>
                ))}
              </select>
            </div>

            <button
              onClick={onToggleTemplates}
              className={`
                flex items-center gap-2 rounded-xl border px-3 py-2 text-sm font-medium transition-all duration-200
                ${showTemplates
                  ? 'border-indigo-500/60 bg-indigo-500/12 text-indigo-200'
                  : 'border-slate-800 bg-slate-900/80 text-slate-200 hover:border-slate-700 hover:bg-slate-900'
                }
              `}
              title="Browse Templates"
            >
              <LayoutGrid className={`h-4 w-4 ${showTemplates ? 'text-indigo-300' : 'text-slate-400'}`} />
              Templates
            </button>

            <button
              onClick={onClearDiagrams}
              disabled={!hasDiagrams}
              className={`
                flex items-center gap-2 rounded-xl border px-3 py-2 text-sm font-medium transition-all duration-200
                ${hasDiagrams
                  ? 'border-rose-500/40 bg-rose-500/10 text-rose-100 hover:border-rose-400/60 hover:bg-rose-500/15'
                  : 'cursor-not-allowed border-slate-800 bg-slate-900/60 text-slate-500 opacity-60'
                }
              `}
              title="Clear all diagrams"
            >
              <Trash2 className={`h-4 w-4 ${hasDiagrams ? 'text-rose-300' : 'text-slate-500'}`} />
              Clear Canvas
            </button>
          </div>

          <button
            onClick={onGenerate}
            disabled={isGenerating}
            className={`
              flex items-center justify-center gap-2 self-start rounded-xl px-4 py-2 text-sm font-semibold text-white transition-all duration-200 lg:self-auto
              ${isGenerating
                ? 'cursor-not-allowed bg-slate-700/80 opacity-60'
                : 'bg-gradient-to-r from-indigo-500 to-violet-600 shadow-lg shadow-indigo-500/20 hover:from-indigo-400 hover:to-violet-500'
              }
            `}
          >
            <Play className={`h-4 w-4 ${isGenerating ? 'animate-spin' : ''}`} />
            {isGenerating ? 'Generating...' : 'Generate Terraform'}
          </button>
        </div>

        <div className="flex flex-wrap gap-2">
          {RESOURCES.map((resource) => (
            <button
              key={resource.type}
              onClick={() => onAddResource(resource.type)}
              className={`
                flex items-center gap-2 rounded-xl border bg-gradient-to-b px-3 py-2 text-xs font-semibold backdrop-blur-sm
                ${resource.gradient}
                transition-all duration-200 ease-out hover:border-slate-500/40 hover:shadow-lg active:scale-[0.98]
              `}
              title={`Add ${resource.label}`}
            >
              {resource.icon}
              <span>{resource.label}</span>
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}

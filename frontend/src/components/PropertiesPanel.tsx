import { CSSProperties, PointerEvent } from 'react';
import { Node } from 'reactflow';
import { ResourceType, ResourceProperties } from '../types/diagram';
import { Settings2, Trash2, X } from 'lucide-react';
import { CatalogSelector } from './CatalogSelector';
import { CatalogItem, Ec2Instance, Vpc, Subnet, S3Bucket, Alb } from '../services/configService';
import { REGIONS } from './Toolbar';
import { PanelResizeHandle } from './PanelResizeHandle';

interface PropertiesPanelProps {
  selectedNode: Node | null;
  onPropertyChange: (nodeId: string, properties: ResourceProperties) => void;
  onDeleteNode: (nodeId: string) => void;
  selectedRegion: string;
  onClose?: () => void;
  variant?: 'sidebar' | 'sheet';
  className?: string;
  style?: CSSProperties;
  onResizeStart?: (event: PointerEvent<HTMLButtonElement>, direction: 'horizontal' | 'vertical') => void;
}

const PROPERTY_FIELDS: Record<ResourceType, Array<{ name: string; label: string; type: string; required?: boolean; options?: string[] }>> = {
  VPC: [
    { name: 'cidr_block', label: 'CIDR Block', type: 'text', required: true },
    { name: 'enable_dns_hostnames', label: 'DNS Hostnames', type: 'checkbox' },
    { name: 'enable_dns_support', label: 'DNS Support', type: 'checkbox' },
    { name: 'instance_tenancy', label: 'Tenancy', type: 'select', options: ['default', 'dedicated'] },
    { name: 'tags', label: 'Tags (Key=Value,...)', type: 'text' },
    { name: 'region', label: 'Region', type: 'select', options: REGIONS.map(r => r.value) }
  ],
  SUBNET: [
    { name: 'cidr_block', label: 'CIDR Block', type: 'text', required: true },
    { name: 'availability_zone', label: 'Availability Zone', type: 'text', required: true },
    { name: 'map_public_ip_on_launch', label: 'Public IP on Launch', type: 'checkbox' },
    { name: 'tags', label: 'Tags (Key=Value,...)', type: 'text' },
    { name: 'region', label: 'Region', type: 'select', options: REGIONS.map(r => r.value) }
  ],
  EC2: [
    { name: 'ami', label: 'AMI ID', type: 'text', required: true },
    { name: 'instance_type', label: 'Instance Type', type: 'text', required: true },
    { name: 'key_name', label: 'Key Pair Name', type: 'text' },
    { name: 'associate_public_ip_address', label: 'Public IP', type: 'checkbox' },
    { name: 'user_data', label: 'User Data (Script)', type: 'textarea' },
    { name: 'tags', label: 'Tags (Key=Value,...)', type: 'text' },
    { name: 'region', label: 'Region', type: 'select', options: REGIONS.map(r => r.value) }
  ],
  S3: [
    { name: 'bucket_name', label: 'Bucket Name', type: 'text', required: true },
    { name: 'acl', label: 'ACL', type: 'select', options: ['private', 'public-read', 'public-read-write', 'authenticated-read'] },
    { name: 'versioning', label: 'Versioning', type: 'select', options: ['Enabled', 'Disabled', 'Suspended'] },
    { name: 'force_destroy', label: 'Force Destroy', type: 'checkbox' },
    { name: 'tags', label: 'Tags (Key=Value,...)', type: 'text' },
    { name: 'region', label: 'Region', type: 'select', options: REGIONS.map(r => r.value) }
  ],
  RDS: [
    { name: 'engine', label: 'Engine', type: 'select', required: true, options: ['mysql', 'postgres', 'mariadb', 'oracle-ee', 'sqlserver-ex'] },
    { name: 'engine_version', label: 'Engine Version', type: 'text' },
    { name: 'instance_class', label: 'Instance Class', type: 'text', required: true },
    { name: 'allocated_storage', label: 'Storage (GB)', type: 'text', required: true },
    { name: 'storage_type', label: 'Storage Type', type: 'select', options: ['standard', 'gp2', 'io1'] },
    { name: 'db_name', label: 'DB Name', type: 'text', required: true },
    { name: 'username', label: 'Username', type: 'text', required: true },
    { name: 'password', label: 'Password', type: 'password', required: true },
    { name: 'publicly_accessible', label: 'Public Access', type: 'checkbox' },
    { name: 'skip_final_snapshot', label: 'Skip Final Snapshot', type: 'checkbox' },
    { name: 'tags', label: 'Tags (Key=Value,...)', type: 'text' },
    { name: 'region', label: 'Region', type: 'select', options: REGIONS.map(r => r.value) }
  ],
  INTERNET_GATEWAY: [
    { name: 'tags', label: 'Tags (Key=Value,...)', type: 'text' },
    { name: 'region', label: 'Region', type: 'select', options: REGIONS.map(r => r.value) }
  ],
  LOAD_BALANCER: [
    { name: 'name', label: 'LB Name', type: 'text' },
    { name: 'load_balancer_type', label: 'Type', type: 'select', required: true, options: ['application', 'network', 'gateway'] },
    { name: 'internal', label: 'Internal', type: 'checkbox' },
    { name: 'security_groups', label: 'Security Groups (CSV)', type: 'text' },
    { name: 'enable_deletion_protection', label: 'Deletion Protection', type: 'checkbox' },
    { name: 'tags', label: 'Tags (Key=Value,...)', type: 'text' },
    { name: 'region', label: 'Region', type: 'select', options: REGIONS.map(r => r.value) }
  ],
  LAMBDA: [
    { name: 'function_name', label: 'Function Name', type: 'text', required: true },
    { name: 'runtime', label: 'Runtime', type: 'select', required: true, options: ['nodejs18.x', 'nodejs20.x', 'python3.9', 'python3.10', 'python3.11', 'java17', 'java21', 'go1.x', 'ruby3.2'] },
    { name: 'handler', label: 'Handler', type: 'text', required: true },
    { name: 'tags', label: 'Tags (Key=Value,...)', type: 'text' },
    { name: 'region', label: 'Region', type: 'select', options: REGIONS.map(r => r.value) }
  ],
  DYNAMODB: [
    { name: 'table_name', label: 'Table Name', type: 'text', required: true },
    { name: 'billing_mode', label: 'Billing Mode', type: 'select', required: true, options: ['PAY_PER_REQUEST', 'PROVISIONED'] },
    { name: 'hash_key', label: 'Partition Key', type: 'text', required: true },
    { name: 'tags', label: 'Tags (Key=Value,...)', type: 'text' },
    { name: 'region', label: 'Region', type: 'select', options: REGIONS.map(r => r.value) }
  ],
  API_GATEWAY: [
    { name: 'name', label: 'API Name', type: 'text', required: true },
    { name: 'tags', label: 'Tags (Key=Value,...)', type: 'text' },
    { name: 'region', label: 'Region', type: 'select', options: REGIONS.map(r => r.value) }
  ],
  SQS: [
    { name: 'queue_name', label: 'Queue Name', type: 'text', required: true },
    { name: 'delay_seconds', label: 'Delay (seconds)', type: 'select', options: ['0', '5', '10', '15', '30', '60', '120', '300', '600', '900'] },
    { name: 'visibility_timeout', label: 'Visibility Timeout (sec)', type: 'select', options: ['0', '5', '10', '15', '30', '60', '120', '300', '600', '900', '3600', '43200'] },
    { name: 'message_retention_seconds', label: 'Retention (seconds)', type: 'select', options: ['60', '3600', '86400', '345600', '604800', '1209600'] },
    { name: 'max_message_size', label: 'Max Message Size (bytes)', type: 'select', options: ['1024', '4096', '16384', '65536', '262144'] },
    { name: 'fifo_queue', label: 'FIFO Queue', type: 'select', options: ['false', 'true'] },
    { name: 'tags', label: 'Tags (Key=Value,...)', type: 'text' },
    { name: 'region', label: 'Region', type: 'select', options: REGIONS.map(r => r.value) }
  ],
  SNS: [
    { name: 'topic_name', label: 'Topic Name', type: 'text', required: true },
    { name: 'display_name', label: 'Display Name', type: 'text' },
    { name: 'fifo_topic', label: 'FIFO Topic', type: 'select', options: ['false', 'true'] },
    { name: 'tags', label: 'Tags (Key=Value,...)', type: 'text' },
    { name: 'region', label: 'Region', type: 'select', options: REGIONS.map(r => r.value) }
  ],
  KINESIS: [
    { name: 'stream_name', label: 'Stream Name', type: 'text', required: true },
    { name: 'shard_count', label: 'Shard Count', type: 'select', required: true, options: ['1', '2', '3', '4', '5', '10', '20', '50'] },
    { name: 'retention_period', label: 'Retention (hours)', type: 'select', options: ['24', '48', '72', '96', '120', '168', '720', '2160', '4320', '8760'] },
    { name: 'stream_mode', label: 'Capacity Mode', type: 'select', options: ['PROVISIONED', 'ON_DEMAND'] },
    { name: 'tags', label: 'Tags (Key=Value,...)', type: 'text' },
    { name: 'region', label: 'Region', type: 'select', options: REGIONS.map(r => r.value) }
  ],
  CLOUDWATCH: [
    { name: 'alarm_name', label: 'Alarm Name', type: 'text', required: true },
    { name: 'metric_name', label: 'Metric Name', type: 'select', required: true, options: ['CPUUtilization', 'NetworkIn', 'NetworkOut', 'DiskReadOps', 'DiskWriteOps', 'StatusCheckFailed', 'Errors', 'Invocations', 'Duration', 'Throttles', 'NumberOfMessagesSent', 'ApproximateNumberOfMessagesVisible', 'IncomingRecords'] },
    { name: 'namespace', label: 'Namespace', type: 'select', options: ['AWS/EC2', 'AWS/RDS', 'AWS/Lambda', 'AWS/SQS', 'AWS/SNS', 'AWS/Kinesis', 'AWS/ELB', 'AWS/S3', 'AWS/DynamoDB', 'AWS/ApiGateway'] },
    { name: 'statistic', label: 'Statistic', type: 'select', options: ['Average', 'Sum', 'Minimum', 'Maximum', 'SampleCount'] },
    { name: 'period', label: 'Period (seconds)', type: 'select', options: ['60', '120', '300', '600', '900', '1800', '3600'] },
    { name: 'evaluation_periods', label: 'Evaluation Periods', type: 'select', options: ['1', '2', '3', '5', '10'] },
    { name: 'threshold', label: 'Threshold', type: 'text', required: true },
    { name: 'comparison_operator', label: 'Comparison', type: 'select', options: ['GreaterThanThreshold', 'GreaterThanOrEqualToThreshold', 'LessThanThreshold', 'LessThanOrEqualToThreshold'] },
    { name: 'tags', label: 'Tags (Key=Value,...)', type: 'text' },
    { name: 'region', label: 'Region', type: 'select', options: REGIONS.map(r => r.value) }
  ],
  XRAY: [
    { name: 'rule_name', label: 'Rule Name', type: 'text', required: true },
    { name: 'priority', label: 'Priority', type: 'select', required: true, options: ['1', '10', '100', '1000', '5000', '10000'] },
    { name: 'fixed_rate', label: 'Fixed Rate (0-1)', type: 'select', required: true, options: ['0.01', '0.05', '0.1', '0.25', '0.5', '1.0'] },
    { name: 'reservoir_size', label: 'Reservoir Size', type: 'select', options: ['0', '1', '5', '10', '50'] },
    { name: 'service_name', label: 'Service Name', type: 'text' },
    { name: 'service_type', label: 'Service Type', type: 'select', options: ['*', 'AWS::EC2::Instance', 'AWS::ECS::Container', 'AWS::EKS::Container', 'AWS::Lambda::Function', 'AWS::ApiGateway::Stage'] },
    { name: 'host', label: 'Host', type: 'text' },
    { name: 'http_method', label: 'HTTP Method', type: 'select', options: ['*', 'GET', 'POST', 'PUT', 'DELETE', 'PATCH'] },
    { name: 'url_path', label: 'URL Path', type: 'text' },
    { name: 'tags', label: 'Tags (Key=Value,...)', type: 'text' },
    { name: 'region', label: 'Region', type: 'select', options: REGIONS.map(r => r.value) }
  ],
  CLOUDTRAIL: [
    { name: 'trail_name', label: 'Trail Name', type: 'text', required: true },
    { name: 's3_bucket_name', label: 'S3 Bucket Name', type: 'text', required: true },
    { name: 'is_multi_region_trail', label: 'Multi-Region Trail', type: 'select', options: ['true', 'false'] },
    { name: 'enable_log_file_validation', label: 'Log File Validation', type: 'select', options: ['true', 'false'] },
    { name: 'include_global_service_events', label: 'Global Service Events', type: 'select', options: ['true', 'false'] },
    { name: 'enable_logging', label: 'Enable Logging', type: 'select', options: ['true', 'false'] },
    { name: 'tags', label: 'Tags (Key=Value,...)', type: 'text' },
    { name: 'region', label: 'Region', type: 'select', options: REGIONS.map(r => r.value) }
  ],
  ECR: [
    { name: 'repository_name', label: 'Repository Name', type: 'text', required: true },
    { name: 'image_tag_mutability', label: 'Tag Mutability', type: 'select', options: ['MUTABLE', 'IMMUTABLE'] },
    { name: 'scan_on_push', label: 'Scan on Push', type: 'select', options: ['true', 'false'] },
    { name: 'encryption_type', label: 'Encryption Type', type: 'select', options: ['AES256', 'KMS'] },
    { name: 'force_delete', label: 'Force Delete', type: 'select', options: ['false', 'true'] },
    { name: 'tags', label: 'Tags (Key=Value,...)', type: 'text' },
    { name: 'region', label: 'Region', type: 'select', options: REGIONS.map(r => r.value) }
  ],
  ECS: [
    { name: 'cluster_name', label: 'Cluster Name', type: 'text', required: true },
    { name: 'container_insights', label: 'Container Insights', type: 'select', options: ['enabled', 'disabled'] },
    { name: 'tags', label: 'Tags (Key=Value,...)', type: 'text' },
    { name: 'region', label: 'Region', type: 'select', options: REGIONS.map(r => r.value) }
  ],
  FARGATE: [
    { name: 'family', label: 'Task Family', type: 'text', required: true },
    { name: 'cpu', label: 'CPU Units', type: 'select', required: true, options: ['256', '512', '1024', '2048', '4096'] },
    { name: 'memory', label: 'Memory (MiB)', type: 'select', required: true, options: ['512', '1024', '2048', '3072', '4096', '5120', '6144', '7168', '8192', '16384', '30720'] },
    { name: 'container_name', label: 'Container Name', type: 'text', required: true },
    { name: 'container_image', label: 'Container Image', type: 'text', required: true },
    { name: 'container_port', label: 'Container Port', type: 'text' },
    { name: 'tags', label: 'Tags (Key=Value,...)', type: 'text' },
    { name: 'region', label: 'Region', type: 'select', options: REGIONS.map(r => r.value) }
  ],
  ELASTICACHE: [
    { name: 'cluster_id', label: 'Cluster ID', type: 'text', required: true },
    { name: 'engine', label: 'Engine', type: 'select', required: true, options: ['redis', 'memcached'] },
    { name: 'node_type', label: 'Node Type', type: 'text', required: true },
    { name: 'num_cache_nodes', label: 'Cache Nodes', type: 'select', options: ['1', '2', '3', '4', '5', '6'] },
    { name: 'engine_version', label: 'Engine Version', type: 'text' },
    { name: 'port', label: 'Port', type: 'text' },
    { name: 'tags', label: 'Tags (Key=Value,...)', type: 'text' },
    { name: 'region', label: 'Region', type: 'select', options: REGIONS.map(r => r.value) }
  ],
  EVENTBRIDGE: [
    { name: 'rule_name', label: 'Rule Name', type: 'text', required: true },
    { name: 'event_bus_name', label: 'Event Bus', type: 'text' },
    { name: 'description', label: 'Description', type: 'text' },
    { name: 'schedule_expression', label: 'Schedule', type: 'text' },
    { name: 'state', label: 'State', type: 'select', options: ['ENABLED', 'DISABLED'] },
    { name: 'tags', label: 'Tags (Key=Value,...)', type: 'text' },
    { name: 'region', label: 'Region', type: 'select', options: REGIONS.map(r => r.value) }
  ]
};

// Resource types that support catalog data
const CATALOG_SUPPORTED_TYPES: ResourceType[] = ['EC2', 'VPC', 'SUBNET', 'S3', 'INTERNET_GATEWAY', 'LOAD_BALANCER'];

/**
 * Maps catalog item data to resource properties based on resource type.
 */
function mapCatalogToProperties(resourceType: ResourceType, catalogItem: CatalogItem): ResourceProperties {
  const properties: ResourceProperties = {};

  switch (resourceType) {
    case 'EC2': {
      const ec2 = catalogItem as Ec2Instance;
      properties.ami = ec2.amiId;
      properties.instance_type = ec2.instanceType;
      properties.key_name = ec2.keyName;
      properties.associate_public_ip_address = !!ec2.publicIp;
      break;
    }
    case 'VPC': {
      const vpc = catalogItem as Vpc;
      properties.cidr_block = vpc.cidrBlock;
      properties.instance_tenancy = vpc.instanceTenancy;
      break;
    }
    case 'SUBNET': {
      const subnet = catalogItem as Subnet;
      properties.cidr_block = subnet.cidrBlock;
      properties.availability_zone = subnet.availabilityZone;
      properties.map_public_ip_on_launch = subnet.mapPublicIpOnLaunch;
      break;
    }
    case 'S3': {
      const s3 = catalogItem as S3Bucket;
      properties.bucket_name = s3.bucketName;
      properties.versioning = s3.versioning;
      break;
    }
    case 'INTERNET_GATEWAY': {
      // IGW has minimal properties
      break;
    }
    case 'LOAD_BALANCER': {
      const alb = catalogItem as Alb;
      properties.name = alb.albName;
      properties.load_balancer_type = alb.type;
      properties.internal = alb.scheme === 'internal';
      properties.security_groups = alb.securityGroups.join(',');
      break;
    }
  }

  return properties;
}

export function PropertiesPanel({
  selectedNode,
  onPropertyChange,
  onDeleteNode,
  selectedRegion,
  onClose,
  variant = 'sidebar',
  className = '',
  style,
  onResizeStart,
}: PropertiesPanelProps) {
  const isSheet = variant === 'sheet';
  const panelClassName = [
    isSheet
      ? 'fixed inset-x-3 bottom-3 z-50 flex h-full max-h-[calc(100vh-6.5rem)] flex-col overflow-hidden rounded-2xl border border-slate-700/60 bg-slate-900/95 p-5 shadow-2xl shadow-slate-950/60 backdrop-blur-xl sm:inset-x-4'
      : 'relative flex h-full w-full min-h-0 flex-col overflow-y-auto border-t border-slate-700/50 bg-slate-900/95 p-5 backdrop-blur-xl xl:border-l xl:border-t-0',
    className,
  ].join(' ');

  if (!selectedNode) {
    return (
      <div className={panelClassName} style={style}>
        {onResizeStart && !isSheet && (
          <PanelResizeHandle
            orientation="vertical"
            edge="left"
            label="Resize properties panel width"
            onPointerDown={(event) => onResizeStart(event, 'horizontal')}
            className="hidden xl:flex"
          />
        )}

        <div className="mb-4 flex items-center gap-2">
          <Settings2 className="w-5 h-5 text-slate-400" />
          <h2 className="text-base font-semibold text-slate-200">Properties</h2>
        </div>
        <div className="flex flex-col items-center justify-center py-12 text-center">
          <div className="w-12 h-12 rounded-xl bg-slate-800 border border-slate-700/50 flex items-center justify-center mb-3">
            <Settings2 className="w-6 h-6 text-slate-500" />
          </div>
          <p className="text-sm text-slate-400">Select a resource to edit</p>
          <p className="text-xs text-slate-500 mt-1">Click a node on the canvas</p>
        </div>
      </div>
    );
  }

  const resourceType = selectedNode.data.type as ResourceType;
  const properties = selectedNode.data.properties || {};
  const fields = PROPERTY_FIELDS[resourceType] || [];

  const handleChange = (fieldName: string, value: any) => {
    const updatedProperties = {
      ...properties,
      [fieldName]: value
    };
    onPropertyChange(selectedNode.id, updatedProperties);
  };

  const handleCatalogSelect = (catalogItem: CatalogItem) => {
    const mappedProperties = mapCatalogToProperties(resourceType, catalogItem);
    const mergedProperties = {
      ...properties,
      ...mappedProperties
    };
    onPropertyChange(selectedNode.id, mergedProperties);
  };

  return (
    <div className={panelClassName} style={style}>
      {onResizeStart && (
        isSheet ? (
          <PanelResizeHandle
            orientation="horizontal"
            edge="top"
            label="Resize properties panel height"
            onPointerDown={(event) => onResizeStart(event, 'vertical')}
          />
        ) : (
          <PanelResizeHandle
            orientation="vertical"
            edge="left"
            label="Resize properties panel width"
            onPointerDown={(event) => onResizeStart(event, 'horizontal')}
            className="hidden xl:flex"
          />
        )
      )}

      <div className="mb-4 flex items-center justify-between gap-3">
        <div className="flex items-center gap-2">
          <Settings2 className="w-5 h-5 text-indigo-400" />
          <h2 className="text-base font-semibold text-slate-200">Properties</h2>
        </div>
        {onClose && (
          <button
            type="button"
            onClick={onClose}
            className="inline-flex items-center gap-2 rounded-lg border border-slate-700/70 bg-slate-800/80 px-3 py-2 text-xs font-semibold text-slate-200 transition hover:border-slate-600 hover:bg-slate-800"
            aria-label="Close properties panel"
          >
            <X className="h-4 w-4" />
            {isSheet ? 'Done' : 'Close'}
          </button>
        )}
      </div>

      <div className="mb-5 p-3 rounded-xl bg-slate-800/60 border border-slate-700/40">
        <p className="text-xs font-bold text-indigo-300 uppercase tracking-wider">
          {resourceType.replace(/_/g, ' ')}
        </p>
        <p className="text-[11px] text-slate-400 mt-1 font-mono truncate">{selectedNode.id}</p>
      </div>

      <div className="flex-1 space-y-4 overflow-y-auto">
        {/* Catalog Selector */}
        {CATALOG_SUPPORTED_TYPES.includes(resourceType) && (
          <CatalogSelector
            resourceType={resourceType}
            onSelect={handleCatalogSelect}
            filters={{ region: selectedRegion }}
          />
        )}

        {fields.length === 0 ? (
          <p className="text-sm text-slate-500 italic">No configurable properties</p>
        ) : (
          fields.map((field) => (
            <div key={field.name} className="space-y-1.5">
              <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-wider">
                {field.label}
                {field.required && <span className="text-rose-400 ml-1">*</span>}
              </label>

              {field.type === 'select' ? (
                <select
                  value={String(properties[field.name] || '')}
                  onChange={(e) => handleChange(field.name, e.target.value)}
                  className="w-full px-3 py-2 rounded-lg bg-slate-800 border border-slate-700/50 text-slate-200 text-sm focus:outline-none focus:ring-1 focus:ring-indigo-500 transition-all"
                >
                  <option value="">Select...</option>
                  {(field.options || []).map(opt => (
                    <option key={opt} value={opt}>{opt}</option>
                  ))}
                </select>
              ) : field.type === 'checkbox' ? (
                <div className="flex items-center gap-2 py-1">
                  <input
                    type="checkbox"
                    checked={!!properties[field.name]}
                    onChange={(e) => handleChange(field.name, e.target.checked)}
                    className="w-4 h-4 rounded bg-slate-800 border-slate-700 text-indigo-500 focus:ring-offset-slate-900 focus:ring-indigo-500"
                  />
                  <span className="text-xs text-slate-300">Enabled</span>
                </div>
              ) : field.type === 'textarea' ? (
                <textarea
                  value={String(properties[field.name] || '')}
                  onChange={(e) => handleChange(field.name, e.target.value)}
                  rows={3}
                  className="w-full px-3 py-2 rounded-lg bg-slate-800 border border-slate-700/50 text-slate-200 text-xs font-mono placeholder-slate-600 focus:outline-none focus:ring-1 focus:ring-indigo-500 transition-all"
                  placeholder={`Enter ${field.label.toLowerCase()}...`}
                />
              ) : (
                <input
                  type={field.type}
                  value={String(properties[field.name] || '')}
                  onChange={(e) => handleChange(field.name, e.target.value)}
                  className="w-full px-3 py-2 rounded-lg bg-slate-800 border border-slate-700/50 text-slate-200 text-sm placeholder-slate-600 focus:outline-none focus:ring-1 focus:ring-indigo-500 transition-all"
                  placeholder={`Enter ${field.label.toLowerCase()}...`}
                />
              )}
            </div>
          ))
        )}
      </div>

      <div className="mt-8 pt-5 border-t border-slate-700/50">
        <button
          onClick={() => onDeleteNode(selectedNode.id)}
          className="w-full flex items-center justify-center gap-2 py-2.5 rounded-xl bg-rose-500/10 hover:bg-rose-500/20 text-rose-400 border border-rose-500/20 hover:border-rose-500/40 transition-all font-semibold text-sm"
        >
          <Trash2 className="w-4 h-4" />
          Remove Resource
        </button>
      </div>
    </div>
  );
}

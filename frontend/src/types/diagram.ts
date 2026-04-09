export type ResourceType = 
  | 'VPC'
  | 'SUBNET'
  | 'EC2'
  | 'S3'
  | 'RDS'
  | 'INTERNET_GATEWAY'
  | 'LOAD_BALANCER'
  | 'LAMBDA'
  | 'DYNAMODB'
  | 'API_GATEWAY'
  | 'SQS'
  | 'SNS'
  | 'KINESIS'
  | 'CLOUDWATCH'
  | 'XRAY'
  | 'CLOUDTRAIL'
  | 'ECR'
  | 'ECS'
  | 'FARGATE'
  | 'ELASTICACHE'
  | 'EVENTBRIDGE';

export interface ResourceProperties {
  [key: string]: string | number | boolean | undefined;
}

export interface Position {
  x: number;
  y: number;
}

export interface DiagramNode {
  id: string;
  type: ResourceType;
  position: Position;
  data: ResourceProperties;
}

export interface DiagramEdge {
  id: string;
  source: string;
  target: string;
  type: string;
}

export interface Diagram {
  nodes: DiagramNode[];
  edges: DiagramEdge[];
  region?: string;
}

// Backend API types
export interface NodeDTO {
  id: string;
  type: string;
  properties: { [key: string]: string };
}

export interface EdgeDTO {
  id: string;
  source: string;
  target: string;
  type: string;
}

export interface DiagramDTO {
  nodes: NodeDTO[];
  edges: EdgeDTO[];
  region?: string;
}

export interface TerraformResponse {
  terraform: string;
  cloudformation: string;
}

export interface ErrorResponse {
  error: string;
  details?: string[];
}

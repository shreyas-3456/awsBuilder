package com.awsBuilder.builder.validation.engine;

import com.awsBuilder.builder.diagram.model.DiagramDTO;
import com.awsBuilder.builder.diagram.model.EdgeDTO;
import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.validation.model.ValidationResult;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ValidationEngine {
    
    private static final Set<String> SUPPORTED_RESOURCES = Set.of(
        "VPC", "SUBNET", "EC2", "S3", "RDS", 
        "INTERNET_GATEWAY", "LOAD_BALANCER",
        "LAMBDA", "DYNAMODB", "API_GATEWAY",
        "SQS", "SNS", "KINESIS",
        "CLOUDWATCH", "XRAY", "CLOUDTRAIL",
        "ECR", "ECS", "FARGATE", "ELASTICACHE",
        "EBS", "EFS", "EVENTBRIDGE"
    );
    
    private static final Map<String, Set<String>> VALID_RELATIONSHIPS;
    static {
        Map<String, Set<String>> map = new HashMap<>();
        map.put("SUBNET", Set.of("VPC", "EC2", "ECS", "FARGATE", "LOAD_BALANCER"));
        map.put("EC2", Set.of("SUBNET", "RDS", "ELASTICACHE", "EBS", "EFS"));
        map.put("RDS", Set.of("SUBNET"));
        map.put("INTERNET_GATEWAY", Set.of("VPC"));
        map.put("LOAD_BALANCER", Set.of("SUBNET", "EC2", "ECS", "FARGATE"));
        map.put("LAMBDA", Set.of("VPC", "SUBNET", "RDS", "DYNAMODB", "S3", "SQS", "SNS", "KINESIS", "ELASTICACHE", "EFS"));
        map.put("API_GATEWAY", Set.of("LAMBDA"));
        map.put("DYNAMODB", Set.of());
        map.put("SQS", Set.of());
        map.put("SNS", Set.of("SQS", "LAMBDA"));
        map.put("KINESIS", Set.of());
        map.put("CLOUDWATCH", Set.of("SNS", "EC2", "RDS", "LAMBDA", "SQS", "KINESIS", "ECS", "FARGATE"));
        map.put("XRAY", Set.of("LAMBDA", "API_GATEWAY", "EC2"));
        map.put("CLOUDTRAIL", Set.of("S3", "SNS", "CLOUDWATCH"));
        map.put("ECR", Set.of());
        map.put("ECS", Set.of("SUBNET", "ECR", "LOAD_BALANCER", "ELASTICACHE"));
        map.put("FARGATE", Set.of("ECS", "ECR", "SUBNET", "LOAD_BALANCER", "CLOUDWATCH", "ELASTICACHE"));
        map.put("ELASTICACHE", Set.of("SUBNET"));
        map.put("EBS", Set.of("EC2"));
        map.put("EFS", Set.of("SUBNET", "EC2"));
        map.put("EVENTBRIDGE", Set.of("LAMBDA", "SQS", "SNS", "KINESIS", "ECS", "FARGATE"));
        VALID_RELATIONSHIPS = Map.copyOf(map);
    }
    
    private static final Map<String, Set<String>> REQUIRED_PROPERTIES;
    static {
        Map<String, Set<String>> map = new HashMap<>();
        map.put("VPC", Set.of("cidr_block"));
        map.put("SUBNET", Set.of("cidr_block"));
        map.put("EC2", Set.of("ami", "instance_type"));
        map.put("S3", Set.of("bucket_name"));
        map.put("RDS", Set.of("engine", "instance_class", "allocated_storage", "db_name", "username", "password"));
        map.put("SQS", Set.of("queue_name"));
        map.put("SNS", Set.of("topic_name"));
        map.put("KINESIS", Set.of("stream_name", "shard_count"));
        map.put("CLOUDWATCH", Set.of("alarm_name", "metric_name"));
        map.put("XRAY", Set.of("rule_name", "priority", "fixed_rate"));
        map.put("CLOUDTRAIL", Set.of("trail_name", "s3_bucket_name"));
        map.put("ECR", Set.of("repository_name"));
        map.put("ECS", Set.of("cluster_name"));
        map.put("FARGATE", Set.of("family", "cpu", "memory", "container_name", "container_image"));
        map.put("ELASTICACHE", Set.of("cluster_id", "engine", "node_type"));
        map.put("EBS", Set.of("size_gb", "volume_type"));
        map.put("EFS", Set.of("creation_token"));
        map.put("EVENTBRIDGE", Set.of("rule_name"));
        REQUIRED_PROPERTIES = Map.copyOf(map);
    }
    
    public ValidationResult validate(DiagramDTO diagram) {
        List<String> errors = new ArrayList<>();
        
        if (diagram.getNodes() == null || diagram.getNodes().isEmpty()) {
            errors.add("Diagram must contain at least one node");
            return new ValidationResult(false, errors);
        }
        
        // Build a set of valid node IDs for edge validation
        Set<String> nodeIds = new HashSet<>();
        
        // Validate all node types are supported
        for (NodeDTO node : diagram.getNodes()) {
            if (node.getId() == null || node.getId().isBlank()) {
                errors.add("Node is missing a required id");
                continue;
            }
            
            if (!nodeIds.add(node.getId())) {
                errors.add("Duplicate node id: " + node.getId());
            }
            
            if (node.getType() == null || node.getType().isBlank()) {
                errors.add("Node " + node.getId() + " is missing a required type");
                continue;
            }
            
            if (!SUPPORTED_RESOURCES.contains(node.getType())) {
                errors.add("Unsupported resource type: " + node.getType());
            }
        }
        
        // Validate edge relationships
        if (diagram.getEdges() != null) {
            for (EdgeDTO edge : diagram.getEdges()) {
                NodeDTO target = findNode(diagram, edge.getTarget());
                NodeDTO source = findNode(diagram, edge.getSource());
                
                if (source == null) {
                    errors.add("Edge references non-existent source node: " + edge.getSource());
                    continue;
                }
                
                if (target == null) {
                    errors.add("Edge references non-existent target node: " + edge.getTarget());
                    continue;
                }
                
                if (!isValidRelationship(source.getType(), target.getType())) {
                    errors.add(String.format(
                        "Invalid relationship: %s cannot connect to %s. %s",
                        source.getType(), target.getType(), getConnectionHint(source.getType())
                    ));
                }
            }
        }
        
        // Validate required properties
        errors.addAll(validateRequiredProperties(diagram));
        
        // Validate required parents (no floating nodes that need parents)
        errors.addAll(validateRequiredParents(diagram));
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    private List<String> validateRequiredParents(DiagramDTO diagram) {
        List<String> errors = new ArrayList<>();
        if (diagram.getNodes() == null) return errors;
        
        // Track all connections for each node (both as source and target)
        Map<String, List<String>> nodeConnections = new HashMap<>();
        if (diagram.getEdges() != null) {
            for (EdgeDTO edge : diagram.getEdges()) {
                nodeConnections.computeIfAbsent(edge.getSource(), k -> new ArrayList<>()).add(edge.getTarget());
                nodeConnections.computeIfAbsent(edge.getTarget(), k -> new ArrayList<>()).add(edge.getSource());
            }
        }
        
        for (NodeDTO node : diagram.getNodes()) {
            if (node.getType() == null) continue;
            // Does this node type require a parent? 
            // If it's in VALID_RELATIONSHIPS and not VPC/S3 (which can be roots)
            Set<String> validParents = VALID_RELATIONSHIPS.get(node.getType());
            if (validParents != null && !validParents.isEmpty()) {
                List<String> connections = nodeConnections.get(node.getId());
                if (connections == null || connections.isEmpty()) {
                    errors.add(String.format("Resource %s of type %s must be connected to a parent. Valid parents for %s: %s",
                        node.getId(), node.getType(), node.getType(), String.join(", ", validParents)));
                }
            }
        }
        return errors;
    }
    
    private List<String> validateRequiredProperties(DiagramDTO diagram) {
        List<String> errors = new ArrayList<>();
        
        for (NodeDTO node : diagram.getNodes()) {
            if (node.getType() == null) continue;
            
            Set<String> requiredProps = REQUIRED_PROPERTIES.get(node.getType());
            if (requiredProps != null) {
                Map<String, String> properties = node.getProperties();
                
                if (properties == null) {
                    for (String prop : requiredProps) {
                        errors.add(String.format(
                            "Resource %s of type %s is missing required property: %s",
                            node.getId(), node.getType(), prop
                        ));
                    }
                    continue;
                }
                
                for (String prop : requiredProps) {
                    String value = properties.get(prop);
                    if (value == null || value.trim().isEmpty()) {
                        errors.add(String.format(
                            "Resource %s of type %s is missing required property: %s",
                            node.getId(), node.getType(), prop
                        ));
                    }
                }
            }
        }
        
        return errors;
    }
    
    private boolean isValidRelationship(String sourceType, String targetType) {
        if (sourceType == null || targetType == null) return false;
        Set<String> validParents = VALID_RELATIONSHIPS.get(sourceType);
        return validParents != null && validParents.contains(targetType);
    }
    
    private String getConnectionHint(String sourceType) {
        Set<String> validTargets = VALID_RELATIONSHIPS.get(sourceType);
        if (validTargets == null || validTargets.isEmpty()) {
            return sourceType + " is a standalone resource and cannot be a connection source";
        }
        return sourceType + " can connect to: " + String.join(", ", validTargets);
    }
    
    private NodeDTO findNode(DiagramDTO diagram, String nodeId) {
        return diagram.getNodes().stream()
                .filter(n -> n.getId().equals(nodeId))
                .findFirst()
                .orElse(null);
    }
}

package com.awsBuilder.builder.validation.engine;

import com.awsBuilder.builder.validation.engine.ValidationEngine;
import com.awsBuilder.builder.diagram.model.DiagramDTO;
import com.awsBuilder.builder.diagram.model.EdgeDTO;
import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.validation.model.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ValidationEngineTest {

    private ValidationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new ValidationEngine();
    }

    // --- Supported Resource Types ---

    @Test
    @DisplayName("Valid diagram with single VPC node passes validation")
    void validSingleVpcPasses() {
        DiagramDTO diagram = diagramWithNodes(
            node("vpc1", "VPC", Map.of("cidr_block", "10.0.0.0/16"))
        );
        ValidationResult result = engine.validate(diagram);
        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Unsupported resource type is rejected")
    void unsupportedTypeFails() {
        DiagramDTO diagram = diagramWithNodes(
            node("x", "CLOUDFRONT", Map.of())
        );
        ValidationResult result = engine.validate(diagram);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Unsupported resource type: CLOUDFRONT")));
    }

    @Test
    @DisplayName("All supported resource types pass individual validation")
    void allSupportedTypesPass() {
        for (String type : List.of("VPC", "SUBNET", "EC2", "S3", "RDS", "INTERNET_GATEWAY", "LOAD_BALANCER", "ELASTICACHE")) {
            DiagramDTO diagram = diagramWithNodes(
                node("n1", type, buildRequiredProps(type))
            );
            ValidationResult result = engine.validate(diagram);
            // May have relationship errors, but should not have "unsupported" errors
            assertTrue(result.getErrors().stream().noneMatch(e -> e.contains("Unsupported resource type")),
                "Type " + type + " should be supported");
        }
    }

    // --- Edge Relationship Validation ---

    @Test
    @DisplayName("Valid SUBNET->VPC relationship passes")
    void validSubnetVpcRelationship() {
        DiagramDTO diagram = new DiagramDTO(
            List.of(
                node("vpc1", "VPC", Map.of("cidr_block", "10.0.0.0/16")),
                node("subnet1", "SUBNET", Map.of("cidr_block", "10.0.1.0/24"))
            ),
            List.of(new EdgeDTO("e1", "subnet1", "vpc1", "connects")),
            "us-east-1"
        );
        ValidationResult result = engine.validate(diagram);
        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("Valid EC2->RDS relationship passes")
    void validEc2RdsRelationship() {
        DiagramDTO diagram = new DiagramDTO(
            List.of(
                node("vpc1", "VPC", buildRequiredProps("VPC")),
                node("sub1", "SUBNET", buildRequiredProps("SUBNET")),
                node("ec2_1", "EC2", buildRequiredProps("EC2")),
                node("rds_1", "RDS", buildRequiredProps("RDS"))
            ),
            List.of(
                new EdgeDTO("e1", "sub1", "vpc1", "connects"),
                new EdgeDTO("e2", "ec2_1", "sub1", "connects"),
                new EdgeDTO("e3", "rds_1", "sub1", "connects"),
                new EdgeDTO("e4", "ec2_1", "rds_1", "connects")
            ),
            "us-east-1"
        );
        ValidationResult result = engine.validate(diagram);
        assertTrue(result.isValid(), "EC2 to RDS relationship should be valid, but got: " + result.getErrors());
    }

    @Test
    @DisplayName("Valid EC2->ElastiCache relationship passes")
    void validEc2ElasticacheRelationship() {
        DiagramDTO diagram = new DiagramDTO(
            List.of(
                node("vpc1", "VPC", buildRequiredProps("VPC")),
                node("sub1", "SUBNET", buildRequiredProps("SUBNET")),
                node("ec2_1", "EC2", buildRequiredProps("EC2")),
                node("cache_1", "ELASTICACHE", buildRequiredProps("ELASTICACHE"))
            ),
            List.of(
                new EdgeDTO("e1", "sub1", "vpc1", "connects"),
                new EdgeDTO("e2", "ec2_1", "sub1", "connects"),
                new EdgeDTO("e3", "cache_1", "sub1", "connects"),
                new EdgeDTO("e4", "ec2_1", "cache_1", "connects")
            ),
            "us-east-1"
        );
        ValidationResult result = engine.validate(diagram);
        assertTrue(result.isValid(), "EC2 to ElastiCache relationship should be valid, but got: " + result.getErrors());
    }

    @Test
    @DisplayName("Invalid relationship is rejected")
    void invalidRelationshipFails() {
        DiagramDTO diagram = new DiagramDTO(
            List.of(
                node("ec2_1", "EC2", Map.of()),
                node("vpc1", "VPC", Map.of())
            ),
            List.of(new EdgeDTO("e1", "ec2_1", "vpc1", "connects")),
            "us-east-1"
        );
        ValidationResult result = engine.validate(diagram);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Invalid relationship")));
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("EC2 can connect to:")),
            "Error should include a connection hint");
    }

    @Test
    @DisplayName("Edge referencing non-existent source node is rejected")
    void missingSourceNodeFails() {
        DiagramDTO diagram = new DiagramDTO(
            List.of(node("vpc1", "VPC", Map.of())),
            List.of(new EdgeDTO("e1", "missing", "vpc1", "connects")),
            "us-east-1"
        );
        ValidationResult result = engine.validate(diagram);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("non-existent source")));
    }

    @Test
    @DisplayName("Edge referencing non-existent target node is rejected")
    void missingTargetNodeFails() {
        DiagramDTO diagram = new DiagramDTO(
            List.of(node("vpc1", "VPC", Map.of())),
            List.of(new EdgeDTO("e1", "vpc1", "missing", "connects")),
            "us-east-1"
        );
        ValidationResult result = engine.validate(diagram);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("non-existent target")));
    }

    // --- Required Properties Validation ---

    @Test
    @DisplayName("S3 node missing bucket_name is rejected")
    void s3MissingBucketNameFails() {
        DiagramDTO diagram = diagramWithNodes(
            node("s3_1", "S3", Map.of())
        );
        ValidationResult result = engine.validate(diagram);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("bucket_name")));
    }

    @Test
    @DisplayName("S3 node with bucket_name passes required property check")
    void s3WithBucketNamePasses() {
        DiagramDTO diagram = diagramWithNodes(
            node("s3_1", "S3", Map.of("bucket_name", "my-bucket"))
        );
        ValidationResult result = engine.validate(diagram);
        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("RDS node missing required properties is rejected")
    void rdsMissingPropertiesFails() {
        DiagramDTO diagram = diagramWithNodes(
            node("rds_1", "RDS", Map.of())
        );
        ValidationResult result = engine.validate(diagram);
        assertFalse(result.isValid());
        // Should list all 6 missing properties
        long missingCount = result.getErrors().stream()
            .filter(e -> e.contains("missing required property"))
            .count();
        assertEquals(6, missingCount);
    }

    // --- Null Safety ---

    @Test
    @DisplayName("Null properties map does not cause NPE")
    void nullPropertiesDoesNotThrow() {
        NodeDTO node = new NodeDTO();
        node.setId("s3_1");
        node.setType("S3");
        node.setProperties(null);
        
        DiagramDTO diagram = diagramWithNodes(node);
        assertDoesNotThrow(() -> engine.validate(diagram));
        
        ValidationResult result = engine.validate(diagram);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("bucket_name")));
    }

    @Test
    @DisplayName("Empty diagram is rejected")
    void emptyDiagramFails() {
        DiagramDTO diagram = new DiagramDTO(List.of(), List.of(), "us-east-1");
        ValidationResult result = engine.validate(diagram);
        assertFalse(result.isValid());
    }

    @Test
    @DisplayName("Duplicate node IDs are detected")
    void duplicateNodeIdsFail() {
        DiagramDTO diagram = new DiagramDTO(
            List.of(
                node("vpc1", "VPC", Map.of()),
                node("vpc1", "VPC", Map.of())
            ),
            List.of(),
            "us-east-1"
        );
        ValidationResult result = engine.validate(diagram);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Duplicate node id")));
    }

    // --- Helpers ---

    private NodeDTO node(String id, String type, Map<String, String> properties) {
        NodeDTO n = new NodeDTO();
        n.setId(id);
        n.setType(type);
        n.setProperties(new HashMap<>(properties));
        return n;
    }

    private DiagramDTO diagramWithNodes(NodeDTO... nodes) {
        return new DiagramDTO(List.of(nodes), List.of(), "us-east-1");
    }

    private Map<String, String> buildRequiredProps(String type) {
        return switch (type) {
            case "VPC" -> Map.of("cidr_block", "10.0.0.0/16");
            case "SUBNET" -> Map.of("cidr_block", "10.0.1.0/24");
            case "EC2" -> Map.of("ami", "ami-12345", "instance_type", "t3.micro");
            case "S3" -> Map.of("bucket_name", "test-bucket");
            case "RDS" -> Map.of(
                "engine", "mysql",
                "instance_class", "db.t3.micro",
                "allocated_storage", "20",
                "db_name", "testdb",
                "username", "admin",
                "password", "secret"
            );
            case "ELASTICACHE" -> Map.of(
                "cluster_id", "cache-cluster",
                "engine", "redis",
                "node_type", "cache.t3.micro"
            );
            default -> Map.of();
        };
    }
}

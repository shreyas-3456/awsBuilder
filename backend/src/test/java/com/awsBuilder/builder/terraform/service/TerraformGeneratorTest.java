package com.awsBuilder.builder.terraform.service;

import com.awsBuilder.builder.terraform.service.TerraformGenerator;
import com.awsBuilder.builder.terraform.model.TerraformResource;
import java.util.List;
import com.awsBuilder.builder.terraform.service.ResourceTemplateRegistry;
import com.awsBuilder.builder.diagram.model.DiagramDTO;
import com.awsBuilder.builder.diagram.model.EdgeDTO;
import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;
import com.awsBuilder.builder.validation.engine.DependencyResolver;
import com.awsBuilder.builder.terraform.templates.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class TerraformGeneratorTest {

    private TerraformGenerator generator;
    private DependencyResolver resolver;

    @BeforeEach
    void setUp() {
        List<TerraformResource> templates = List.of(
            new VpcResourceTemplate(),
            new SubnetResourceTemplate(),
            new Ec2ResourceTemplate(),
            new S3ResourceTemplate(),
            new RdsResourceTemplate(),
            new IgwResourceTemplate(),
            new LbResourceTemplate()
        );
        ResourceTemplateRegistry registry = new ResourceTemplateRegistry(templates);
        generator = new TerraformGenerator(registry);
        resolver = new DependencyResolver();
    }

    @Test
    @DisplayName("VPC generates valid Terraform HCL")
    void vpcGeneratesCorrectHcl() {
        DiagramDTO diagram = simpleDiagram("vpc1", "VPC", Map.of("cidr_block", "10.0.0.0/16"));
        String terraform = generateTerraform(diagram);
        
        assertTrue(terraform.contains("resource \"aws_vpc\" \"vpc1\""));
        assertTrue(terraform.contains("cidr_block"));
        assertTrue(terraform.contains("10.0.0.0/16"));
        assertTrue(terraform.contains("enable_dns_support"));
        assertTrue(terraform.contains("enable_dns_hostnames"));
        assertTrue(terraform.contains("provider \"aws\""));
    }

    @Test
    @DisplayName("VPC with custom name uses getDisplayName()")
    void vpcWithNameUsesDisplayName() {
        NodeDTO vpc = node("vpc1", "VPC", Map.of("cidr_block", "10.0.0.0/16"));
        vpc.setName("My Production VPC");
        DiagramDTO diagram = new DiagramDTO(List.of(vpc), List.of(), "us-east-1");
        
        String terraform = generateTerraform(diagram);
        assertTrue(terraform.contains("My Production VPC"));
    }

    @Test
    @DisplayName("Subnet references parent VPC in generated HCL")
    void subnetReferencesVpc() {
        DiagramDTO diagram = new DiagramDTO(
            List.of(
                node("vpc1", "VPC", Map.of()),
                node("subnet1", "SUBNET", Map.of("cidr_block", "10.0.1.0/24"))
            ),
            List.of(new EdgeDTO("e1", "subnet1", "vpc1", "connects")),
            "us-east-1"
        );
        
        String terraform = generateTerraform(diagram);
        assertTrue(terraform.contains("resource \"aws_subnet\" \"subnet1\""));
        assertTrue(terraform.contains("vpc_id            = aws_vpc.vpc1.id"));
    }

    @Test
    @DisplayName("EC2 references parent Subnet in generated HCL")
    void ec2ReferencesSubnet() {
        DiagramDTO diagram = new DiagramDTO(
            List.of(
                node("vpc1", "VPC", Map.of()),
                node("subnet1", "SUBNET", Map.of()),
                node("ec2_1", "EC2", Map.of("ami", "ami-12345", "instance_type", "t3.medium"))
            ),
            List.of(
                new EdgeDTO("e1", "subnet1", "vpc1", "connects"),
                new EdgeDTO("e2", "ec2_1", "subnet1", "connects")
            ),
            "us-east-1"
        );
        
        String terraform = generateTerraform(diagram);
        assertTrue(terraform.contains("resource \"aws_instance\" \"ec2_1\""));
        assertTrue(terraform.contains("subnet_id     = aws_subnet.subnet1.id"));
        assertTrue(terraform.contains("ami-12345"));
        assertTrue(terraform.contains("t3.medium"));
    }

    @Test
    @DisplayName("S3 bucket generates with versioning when enabled")
    void s3WithVersioning() {
        DiagramDTO diagram = simpleDiagram("s3_1", "S3",
            Map.of("bucket_name", "my-test-bucket", "versioning", "true"));
        
        String terraform = generateTerraform(diagram);
        assertTrue(terraform.contains("resource \"aws_s3_bucket\" \"s3_1\""));
        assertTrue(terraform.contains("my-test-bucket"));
        assertTrue(terraform.contains("aws_s3_bucket_versioning"));
        assertTrue(terraform.contains("Enabled"));
    }

    @Test
    @DisplayName("S3 generates ACL resource when acl property is set")
    void s3WithAcl() {
        DiagramDTO diagram = simpleDiagram("s3_1", "S3",
            Map.of("bucket_name", "my-bucket", "acl", "private"));
        
        String terraform = generateTerraform(diagram);
        assertTrue(terraform.contains("aws_s3_bucket_acl"));
        assertTrue(terraform.contains("private"));
    }

    @Test
    @DisplayName("RDS generates subnet group and db instance")
    void rdsGeneratesSubnetGroupAndInstance() {
        DiagramDTO diagram = new DiagramDTO(
            List.of(
                node("vpc1", "VPC", Map.of()),
                node("subnet1", "SUBNET", Map.of()),
                node("rds1", "RDS", Map.of(
                    "engine", "mysql",
                    "instance_class", "db.t3.micro",
                    "allocated_storage", "20",
                    "db_name", "mydb",
                    "username", "admin",
                    "password", "secret"
                ))
            ),
            List.of(
                new EdgeDTO("e1", "subnet1", "vpc1", "connects"),
                new EdgeDTO("e2", "rds1", "subnet1", "connects")
            ),
            "us-east-1"
        );
        
        String terraform = generateTerraform(diagram);
        assertTrue(terraform.contains("aws_db_subnet_group"));
        assertTrue(terraform.contains("aws_db_instance"));
        assertTrue(terraform.contains("mysql"));
        assertTrue(terraform.contains("db.t3.micro"));
    }

    @Test
    @DisplayName("Internet Gateway references parent VPC")
    void igwReferencesVpc() {
        DiagramDTO diagram = new DiagramDTO(
            List.of(
                node("vpc1", "VPC", Map.of()),
                node("igw1", "INTERNET_GATEWAY", Map.of())
            ),
            List.of(new EdgeDTO("e1", "igw1", "vpc1", "connects")),
            "us-east-1"
        );
        
        String terraform = generateTerraform(diagram);
        assertTrue(terraform.contains("resource \"aws_internet_gateway\" \"igw1\""));
        assertTrue(terraform.contains("vpc_id = aws_vpc.vpc1.id"));
    }

    @Test
    @DisplayName("Generated Terraform always starts with provider block")
    void alwaysStartsWithProvider() {
        DiagramDTO diagram = simpleDiagram("vpc1", "VPC", Map.of());
        String terraform = generateTerraform(diagram);
        
        int providerIndex = terraform.indexOf("provider");
        int resourceIndex = terraform.indexOf("resource");
        assertTrue(providerIndex < resourceIndex, "Provider should come before resources");
    }

    // --- Helpers ---

    private String generateTerraform(DiagramDTO diagram) {
        ResourceGraph graph = resolver.buildGraph(diagram);
        List<NodeDTO> ordered = resolver.topologicalSort(graph);
        return generator.generate(ordered, graph, "us-east-1");
    }

    private DiagramDTO simpleDiagram(String id, String type, Map<String, String> props) {
        return new DiagramDTO(List.of(node(id, type, props)), List.of(), "us-east-1");
    }

    private NodeDTO node(String id, String type, Map<String, String> properties) {
        NodeDTO n = new NodeDTO();
        n.setId(id);
        n.setType(type);
        n.setProperties(new HashMap<>(properties));
        return n;
    }
}

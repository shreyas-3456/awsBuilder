package com.awsBuilder.builder.cloudformation.templates;

import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SubnetCfnTemplateTest {
    
    private final SubnetCfnTemplate template = new SubnetCfnTemplate();
    
    @Test
    void testGetResourceType() {
        assertEquals("SUBNET", template.getResourceType());
    }
    
    @Test
    void testGenerateSubnetFragment() {
        // Create a VPC node
        NodeDTO vpcNode = new NodeDTO();
        vpcNode.setId("MyVpc");
        vpcNode.setType("VPC");
        vpcNode.setName("My VPC");
        vpcNode.setProperties(new HashMap<>());
        
        // Create a Subnet node
        NodeDTO subnetNode = new NodeDTO();
        subnetNode.setId("MySubnet");
        subnetNode.setType("SUBNET");
        subnetNode.setName("My Subnet");
        Map<String, String> props = new HashMap<>();
        props.put("cidr_block", "10.0.1.0/24");
        props.put("availability_zone", "us-east-1a");
        subnetNode.setProperties(props);
        
        ResourceGraph graph = new ResourceGraph();
        graph.addNode(vpcNode);
        graph.addNode(subnetNode);
        graph.addEdge(subnetNode.getId(), vpcNode.getId()); // Subnet depends on VPC
        
        // Generate the fragment
        String yamlFragment = template.generate(subnetNode, graph);
        
        // Parse the YAML to verify structure
        Yaml yaml = new Yaml();
        Map<String, Object> parsed = yaml.load(yamlFragment);
        
        // Verify the logical resource ID is the node ID
        assertTrue(parsed.containsKey("MySubnet"));
        
        Map<String, Object> resource = (Map<String, Object>) parsed.get("MySubnet");
        assertEquals("AWS::EC2::Subnet", resource.get("Type"));
        
        Map<String, Object> properties = (Map<String, Object>) resource.get("Properties");
        assertEquals("10.0.1.0/24", properties.get("CidrBlock"));
        assertEquals("us-east-1a", properties.get("AvailabilityZone"));
        assertEquals("!Ref MyVpc", properties.get("VpcId"));
    }
    
    @Test
    void testSubnetMissingVpcParentThrows() {
        NodeDTO subnetNode = new NodeDTO();
        subnetNode.setId("OrphanSubnet");
        subnetNode.setType("SUBNET");
        subnetNode.setName("Orphan Subnet");
        subnetNode.setProperties(new HashMap<>());
        
        ResourceGraph graph = new ResourceGraph();
        graph.addNode(subnetNode);
        
        // Should throw IllegalStateException because no VPC parent
        assertThrows(IllegalStateException.class, () -> template.generate(subnetNode, graph));
    }
    
    @Test
    void testGenerateSubnetWithDefaultCidr() {
        NodeDTO vpcNode = new NodeDTO();
        vpcNode.setId("MyVpc");
        vpcNode.setType("VPC");
        vpcNode.setName("My VPC");
        vpcNode.setProperties(new HashMap<>());
        
        NodeDTO subnetNode = new NodeDTO();
        subnetNode.setId("DefaultSubnet");
        subnetNode.setType("SUBNET");
        subnetNode.setName("Default Subnet");
        subnetNode.setProperties(new HashMap<>());
        
        ResourceGraph graph = new ResourceGraph();
        graph.addNode(vpcNode);
        graph.addNode(subnetNode);
        graph.addEdge(subnetNode.getId(), vpcNode.getId());
        
        String yamlFragment = template.generate(subnetNode, graph);
        
        Yaml yaml = new Yaml();
        Map<String, Object> parsed = yaml.load(yamlFragment);
        Map<String, Object> resource = (Map<String, Object>) parsed.get("DefaultSubnet");
        Map<String, Object> properties = (Map<String, Object>) resource.get("Properties");
        
        // Should use default CIDR block
        assertEquals("10.0.1.0/24", properties.get("CidrBlock"));
    }
}

package com.awsBuilder.builder.cloudformation.templates;

import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class VpcCfnTemplateTest {
    
    private final VpcCfnTemplate template = new VpcCfnTemplate();
    
    @Test
    void testGetResourceType() {
        assertEquals("VPC", template.getResourceType());
    }
    
    @Test
    void testGenerateVpcFragment() {
        // Create a VPC node
        NodeDTO vpcNode = new NodeDTO();
        vpcNode.setId("MyVpc");
        vpcNode.setType("VPC");
        vpcNode.setName("My VPC");
        Map<String, String> props = new HashMap<>();
        props.put("cidr_block", "10.0.0.0/16");
        vpcNode.setProperties(props);
        
        ResourceGraph graph = new ResourceGraph();
        graph.addNode(vpcNode);
        
        // Generate the fragment
        String yamlFragment = template.generate(vpcNode, graph);
        
        // Parse the YAML to verify structure
        Yaml yaml = new Yaml();
        Map<String, Object> parsed = yaml.load(yamlFragment);
        
        // Verify the logical resource ID is the node ID
        assertTrue(parsed.containsKey("MyVpc"));
        
        Map<String, Object> resource = (Map<String, Object>) parsed.get("MyVpc");
        assertEquals("AWS::EC2::VPC", resource.get("Type"));
        
        Map<String, Object> properties = (Map<String, Object>) resource.get("Properties");
        assertEquals("10.0.0.0/16", properties.get("CidrBlock"));
        assertEquals(true, properties.get("EnableDnsSupport"));
        assertEquals(true, properties.get("EnableDnsHostnames"));
    }
    
    @Test
    void testGenerateVpcWithDefaultCidr() {
        NodeDTO vpcNode = new NodeDTO();
        vpcNode.setId("DefaultVpc");
        vpcNode.setType("VPC");
        vpcNode.setName("Default VPC");
        vpcNode.setProperties(new HashMap<>());
        
        ResourceGraph graph = new ResourceGraph();
        graph.addNode(vpcNode);
        
        String yamlFragment = template.generate(vpcNode, graph);
        
        Yaml yaml = new Yaml();
        Map<String, Object> parsed = yaml.load(yamlFragment);
        Map<String, Object> resource = (Map<String, Object>) parsed.get("DefaultVpc");
        Map<String, Object> properties = (Map<String, Object>) resource.get("Properties");
        
        // Should use default CIDR block
        assertEquals("10.0.0.0/16", properties.get("CidrBlock"));
    }
}

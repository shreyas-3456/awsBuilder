package com.awsBuilder.builder.cloudformation.templates;

import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class IgwCfnTemplateTest {
    
    private final IgwCfnTemplate template = new IgwCfnTemplate();
    
    @Test
    void testGetResourceType() {
        assertEquals("INTERNET_GATEWAY", template.getResourceType());
    }
    
    @Test
    void testGenerateIgwFragment() {
        // Create a VPC node
        NodeDTO vpcNode = new NodeDTO();
        vpcNode.setId("MyVpc");
        vpcNode.setType("VPC");
        vpcNode.setName("My VPC");
        vpcNode.setProperties(new HashMap<>());
        
        // Create an IGW node
        NodeDTO igwNode = new NodeDTO();
        igwNode.setId("MyIgw");
        igwNode.setType("INTERNET_GATEWAY");
        igwNode.setName("My IGW");
        igwNode.setProperties(new HashMap<>());
        
        ResourceGraph graph = new ResourceGraph();
        graph.addNode(vpcNode);
        graph.addNode(igwNode);
        graph.addEdge(igwNode.getId(), vpcNode.getId()); // IGW depends on VPC
        
        // Generate the fragment
        String yamlFragment = template.generate(igwNode, graph);
        
        // Parse the YAML to verify structure
        Yaml yaml = new Yaml();
        Map<String, Object> parsed = yaml.load(yamlFragment);
        
        // Verify both resources are present
        assertTrue(parsed.containsKey("MyIgw"), "Should contain IGW resource");
        assertTrue(parsed.containsKey("MyIgwAttachment"), "Should contain attachment resource");
        
        // Verify IGW resource
        Map<String, Object> igwResource = (Map<String, Object>) parsed.get("MyIgw");
        assertEquals("AWS::EC2::InternetGateway", igwResource.get("Type"));
        
        // Verify VPCGatewayAttachment resource
        Map<String, Object> attachmentResource = (Map<String, Object>) parsed.get("MyIgwAttachment");
        assertEquals("AWS::EC2::VPCGatewayAttachment", attachmentResource.get("Type"));
        
        Map<String, Object> attachmentProps = (Map<String, Object>) attachmentResource.get("Properties");
        assertEquals("!Ref MyIgw", attachmentProps.get("InternetGatewayId"));
        assertEquals("!Ref MyVpc", attachmentProps.get("VpcId"));
    }
    
    @Test
    void testIgwMissingVpcParentThrows() {
        NodeDTO igwNode = new NodeDTO();
        igwNode.setId("OrphanIgw");
        igwNode.setType("INTERNET_GATEWAY");
        igwNode.setName("Orphan IGW");
        igwNode.setProperties(new HashMap<>());
        
        ResourceGraph graph = new ResourceGraph();
        graph.addNode(igwNode);
        
        // Should throw IllegalStateException because no VPC parent
        assertThrows(IllegalStateException.class, () -> template.generate(igwNode, graph));
    }
}

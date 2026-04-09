package com.awsBuilder.builder.cloudformation.templates;

import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LbCfnTemplateTest {
    
    private final LbCfnTemplate template = new LbCfnTemplate();
    
    @Test
    void testGetResourceType() {
        assertEquals("LOAD_BALANCER", template.getResourceType());
    }
    
    @Test
    void testGenerateLbFragment() {
        // Create Subnet nodes
        NodeDTO subnet1 = new NodeDTO();
        subnet1.setId("Subnet1");
        subnet1.setType("SUBNET");
        subnet1.setName("Subnet 1");
        subnet1.setProperties(new HashMap<>());
        
        NodeDTO subnet2 = new NodeDTO();
        subnet2.setId("Subnet2");
        subnet2.setType("SUBNET");
        subnet2.setName("Subnet 2");
        subnet2.setProperties(new HashMap<>());
        
        // Create a Load Balancer node
        NodeDTO lbNode = new NodeDTO();
        lbNode.setId("MyLb");
        lbNode.setType("LOAD_BALANCER");
        lbNode.setName("My Load Balancer");
        Map<String, String> props = new HashMap<>();
        props.put("type", "application");
        props.put("scheme", "internet-facing");
        lbNode.setProperties(props);
        
        ResourceGraph graph = new ResourceGraph();
        graph.addNode(subnet1);
        graph.addNode(subnet2);
        graph.addNode(lbNode);
        graph.addEdge(lbNode.getId(), subnet1.getId()); // LB depends on Subnet1
        graph.addEdge(lbNode.getId(), subnet2.getId()); // LB depends on Subnet2
        
        // Generate the fragment
        String yamlFragment = template.generate(lbNode, graph);
        
        // Parse the YAML to verify structure
        Yaml yaml = new Yaml();
        Map<String, Object> parsed = yaml.load(yamlFragment);
        
        // Verify the logical resource ID is the node ID
        assertTrue(parsed.containsKey("MyLb"));
        
        Map<String, Object> resource = (Map<String, Object>) parsed.get("MyLb");
        assertEquals("AWS::ElasticLoadBalancingV2::LoadBalancer", resource.get("Type"));
        
        Map<String, Object> properties = (Map<String, Object>) resource.get("Properties");
        assertEquals("application", properties.get("Type"));
        assertEquals("internet-facing", properties.get("Scheme"));
        
        // Verify Subnets list contains references to both subnets
        List<String> subnets = (List<String>) properties.get("Subnets");
        assertEquals(2, subnets.size());
        assertTrue(subnets.contains("!Ref Subnet1"));
        assertTrue(subnets.contains("!Ref Subnet2"));
    }
    
    @Test
    void testLbMissingSubnetParentThrows() {
        NodeDTO lbNode = new NodeDTO();
        lbNode.setId("OrphanLb");
        lbNode.setType("LOAD_BALANCER");
        lbNode.setName("Orphan LB");
        lbNode.setProperties(new HashMap<>());
        
        ResourceGraph graph = new ResourceGraph();
        graph.addNode(lbNode);
        
        // Should throw IllegalStateException because no Subnet parent
        assertThrows(IllegalStateException.class, () -> template.generate(lbNode, graph));
    }
    
    @Test
    void testGenerateLbWithDefaultProperties() {
        NodeDTO subnet = new NodeDTO();
        subnet.setId("DefaultSubnet");
        subnet.setType("SUBNET");
        subnet.setName("Default Subnet");
        subnet.setProperties(new HashMap<>());
        
        NodeDTO lbNode = new NodeDTO();
        lbNode.setId("DefaultLb");
        lbNode.setType("LOAD_BALANCER");
        lbNode.setName("Default LB");
        lbNode.setProperties(new HashMap<>());
        
        ResourceGraph graph = new ResourceGraph();
        graph.addNode(subnet);
        graph.addNode(lbNode);
        graph.addEdge(lbNode.getId(), subnet.getId());
        
        String yamlFragment = template.generate(lbNode, graph);
        
        Yaml yaml = new Yaml();
        Map<String, Object> parsed = yaml.load(yamlFragment);
        Map<String, Object> resource = (Map<String, Object>) parsed.get("DefaultLb");
        Map<String, Object> properties = (Map<String, Object>) resource.get("Properties");
        
        // Should use default values
        assertEquals("application", properties.get("Type"));
        assertEquals("internet-facing", properties.get("Scheme"));
        
        List<String> subnets = (List<String>) properties.get("Subnets");
        assertEquals(1, subnets.size());
        assertEquals("!Ref DefaultSubnet", subnets.get(0));
    }
}

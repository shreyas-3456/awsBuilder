package com.awsBuilder.builder.cloudformation.templates;

import com.awsBuilder.builder.cloudformation.model.CloudFormationResource;
import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class LbCfnTemplate implements CloudFormationResource {
    
    @Override
    public String generate(NodeDTO node, ResourceGraph graph) {
        // Find all parent SUBNET nodes from graph
        List<NodeDTO> parents = graph.getParents(node.getId());
        List<NodeDTO> subnetNodes = parents.stream()
            .filter(n -> "SUBNET".equals(n.getType()))
            .toList();
        
        if (subnetNodes.isEmpty()) {
            throw new IllegalStateException(
                "Load Balancer must be connected to at least one Subnet"
            );
        }
        
        String type = node.getProperties().getOrDefault("type", "application");
        String scheme = node.getProperties().getOrDefault("scheme", "internet-facing");
        
        // Build the Load Balancer resource fragment
        Map<String, Object> lbResource = new LinkedHashMap<>();
        lbResource.put("Type", "AWS::ElasticLoadBalancingV2::LoadBalancer");
        
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("Type", type);
        properties.put("Scheme", scheme);
        
        List<String> subnets = new ArrayList<>();
        for (NodeDTO subnet : subnetNodes) {
            subnets.add("!Ref " + subnet.getId());
        }
        properties.put("Subnets", subnets);
        
        lbResource.put("Properties", properties);
        
        // Create a map with the logical resource ID as the key
        Map<String, Object> fragment = new LinkedHashMap<>();
        fragment.put(node.getId(), lbResource);
        
        // Serialize to YAML using BLOCK style
        DumperOptions opts = new DumperOptions();
        opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        opts.setPrettyFlow(true);
        Yaml yaml = new Yaml(opts);
        
        return yaml.dump(fragment);
    }
    
    @Override
    public String getResourceType() {
        return "LOAD_BALANCER";
    }
}

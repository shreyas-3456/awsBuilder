package com.awsBuilder.builder.terraform.templates;

import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;
import com.awsBuilder.builder.terraform.model.TerraformResource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LbResourceTemplate implements TerraformResource {
    
    @Override
    public String generate(NodeDTO node, ResourceGraph graph, String providerAlias) {
        String loadBalancerType = node.getProperties().getOrDefault("load_balancer_type", "application");
        String internal = node.getProperties().getOrDefault("internal", "false");
        
        // Find parent Subnets from graph
        List<NodeDTO> parents = graph.getParents(node.getId());
        List<NodeDTO> subnetNodes = parents.stream()
            .filter(n -> "SUBNET".equals(n.getType()))
            .collect(Collectors.toList());
        
        if (subnetNodes.isEmpty()) {
            throw new IllegalStateException(
                "Load Balancer must be connected to at least one Subnet"
            );
        }
        
        String subnetIds = subnetNodes.stream()
            .map(s -> "aws_subnet." + s.getId() + ".id")
            .collect(Collectors.joining(", "));
            
        String providerLine = (providerAlias != null && !providerAlias.isEmpty()) 
            ? String.format("  provider = aws.%s\n", providerAlias) 
            : "";
        
        return String.format("""
            resource "aws_lb" "%s" {
            %s  name               = "%s"
              internal           = %s
              load_balancer_type = "%s"
              subnets            = [%s]
              
              tags = {
                Name = "%s"
              }
            }
            """, node.getId(), providerLine, node.getId(), internal, loadBalancerType, subnetIds, node.getDisplayName());
    }
    
    @Override
    public String getResourceType() {
        return "LOAD_BALANCER";
    }
}

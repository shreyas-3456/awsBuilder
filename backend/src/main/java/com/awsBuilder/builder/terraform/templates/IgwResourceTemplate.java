package com.awsBuilder.builder.terraform.templates;

import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;
import com.awsBuilder.builder.terraform.model.TerraformResource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IgwResourceTemplate implements TerraformResource {
    
    @Override
    public String generate(NodeDTO node, ResourceGraph graph, String providerAlias) {
        // Find parent VPC from graph
        List<NodeDTO> parents = graph.getParents(node.getId());
        NodeDTO vpcNode = parents.stream()
            .filter(n -> "VPC".equals(n.getType()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "Internet Gateway must be connected to a VPC"
            ));
        
        String providerLine = (providerAlias != null && !providerAlias.isEmpty()) 
            ? String.format("  provider = aws.%s\n", providerAlias) 
            : "";
            
        return String.format("""
            resource "aws_internet_gateway" "%s" {
            %s  vpc_id = aws_vpc.%s.id
              
              tags = {
                Name = "%s"
              }
            }
            """, node.getId(), providerLine, vpcNode.getId(), node.getDisplayName());
    }
    
    @Override
    public String getResourceType() {
        return "INTERNET_GATEWAY";
    }
}

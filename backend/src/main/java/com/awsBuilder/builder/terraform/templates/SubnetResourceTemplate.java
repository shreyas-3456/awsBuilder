package com.awsBuilder.builder.terraform.templates;

import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;
import com.awsBuilder.builder.terraform.model.TerraformResource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SubnetResourceTemplate implements TerraformResource {
    
    @Override
    public String generate(NodeDTO node, ResourceGraph graph, String providerAlias) {
        // Find parent VPC from graph
        List<NodeDTO> parents = graph.getParents(node.getId());
        NodeDTO vpcNode = parents.stream()
            .filter(n -> "VPC".equals(n.getType()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "Subnet must be connected to a VPC"
            ));
        
        String cidrBlock = node.getProperties().getOrDefault("cidr_block", "10.0.1.0/24");
        String availabilityZone = node.getProperties().getOrDefault("availability_zone", "");
        String providerLine = (providerAlias != null && !providerAlias.isEmpty()) 
            ? String.format("  provider = aws.%s\n", providerAlias) 
            : "";
        
        String azLine = (availabilityZone != null && !availabilityZone.isEmpty())
            ? String.format("  availability_zone = \"%s\"\n", availabilityZone)
            : "";
            
        return String.format("""
            resource "aws_subnet" "%s" {
            %s  vpc_id            = aws_vpc.%s.id
              cidr_block        = "%s"
            %s  
              tags = {
                Name = "%s"
              }
            }
            """, node.getId(), providerLine, vpcNode.getId(), cidrBlock, azLine, node.getDisplayName());
    }
    
    @Override
    public String getResourceType() {
        return "SUBNET";
    }
}

package com.awsBuilder.builder.terraform.templates;

import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;
import com.awsBuilder.builder.terraform.model.TerraformResource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Ec2ResourceTemplate implements TerraformResource {
    
    @Override
    public String generate(NodeDTO node, ResourceGraph graph, String providerAlias) {
        // Find parent Subnet from graph
        List<NodeDTO> parents = graph.getParents(node.getId());
        NodeDTO subnetNode = parents.stream()
            .filter(n -> "SUBNET".equals(n.getType()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "EC2 instance must be connected to a Subnet"
            ));
        
        String ami = node.getProperties().getOrDefault("ami", "");
        String instanceType = node.getProperties().getOrDefault("instance_type", "t2.micro");
        String providerLine = (providerAlias != null && !providerAlias.isEmpty()) 
            ? String.format("  provider = aws.%s\n", providerAlias) 
            : "";
        
        String amiLine = (ami != null && !ami.isEmpty())
            ? String.format("  ami           = \"%s\"\n", ami)
            : "";
            
        return String.format("""
            resource "aws_instance" "%s" {
            %s%s  instance_type = "%s"
              subnet_id     = aws_subnet.%s.id
              
              tags = {
                Name = "%s"
              }
            }
            """, node.getId(), providerLine, amiLine, instanceType, subnetNode.getId(), node.getDisplayName());
    }
    
    @Override
    public String getResourceType() {
        return "EC2";
    }
}

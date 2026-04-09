package com.awsBuilder.builder.terraform.templates;

import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;
import com.awsBuilder.builder.terraform.model.TerraformResource;
import org.springframework.stereotype.Component;

@Component
public class VpcResourceTemplate implements TerraformResource {
    
    @Override
    public String generate(NodeDTO node, ResourceGraph graph, String providerAlias) {
        String cidrBlock = node.getProperties().getOrDefault("cidr_block", "10.0.0.0/16");
        String providerLine = (providerAlias != null && !providerAlias.isEmpty()) 
            ? String.format("  provider = aws.%s\n", providerAlias) 
            : "";
        
        return String.format("""
            resource "aws_vpc" "%s" {
            %s  cidr_block           = "%s"
              enable_dns_support   = true
              enable_dns_hostnames = true
              
              tags = {
                Name = "%s"
              }
            }
            """, node.getId(), providerLine, cidrBlock, node.getDisplayName());
    }
    
    @Override
    public String getResourceType() {
        return "VPC";
    }
}

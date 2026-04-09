package com.awsBuilder.builder.terraform.templates;

import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;
import com.awsBuilder.builder.terraform.model.TerraformResource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RdsResourceTemplate implements TerraformResource {
    
    @Override
    public String generate(NodeDTO node, ResourceGraph graph, String providerAlias) {
        String engine = node.getProperties().get("engine");
        String instanceClass = node.getProperties().get("instance_class");
        String allocatedStorage = node.getProperties().get("allocated_storage");
        String dbName = node.getProperties().get("db_name");
        String username = node.getProperties().get("username");
        String password = node.getProperties().get("password");
        
        // Find parent Subnets from graph
        List<NodeDTO> parents = graph.getParents(node.getId());
        List<NodeDTO> subnetNodes = parents.stream()
            .filter(n -> "SUBNET".equals(n.getType()))
            .collect(Collectors.toList());
        
        if (subnetNodes.isEmpty()) {
            throw new IllegalStateException(
                "RDS instance must be connected to at least one Subnet"
            );
        }
        
        String providerLine = (providerAlias != null && !providerAlias.isEmpty()) 
            ? String.format("  provider = aws.%s\n", providerAlias) 
            : "";
            
        StringBuilder terraform = new StringBuilder();
        
        // Create DB subnet group (works for both single and multiple subnets)
        String subnetGroupName = node.getId() + "_subnet_group";
        String subnetIds = subnetNodes.stream()
            .map(s -> "aws_subnet." + s.getId() + ".id")
            .collect(Collectors.joining(", "));
        
        terraform.append(String.format("""
            resource "aws_db_subnet_group" "%s" {
            %s  name       = "%s"
              subnet_ids = [%s]
              
              tags = {
                Name = "%s"
              }
            }
            
            """, subnetGroupName, providerLine, subnetGroupName, subnetIds, subnetGroupName));
        
        terraform.append(String.format("""
            resource "aws_db_instance" "%s" {
            %s  engine               = "%s"
              instance_class       = "%s"
              allocated_storage    = %s
              db_name              = "%s"
              username             = "%s"
              password             = "%s"
              db_subnet_group_name = aws_db_subnet_group.%s.name
              skip_final_snapshot  = true
              
              tags = {
                Name = "%s"
              }
            }
            """, node.getId(), providerLine, engine, instanceClass, allocatedStorage, 
                 dbName, username, password, subnetGroupName, node.getDisplayName()));
        
        return terraform.toString();
    }
    
    @Override
    public String getResourceType() {
        return "RDS";
    }
}

package com.awsBuilder.builder.terraform.templates;

import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;
import com.awsBuilder.builder.terraform.model.TerraformResource;
import org.springframework.stereotype.Component;

@Component
public class S3ResourceTemplate implements TerraformResource {
    
    @Override
    public String generate(NodeDTO node, ResourceGraph graph, String providerAlias) {
        String bucketName = node.getProperties().get("bucket_name");
        
        if (bucketName == null || bucketName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "S3 bucket requires bucket_name property"
            );
        }
        
        String providerLine = (providerAlias != null && !providerAlias.isEmpty()) 
            ? String.format("  provider = aws.%s\n", providerAlias) 
            : "";
            
        StringBuilder terraform = new StringBuilder();
        terraform.append(String.format("""
            resource "aws_s3_bucket" "%s" {
            %s  bucket = "%s"
              
              tags = {
                Name = "%s"
              }
            }
            """, node.getId(), providerLine, bucketName, node.getDisplayName()));
        
        // Add optional ACL if provided
        String acl = node.getProperties().get("acl");
        if (acl != null && !acl.trim().isEmpty()) {
            terraform.append(String.format("""
                
                resource "aws_s3_bucket_acl" "%s_acl" {
                %s  bucket = aws_s3_bucket.%s.id
                  acl    = "%s"
                }
                """, node.getId(), providerLine, node.getId(), acl));
        }
        
        // Add optional versioning if provided
        String versioning = node.getProperties().get("versioning");
        if ("true".equalsIgnoreCase(versioning)) {
            terraform.append(String.format("""
                
                resource "aws_s3_bucket_versioning" "%s_versioning" {
                %s  bucket = aws_s3_bucket.%s.id
                  
                  versioning_configuration {
                    status = "Enabled"
                  }
                }
                """, node.getId(), providerLine, node.getId()));
        }
        
        return terraform.toString();
    }
    
    @Override
    public String getResourceType() {
        return "S3";
    }
}

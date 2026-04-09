package com.awsBuilder.builder.terraform.model;

import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;

/**
 * Interface for generating Terraform HCL code for specific AWS resource types.
 */
public interface TerraformResource {
    
    /**
     * Generates Terraform HCL code for a specific resource
     * 
     * @param node The resource node containing type and properties
     * @param graph The complete resource graph for resolving dependencies
     * @return Terraform HCL code block as a string
     */
    String generate(NodeDTO node, ResourceGraph graph, String providerAlias);
    
    /**
     * Returns the resource type this template handles
     * 
     * @return The AWS resource type (e.g., "VPC", "SUBNET", "EC2")
     */
    String getResourceType();
}

package com.awsBuilder.builder.cloudformation.model;

import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;

/**
 * Interface for CloudFormation resource templates.
 * Each implementation generates a CloudFormation YAML fragment for a specific AWS resource type.
 */
public interface CloudFormationResource {

    /**
     * Generates a CloudFormation YAML fragment for the given node.
     *
     * @param node the diagram node to generate CloudFormation for
     * @param graph the resource dependency graph
     * @return a YAML string representing the CloudFormation resource fragment
     */
    String generate(NodeDTO node, ResourceGraph graph);

    /**
     * Returns the resource type this template handles (e.g., "VPC", "SUBNET", "EC2").
     *
     * @return the resource type string
     */
    String getResourceType();
}

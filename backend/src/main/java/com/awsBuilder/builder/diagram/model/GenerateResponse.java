package com.awsBuilder.builder.diagram.model;

/**
 * Response model for code generation containing both Terraform and CloudFormation outputs.
 */
public record GenerateResponse(String terraform, String cloudformation) {}

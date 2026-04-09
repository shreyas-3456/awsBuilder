package com.awsBuilder.builder.cloudformation.service;

import com.awsBuilder.builder.cloudformation.model.CloudFormationResource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CloudFormationTemplateRegistry {
    
    private final Map<String, CloudFormationResource> templates;
    
    public CloudFormationTemplateRegistry(List<CloudFormationResource> allTemplates) {
        this.templates = allTemplates.stream()
            .collect(Collectors.toMap(
                CloudFormationResource::getResourceType,
                Function.identity()
            ));
    }
    
    public CloudFormationResource getTemplate(String resourceType) {
        CloudFormationResource template = templates.get(resourceType);
        if (template == null) {
            throw new IllegalArgumentException(
                "No template found for resource type: " + resourceType
            );
        }
        return template;
    }
}

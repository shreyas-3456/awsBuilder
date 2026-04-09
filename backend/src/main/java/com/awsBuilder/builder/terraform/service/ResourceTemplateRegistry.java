package com.awsBuilder.builder.terraform.service;

import com.awsBuilder.builder.terraform.model.TerraformResource;
import com.awsBuilder.builder.exception.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ResourceTemplateRegistry {
    
    private final Map<String, TerraformResource> templates;
    
    public ResourceTemplateRegistry(List<TerraformResource> allTemplates) {
        this.templates = allTemplates.stream()
            .collect(Collectors.toMap(
                TerraformResource::getResourceType,
                Function.identity()
            ));
        System.out.println("REGISTERED TEMPLATES: " + this.templates.keySet());
    }
    
    public TerraformResource getTemplate(String resourceType) {
        TerraformResource template = templates.get(resourceType);
        if (template == null) {
            throw new ResourceNotFoundException(
                "No template found for resource type: " + resourceType
            );
        }
        return template;
    }
}

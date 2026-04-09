package com.awsBuilder.builder.terraform.service;

import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.terraform.model.TerraformResource;
import com.awsBuilder.builder.diagram.model.ResourceGraph;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TerraformGenerator {
    
    private final ResourceTemplateRegistry registry;
    
    public TerraformGenerator(ResourceTemplateRegistry registry) {
        this.registry = registry;
    }
    
    public String generate(List<NodeDTO> orderedNodes, ResourceGraph graph, String primaryRegion) {
        StringBuilder terraform = new StringBuilder();
        
        // 1. Identify all unique regions and map them to aliases
        java.util.Map<String, String> regionToAlias = new java.util.HashMap<>();
        int aliasCount = 1;
        
        for (NodeDTO node : orderedNodes) {
            String nodeRegion = node.getProperties().get("region");
            if (nodeRegion != null && !nodeRegion.isEmpty() && !nodeRegion.equals(primaryRegion)) {
                if (!regionToAlias.containsKey(nodeRegion)) {
                    regionToAlias.put(nodeRegion, "region" + aliasCount++);
                }
            }
        }
        
        // 2. Add provider blocks
        terraform.append(generateProviderBlock(primaryRegion, null));
        for (java.util.Map.Entry<String, String> entry : regionToAlias.entrySet()) {
            terraform.append("\n");
            terraform.append(generateProviderBlock(entry.getKey(), entry.getValue()));
        }
        terraform.append("\n");
        
        // 3. Generate resource blocks in dependency order
        for (NodeDTO node : orderedNodes) {
            TerraformResource template = registry.getTemplate(node.getType());
            String nodeRegion = node.getProperties().get("region");
            String alias = regionToAlias.get(nodeRegion);
            
            String resourceBlock = template.generate(node, graph, alias);
            terraform.append(resourceBlock);
            terraform.append("\n");
        }
        
        return terraform.toString();
    }
    
    private String generateProviderBlock(String region, String alias) {
        if (alias == null || alias.isEmpty()) {
            return String.format("""
                provider "aws" {
                  region = "%s"
                }
                """, region);
        } else {
            return String.format("""
                provider "aws" {
                  alias  = "%s"
                  region = "%s"
                }
                """, alias, region);
        }
    }
}

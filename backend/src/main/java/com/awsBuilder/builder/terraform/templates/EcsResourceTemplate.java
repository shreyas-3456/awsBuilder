package com.awsBuilder.builder.terraform.templates;

import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;
import com.awsBuilder.builder.terraform.model.TerraformResource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EcsResourceTemplate implements TerraformResource {
    @Override
    public String getResourceType() {
        return "ECS";
    }

    @Override
    public String generate(NodeDTO node, ResourceGraph graph, String regionAlias) {
        String id = node.getId();
        Map<String, String> props = node.getProperties();
        String clusterName = props.getOrDefault("cluster_name", id);
        String containerInsights = props.getOrDefault("container_insights", "enabled");

        String provider = (regionAlias != null && !regionAlias.isEmpty()) ? "  provider = aws." + regionAlias + "\n" : "";

        return String.format("""
            resource "aws_ecs_cluster" "%s" {
            %s  name = "%s"

              setting {
                name  = "containerInsights"
                value = "%s"
              }

              tags = {
                Name = "%s"
              }
            }
            """, id, provider, clusterName, containerInsights, id);
    }
}

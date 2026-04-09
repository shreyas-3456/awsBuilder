package com.awsBuilder.builder.terraform.templates;

import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;
import com.awsBuilder.builder.terraform.model.TerraformResource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FargateResourceTemplate implements TerraformResource {
    @Override
    public String getResourceType() {
        return "FARGATE";
    }

    @Override
    public String generate(NodeDTO node, ResourceGraph graph, String regionAlias) {
        String id = node.getId();
        Map<String, String> props = node.getProperties();
        String family = props.getOrDefault("family", id);
        String cpu = props.getOrDefault("cpu", "256");
        String memory = props.getOrDefault("memory", "512");
        String containerName = props.getOrDefault("container_name", id + "-container");
        String containerImage = props.getOrDefault("container_image", "nginx:latest");
        String containerPort = props.getOrDefault("container_port", "80");

        String provider = (regionAlias != null && !regionAlias.isEmpty()) ? "  provider = aws." + regionAlias + "\n" : "";

        return String.format("""
            resource "aws_ecs_task_definition" "%s" {
            %s  family                   = "%s"
              requires_compatibilities = ["FARGATE"]
              network_mode             = "awsvpc"
              cpu                      = "%s"
              memory                   = "%s"

              container_definitions = jsonencode([
                {
                  name      = "%s"
                  image     = "%s"
                  essential = true
                  portMappings = [
                    {
                      containerPort = %s
                      hostPort      = %s
                      protocol      = "tcp"
                    }
                  ]
                }
              ])

              tags = {
                Name = "%s"
              }
            }
            """, id, provider, family, cpu, memory,
                containerName, containerImage, containerPort, containerPort, id);
    }
}

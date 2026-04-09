package com.awsBuilder.builder.terraform.templates;

import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;
import com.awsBuilder.builder.terraform.model.TerraformResource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ApiGatewayResourceTemplate implements TerraformResource {
    @Override
    public String getResourceType() {
        return "API_GATEWAY";
    }

    @Override
    public String generate(NodeDTO node, ResourceGraph graph, String regionAlias) {
        String id = node.getId();
        Map<String, String> props = node.getProperties();
        String name = props.getOrDefault("name", id);
        
        String provider = (regionAlias != null && !regionAlias.isEmpty()) ? "  provider = aws." + regionAlias + "\n" : "";

        return String.format("""
            resource "aws_apigatewayv2_api" "%s" {
            %s  name          = "%s"
              protocol_type = "HTTP"

              tags = {
                Name = "%s"
              }
            }
            """, id, provider, name, id);
    }
}

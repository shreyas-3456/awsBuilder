package com.awsBuilder.builder.terraform.templates;

import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;
import com.awsBuilder.builder.terraform.model.TerraformResource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class LambdaResourceTemplate implements TerraformResource {
    @Override
    public String getResourceType() {
        return "LAMBDA";
    }

    @Override
    public String generate(NodeDTO node, ResourceGraph graph, String regionAlias) {
        String id = node.getId();
        Map<String, String> props = node.getProperties();
        String functionName = props.getOrDefault("function_name", id);
        String runtime = props.getOrDefault("runtime", "nodejs18.x");
        String handler = props.getOrDefault("handler", "index.handler");
        
        String provider = (regionAlias != null && !regionAlias.isEmpty()) ? "  provider = aws." + regionAlias + "\n" : "";

        return String.format("""
            resource "aws_lambda_function" "%s" {
            %s  function_name = "%s"
              role          = "arn:aws:iam::123456789012:role/lambda-role"
              handler       = "%s"
              runtime       = "%s"
              filename      = "lambda_function_payload.zip"

              tags = {
                Name = "%s"
              }
            }
            """, id, provider, functionName, handler, runtime, id);
    }
}

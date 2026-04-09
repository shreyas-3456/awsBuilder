package com.awsBuilder.builder.terraform.templates;

import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;
import com.awsBuilder.builder.terraform.model.TerraformResource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DynamoDbResourceTemplate implements TerraformResource {
    @Override
    public String getResourceType() {
        return "DYNAMODB";
    }

    @Override
    public String generate(NodeDTO node, ResourceGraph graph, String regionAlias) {
        String id = node.getId();
        Map<String, String> props = node.getProperties();
        String tableName = props.getOrDefault("table_name", id);
        String billingMode = props.getOrDefault("billing_mode", "PAY_PER_REQUEST");
        String hashKey = props.getOrDefault("hash_key", "id");
        
        String provider = (regionAlias != null && !regionAlias.isEmpty()) ? "  provider = aws." + regionAlias + "\n" : "";

        return String.format("""
            resource "aws_dynamodb_table" "%s" {
            %s  name         = "%s"
              billing_mode = "%s"
              hash_key     = "%s"

              attribute {
                name = "%s"
                type = "S"
              }

              tags = {
                Name = "%s"
              }
            }
            """, id, provider, tableName, billingMode, hashKey, hashKey, id);
    }
}

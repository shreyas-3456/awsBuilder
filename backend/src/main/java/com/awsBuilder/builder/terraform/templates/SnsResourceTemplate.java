package com.awsBuilder.builder.terraform.templates;

import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;
import com.awsBuilder.builder.terraform.model.TerraformResource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SnsResourceTemplate implements TerraformResource {
    @Override
    public String getResourceType() {
        return "SNS";
    }

    @Override
    public String generate(NodeDTO node, ResourceGraph graph, String regionAlias) {
        String id = node.getId();
        Map<String, String> props = node.getProperties();
        String topicName = props.getOrDefault("topic_name", id);
        String displayName = props.getOrDefault("display_name", topicName);
        String fifoTopic = props.getOrDefault("fifo_topic", "false");
        
        String provider = (regionAlias != null && !regionAlias.isEmpty()) ? "  provider = aws." + regionAlias + "\n" : "";

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("""
            resource "aws_sns_topic" "%s" {
            %s  name         = "%s"
              display_name = "%s"
            """, id, provider, topicName, displayName));

        if ("true".equals(fifoTopic)) {
            sb.append("  fifo_topic                  = true\n");
            sb.append("  content_based_deduplication = true\n");
        }

        sb.append(String.format("""
              tags = {
                Name = "%s"
              }
            }
            """, id));
        return sb.toString();
    }
}

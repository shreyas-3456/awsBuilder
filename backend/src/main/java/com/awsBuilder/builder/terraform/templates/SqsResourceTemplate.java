package com.awsBuilder.builder.terraform.templates;

import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;
import com.awsBuilder.builder.terraform.model.TerraformResource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SqsResourceTemplate implements TerraformResource {
    @Override
    public String getResourceType() {
        return "SQS";
    }

    @Override
    public String generate(NodeDTO node, ResourceGraph graph, String regionAlias) {
        String id = node.getId();
        Map<String, String> props = node.getProperties();
        String queueName = props.getOrDefault("queue_name", id);
        String delaySeconds = props.getOrDefault("delay_seconds", "0");
        String maxMessageSize = props.getOrDefault("max_message_size", "262144");
        String messageRetentionSeconds = props.getOrDefault("message_retention_seconds", "345600");
        String visibilityTimeout = props.getOrDefault("visibility_timeout", "30");
        String fifoQueue = props.getOrDefault("fifo_queue", "false");
        
        String provider = (regionAlias != null && !regionAlias.isEmpty()) ? "  provider = aws." + regionAlias + "\n" : "";

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("""
            resource "aws_sqs_queue" "%s" {
            %s  name                       = "%s"
              delay_seconds              = %s
              max_message_size           = %s
              message_retention_seconds  = %s
              visibility_timeout_seconds = %s
            """, id, provider, queueName, delaySeconds, maxMessageSize, messageRetentionSeconds, visibilityTimeout));

        if ("true".equals(fifoQueue)) {
            sb.append("  fifo_queue                 = true\n");
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

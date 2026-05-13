package com.awsBuilder.builder.terraform.templates;

import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;
import com.awsBuilder.builder.terraform.model.TerraformResource;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        appendEventBridgePolicy(sb, provider, id, graph);
        return sb.toString();
    }

    private void appendEventBridgePolicy(StringBuilder sb, String provider, String queueId, ResourceGraph graph) {
        List<NodeDTO> eventBridgeRules = graph.getChildren(queueId).stream()
                .filter(node -> "EVENTBRIDGE".equals(node.getType()))
                .sorted(Comparator.comparing(NodeDTO::getId))
                .toList();

        if (eventBridgeRules.isEmpty()) {
            return;
        }

        String statements = eventBridgeRules.stream()
                .map(rule -> String.format("""
                    {
                      Sid = "%s"
                      Effect = "Allow"
                      Principal = {
                        Service = "events.amazonaws.com"
                      }
                      Action = "sqs:SendMessage"
                      Resource = aws_sqs_queue.%s.arn
                      Condition = {
                        ArnEquals = {
                          "aws:SourceArn" = aws_cloudwatch_event_rule.%s.arn
                        }
                      }
                    }""", statementId("AllowEventBridge", rule.getId(), queueId), queueId, rule.getId()))
                .collect(Collectors.joining(",\n"));

        sb.append(String.format("""

            resource "aws_sqs_queue_policy" "%s_eventbridge_policy" {
            %s  queue_url = aws_sqs_queue.%s.id

              policy = jsonencode({
                Version = "2012-10-17"
                Statement = [
            %s
                ]
              })
            }
            """, queueId, provider, queueId, statements.indent(6).stripTrailing()));
    }

    private String statementId(String prefix, String ruleId, String queueId) {
        return (prefix + ruleId + queueId).replaceAll("[^A-Za-z0-9]", "");
    }
}

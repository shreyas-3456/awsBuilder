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
        appendEventBridgePolicy(sb, provider, id, graph);
        return sb.toString();
    }

    private void appendEventBridgePolicy(StringBuilder sb, String provider, String topicId, ResourceGraph graph) {
        List<NodeDTO> eventBridgeRules = graph.getChildren(topicId).stream()
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
                      Action = "sns:Publish"
                      Resource = aws_sns_topic.%s.arn
                      Condition = {
                        ArnEquals = {
                          "aws:SourceArn" = aws_cloudwatch_event_rule.%s.arn
                        }
                      }
                    }""", statementId("AllowEventBridge", rule.getId(), topicId), topicId, rule.getId()))
                .collect(Collectors.joining(",\n"));

        sb.append(String.format("""

            resource "aws_sns_topic_policy" "%s_eventbridge_policy" {
            %s  arn = aws_sns_topic.%s.arn

              policy = jsonencode({
                Version = "2012-10-17"
                Statement = [
            %s
                ]
              })
            }
            """, topicId, provider, topicId, statements.indent(6).stripTrailing()));
    }

    private String statementId(String prefix, String ruleId, String topicId) {
        return (prefix + ruleId + topicId).replaceAll("[^A-Za-z0-9]", "");
    }
}

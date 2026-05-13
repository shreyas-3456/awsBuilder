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
public class EventBridgeResourceTemplate implements TerraformResource {
    @Override
    public String getResourceType() {
        return "EVENTBRIDGE";
    }

    @Override
    public String generate(NodeDTO node, ResourceGraph graph, String regionAlias) {
        String id = node.getId();
        Map<String, String> props = node.getProperties();

        String ruleName = props.getOrDefault("rule_name", id.toLowerCase() + "-rule");
        String eventBusName = props.getOrDefault("event_bus_name", "default");
        String description = props.getOrDefault("description", "EventBridge rule managed by Terraform");
        String scheduleExpression = props.getOrDefault("schedule_expression", "");
        String eventPattern = props.getOrDefault("event_pattern", "");
        String state = props.getOrDefault("state", "ENABLED");

        String provider = providerLine(regionAlias);
        List<NodeDTO> targets = graph.getParents(node.getId()).stream()
                .sorted(Comparator.comparing(NodeDTO::getId))
                .toList();
        List<NodeDTO> kinesisTargets = targets.stream()
                .filter(target -> "KINESIS".equals(target.getType()))
                .toList();
        String providedTargetRoleArn = firstNonBlank(
                props.get("target_role_arn"),
                props.get("role_arn")
        );
        String generatedTargetRoleName = id + "_eventbridge_target_role";

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("""
            resource "aws_cloudwatch_event_rule" "%s" {
            %s  name           = "%s"
              description    = "%s"
              event_bus_name = "%s"
              state          = "%s"
            """, id, provider, hclString(ruleName), hclString(description), hclString(eventBusName), hclString(state)));

        if (!scheduleExpression.isEmpty()) {
            sb.append(String.format("  schedule_expression = \"%s\"\n", hclString(scheduleExpression)));
        }

        if (!eventPattern.isEmpty()) {
            sb.append(String.format("  event_pattern = <<PATTERN\n%s\nPATTERN\n", eventPattern));
        }

        sb.append(String.format("""

              tags = {
            %s
              }
            }
            """, formatTags(props.get("tags"), id)));

        if (!kinesisTargets.isEmpty() && providedTargetRoleArn == null) {
            appendGeneratedTargetRole(sb, provider, generatedTargetRoleName, kinesisTargets);
        }

        for (NodeDTO target : targets) {
            appendTargetResource(sb, provider, id, target, providedTargetRoleArn, generatedTargetRoleName);
            appendTargetPermission(sb, provider, id, target);
        }

        return sb.toString();
    }

    private void appendGeneratedTargetRole(StringBuilder sb, String provider, String roleName, List<NodeDTO> kinesisTargets) {
        String streamArns = kinesisTargets.stream()
                .map(target -> targetArnExpression(target))
                .collect(Collectors.joining(",\n        "));

        sb.append(String.format("""

            resource "aws_iam_role" "%s" {
            %s  assume_role_policy = jsonencode({
                Version = "2012-10-17"
                Statement = [
                  {
                    Effect = "Allow"
                    Action = "sts:AssumeRole"
                    Principal = {
                      Service = "events.amazonaws.com"
                    }
                  }
                ]
              })
            }

            resource "aws_iam_role_policy" "%s_kinesis" {
            %s  role = aws_iam_role.%s.id

              policy = jsonencode({
                Version = "2012-10-17"
                Statement = [
                  {
                    Effect = "Allow"
                    Action = [
                      "kinesis:PutRecord",
                      "kinesis:PutRecords"
                    ]
                    Resource = [
                    %s
                    ]
                  }
                ]
              })
            }
            """, roleName, provider, roleName, provider, roleName, streamArns.indent(2).stripTrailing()));
    }

    private void appendTargetResource(StringBuilder sb, String provider, String ruleId, NodeDTO target,
                                      String providedTargetRoleArn, String generatedTargetRoleName) {
        String targetResourceName = ruleId + "_" + target.getId();
        String roleArnLine = "";

        if ("KINESIS".equals(target.getType())) {
            String roleArnExpression = providedTargetRoleArn != null
                    ? "\"" + hclString(providedTargetRoleArn) + "\""
                    : "aws_iam_role." + generatedTargetRoleName + ".arn";
            roleArnLine = String.format("  role_arn       = %s\n", roleArnExpression);
        }

        sb.append(String.format("""

            resource "aws_cloudwatch_event_target" "%s" {
            %s  rule           = aws_cloudwatch_event_rule.%s.name
              event_bus_name = aws_cloudwatch_event_rule.%s.event_bus_name
              target_id      = "%s"
              arn            = %s
            %s}
            """, targetResourceName, provider, ruleId, ruleId, hclString(target.getId()),
                targetArnExpression(target), roleArnLine));
    }

    private void appendTargetPermission(StringBuilder sb, String provider, String ruleId, NodeDTO target) {
        String resourceName = ruleId + "_" + target.getId();

        switch (target.getType()) {
            case "LAMBDA" -> sb.append(String.format("""

                resource "aws_lambda_permission" "%s" {
                %s  statement_id  = "%s"
                  action        = "lambda:InvokeFunction"
                  function_name = aws_lambda_function.%s.function_name
                  principal     = "events.amazonaws.com"
                  source_arn    = aws_cloudwatch_event_rule.%s.arn
                }
                """, resourceName, provider, statementId("AllowEventBridge", ruleId, target.getId()), target.getId(), ruleId));
            case "SQS", "SNS" -> {
                // SQS and SNS resource policies are generated by the target resource templates
                // so multiple EventBridge rules can share the same queue or topic safely.
            }
            case "KINESIS" -> {
                // Kinesis access is granted by the generated or user-provided EventBridge target role.
            }
            default -> throw new IllegalArgumentException(
                    "Unsupported EventBridge target type: " + target.getType()
            );
        }
    }

    private String targetArnExpression(NodeDTO target) {
        return switch (target.getType()) {
            case "LAMBDA" -> "aws_lambda_function." + target.getId() + ".arn";
            case "SQS" -> "aws_sqs_queue." + target.getId() + ".arn";
            case "SNS" -> "aws_sns_topic." + target.getId() + ".arn";
            case "KINESIS" -> "aws_kinesis_stream." + target.getId() + ".arn";
            default -> throw new IllegalArgumentException(
                    "Unsupported EventBridge target type: " + target.getType()
            );
        };
    }

    private String providerLine(String regionAlias) {
        return (regionAlias != null && !regionAlias.isEmpty()) ? "  provider = aws." + regionAlias + "\n" : "";
    }

    private String hclString(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }

    private String formatTags(String tagsProperty, String defaultName) {
        if (tagsProperty == null || tagsProperty.isBlank()) {
            return String.format("    Name = \"%s\"", hclString(defaultName));
        }

        return java.util.Arrays.stream(tagsProperty.split(","))
                .map(String::trim)
                .filter(tag -> tag.contains("="))
                .map(tag -> {
                    String[] parts = tag.split("=", 2);
                    return String.format("    \"%s\" = \"%s\"", hclString(parts[0].trim()), hclString(parts[1].trim()));
                })
                .collect(Collectors.joining("\n"));
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String statementId(String prefix, String ruleId, String targetId) {
        String statementId = prefix + ruleId + targetId;
        return statementId.replaceAll("[^A-Za-z0-9]", "");
    }
}

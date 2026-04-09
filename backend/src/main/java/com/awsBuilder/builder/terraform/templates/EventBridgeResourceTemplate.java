package com.awsBuilder.builder.terraform.templates;

import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;
import com.awsBuilder.builder.terraform.model.TerraformResource;
import org.springframework.stereotype.Component;

import java.util.Map;

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

        String provider = (regionAlias != null && !regionAlias.isEmpty()) ? "  provider = aws." + regionAlias + "\n" : "";

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("""
            resource "aws_cloudwatch_event_rule" "%s" {
            %s  name        = "%s"
              description = "%s"
              event_bus_name = "%s"
              state       = "%s"
            """, id, provider, ruleName, description, eventBusName, state));

        if (!scheduleExpression.isEmpty()) {
            sb.append(String.format("  schedule_expression = \"%s\"\n", scheduleExpression));
        }

        if (!eventPattern.isEmpty()) {
            sb.append(String.format("  event_pattern = <<PATTERN\n%s\nPATTERN\n", eventPattern));
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

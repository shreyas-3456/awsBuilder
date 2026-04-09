package com.awsBuilder.builder.terraform.templates;

import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;
import com.awsBuilder.builder.terraform.model.TerraformResource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CloudWatchResourceTemplate implements TerraformResource {
    @Override
    public String getResourceType() {
        return "CLOUDWATCH";
    }

    @Override
    public String generate(NodeDTO node, ResourceGraph graph, String regionAlias) {
        String id = node.getId();
        Map<String, String> props = node.getProperties();
        String alarmName = props.getOrDefault("alarm_name", id);
        String metricName = props.getOrDefault("metric_name", "CPUUtilization");
        String namespace = props.getOrDefault("namespace", "AWS/EC2");
        String statistic = props.getOrDefault("statistic", "Average");
        String period = props.getOrDefault("period", "300");
        String evaluationPeriods = props.getOrDefault("evaluation_periods", "2");
        String threshold = props.getOrDefault("threshold", "80");
        String comparisonOperator = props.getOrDefault("comparison_operator", "GreaterThanThreshold");

        String provider = (regionAlias != null && !regionAlias.isEmpty()) ? "  provider = aws." + regionAlias + "\n" : "";

        return String.format("""
            resource "aws_cloudwatch_metric_alarm" "%s" {
            %s  alarm_name          = "%s"
              comparison_operator = "%s"
              evaluation_periods  = %s
              metric_name         = "%s"
              namespace           = "%s"
              period              = %s
              statistic           = "%s"
              threshold           = %s
              alarm_description   = "Managed by Terraform Builder"

              tags = {
                Name = "%s"
              }
            }
            """, id, provider, alarmName, comparisonOperator, evaluationPeriods,
                metricName, namespace, period, statistic, threshold, id);
    }
}

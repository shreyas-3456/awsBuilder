package com.awsBuilder.builder.cloudformation.templates;

import com.awsBuilder.builder.cloudformation.model.CloudFormationResource;
import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class CloudWatchCfnTemplate implements CloudFormationResource {
    @Override
    public String generate(NodeDTO node, ResourceGraph graph) {
        String alarmName = node.getProperties().getOrDefault("alarm_name", node.getId());
        String metricName = node.getProperties().getOrDefault("metric_name", "CPUUtilization");
        String namespace = node.getProperties().getOrDefault("namespace", "AWS/EC2");
        String statistic = node.getProperties().getOrDefault("statistic", "Average");
        String period = node.getProperties().getOrDefault("period", "300");
        String evaluationPeriods = node.getProperties().getOrDefault("evaluation_periods", "2");
        String threshold = node.getProperties().getOrDefault("threshold", "80");
        String comparisonOperator = node.getProperties().getOrDefault("comparison_operator", "GreaterThanThreshold");

        Map<String, Object> resource = new LinkedHashMap<>();
        resource.put("Type", "AWS::CloudWatch::Alarm");

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("AlarmName", alarmName);
        properties.put("MetricName", metricName);
        properties.put("Namespace", namespace);
        properties.put("Statistic", statistic);
        properties.put("Period", Integer.parseInt(period));
        properties.put("EvaluationPeriods", Integer.parseInt(evaluationPeriods));
        properties.put("Threshold", Double.parseDouble(threshold));
        properties.put("ComparisonOperator", comparisonOperator);

        resource.put("Properties", properties);

        Map<String, Object> fragment = new LinkedHashMap<>();
        fragment.put(node.getId(), resource);

        DumperOptions opts = new DumperOptions();
        opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        opts.setPrettyFlow(true);
        return new Yaml(opts).dump(fragment);
    }

    @Override
    public String getResourceType() {
        return "CLOUDWATCH";
    }
}

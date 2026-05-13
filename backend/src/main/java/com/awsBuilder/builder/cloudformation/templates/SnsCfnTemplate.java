package com.awsBuilder.builder.cloudformation.templates;

import com.awsBuilder.builder.cloudformation.model.CloudFormationResource;
import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class SnsCfnTemplate implements CloudFormationResource {
    @Override
    public String generate(NodeDTO node, ResourceGraph graph) {
        String topicName = node.getProperties().getOrDefault("topic_name", node.getId());
        String displayName = node.getProperties().getOrDefault("display_name", topicName);
        String fifoTopic = node.getProperties().getOrDefault("fifo_topic", "false");
        
        Map<String, Object> resource = new LinkedHashMap<>();
        resource.put("Type", "AWS::SNS::Topic");
        
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("TopicName", topicName);
        properties.put("DisplayName", displayName);
        
        if ("true".equals(fifoTopic)) {
            properties.put("FifoTopic", true);
            properties.put("ContentBasedDeduplication", true);
        }
        
        resource.put("Properties", properties);
        
        Map<String, Object> fragment = new LinkedHashMap<>();
        fragment.put(node.getId(), resource);
        addEventBridgeTopicPolicy(fragment, node, graph);
        
        DumperOptions opts = new DumperOptions();
        opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        opts.setPrettyFlow(true);
        return new Yaml(opts).dump(fragment);
    }

    @Override
    public String getResourceType() {
        return "SNS";
    }

    private void addEventBridgeTopicPolicy(Map<String, Object> fragment, NodeDTO node, ResourceGraph graph) {
        List<NodeDTO> eventBridgeRules = graph.getChildren(node.getId()).stream()
            .filter(source -> "EVENTBRIDGE".equals(source.getType()))
            .sorted(Comparator.comparing(NodeDTO::getId))
            .toList();

        if (eventBridgeRules.isEmpty()) {
            return;
        }

        Map<String, Object> policyResource = new LinkedHashMap<>();
        policyResource.put("Type", "AWS::SNS::TopicPolicy");

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("Topics", List.of("!Ref " + node.getId()));
        properties.put("PolicyDocument", policyDocument(eventBridgeRules.stream()
            .map(rule -> statement(
                "sns:Publish",
                "!Ref " + node.getId(),
                "!GetAtt " + rule.getId() + ".Arn"
            ))
            .toList()));
        policyResource.put("Properties", properties);

        fragment.put(logicalId(node.getId(), "EventBridgeTopicPolicy"), policyResource);
    }

    private Map<String, Object> policyDocument(List<Map<String, Object>> statements) {
        Map<String, Object> policyDocument = new LinkedHashMap<>();
        policyDocument.put("Version", "2012-10-17");
        policyDocument.put("Statement", statements);
        return policyDocument;
    }

    private Map<String, Object> statement(String action, String resourceArn, String sourceArn) {
        Map<String, Object> statement = new LinkedHashMap<>();
        statement.put("Effect", "Allow");
        statement.put("Principal", Map.of("Service", "events.amazonaws.com"));
        statement.put("Action", action);
        statement.put("Resource", resourceArn);
        statement.put("Condition", Map.of("ArnEquals", Map.of("aws:SourceArn", sourceArn)));
        return statement;
    }

    private String logicalId(String... parts) {
        StringBuilder logicalId = new StringBuilder();

        for (String part : parts) {
            for (String token : part.split("[^A-Za-z0-9]+")) {
                if (!token.isBlank()) {
                    logicalId.append(Character.toUpperCase(token.charAt(0)));
                    if (token.length() > 1) {
                        logicalId.append(token.substring(1));
                    }
                }
            }
        }

        return logicalId.length() == 0 ? "SnsResource" : logicalId.toString();
    }
}

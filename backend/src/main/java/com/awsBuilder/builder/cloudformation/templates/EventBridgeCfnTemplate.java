package com.awsBuilder.builder.cloudformation.templates;

import com.awsBuilder.builder.cloudformation.model.CloudFormationResource;
import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class EventBridgeCfnTemplate implements CloudFormationResource {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String generate(NodeDTO node, ResourceGraph graph) {
        Map<String, String> props = node.getProperties();

        String ruleName = props.getOrDefault("rule_name", node.getId().toLowerCase() + "-rule");
        String eventBusName = props.getOrDefault("event_bus_name", "default");
        String description = props.getOrDefault("description", "EventBridge rule managed by CloudFormation");
        String scheduleExpression = props.getOrDefault("schedule_expression", "");
        String eventPattern = props.getOrDefault("event_pattern", "");
        String state = props.getOrDefault("state", "ENABLED");
        String targetRoleArn = firstNonBlank(props.get("target_role_arn"), props.get("role_arn"));

        List<NodeDTO> targets = graph.getParents(node.getId()).stream()
                .sorted(Comparator.comparing(NodeDTO::getId))
                .toList();
        List<NodeDTO> kinesisTargets = targets.stream()
                .filter(target -> "KINESIS".equals(target.getType()))
                .toList();

        Map<String, Object> ruleResource = new LinkedHashMap<>();
        ruleResource.put("Type", "AWS::Events::Rule");

        Map<String, Object> ruleProperties = new LinkedHashMap<>();
        ruleProperties.put("Name", ruleName);
        ruleProperties.put("Description", description);
        ruleProperties.put("EventBusName", eventBusName);
        ruleProperties.put("State", state);

        if (!scheduleExpression.isBlank()) {
            ruleProperties.put("ScheduleExpression", scheduleExpression);
        }

        if (!eventPattern.isBlank()) {
            ruleProperties.put("EventPattern", parseEventPattern(eventPattern, node.getId()));
        }

        if (!targets.isEmpty()) {
            ruleProperties.put("Targets", buildTargets(node, targets, targetRoleArn));
        }

        ruleProperties.put("Tags", parseTags(props.get("tags"), node.getId()));
        ruleResource.put("Properties", ruleProperties);

        Map<String, Object> fragment = new LinkedHashMap<>();
        fragment.put(node.getId(), ruleResource);

        if (!kinesisTargets.isEmpty() && targetRoleArn == null) {
            addKinesisTargetRole(fragment, node, kinesisTargets);
        }

        for (NodeDTO target : targets) {
            addTargetPermission(fragment, node, target);
        }

        DumperOptions opts = new DumperOptions();
        opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        opts.setPrettyFlow(true);
        return new Yaml(opts).dump(fragment);
    }

    @Override
    public String getResourceType() {
        return "EVENTBRIDGE";
    }

    private List<Map<String, Object>> buildTargets(NodeDTO ruleNode, List<NodeDTO> targets, String targetRoleArn) {
        List<Map<String, Object>> cfnTargets = new ArrayList<>();

        for (NodeDTO target : targets) {
            Map<String, Object> cfnTarget = new LinkedHashMap<>();
            cfnTarget.put("Id", target.getId());
            cfnTarget.put("Arn", targetArn(target));

            if ("KINESIS".equals(target.getType())) {
                String roleArn = targetRoleArn != null
                        ? targetRoleArn
                        : "!GetAtt " + kinesisRoleLogicalId(ruleNode) + ".Arn";
                cfnTarget.put("RoleArn", roleArn);
            }

            cfnTargets.add(cfnTarget);
        }

        return cfnTargets;
    }

    private String targetArn(NodeDTO target) {
        return switch (target.getType()) {
            case "LAMBDA", "SQS", "KINESIS" -> "!GetAtt " + target.getId() + ".Arn";
            case "SNS" -> "!Ref " + target.getId();
            default -> throw new IllegalArgumentException(
                    "Unsupported EventBridge target type: " + target.getType()
            );
        };
    }

    private Map<String, Object> parseEventPattern(String eventPattern, String ruleId) {
        try {
            return OBJECT_MAPPER.readValue(eventPattern, new TypeReference<LinkedHashMap<String, Object>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid event_pattern JSON for EventBridge rule " + ruleId, e);
        }
    }

    private void addKinesisTargetRole(Map<String, Object> fragment, NodeDTO ruleNode, List<NodeDTO> kinesisTargets) {
        String roleLogicalId = kinesisRoleLogicalId(ruleNode);
        String policyLogicalId = logicalId(ruleNode.getId(), "KinesisTargetPolicy");

        Map<String, Object> roleResource = new LinkedHashMap<>();
        roleResource.put("Type", "AWS::IAM::Role");

        Map<String, Object> roleProperties = new LinkedHashMap<>();
        roleProperties.put("AssumeRolePolicyDocument", policyDocument(List.of(statement(
                "Allow",
                Map.of("Service", "events.amazonaws.com"),
                "sts:AssumeRole",
                null,
                null
        ))));
        roleResource.put("Properties", roleProperties);
        fragment.put(roleLogicalId, roleResource);

        List<String> streamArns = kinesisTargets.stream()
                .map(target -> "!GetAtt " + target.getId() + ".Arn")
                .toList();

        Map<String, Object> policyResource = new LinkedHashMap<>();
        policyResource.put("Type", "AWS::IAM::Policy");

        Map<String, Object> policyProperties = new LinkedHashMap<>();
        policyProperties.put("PolicyName", ruleNode.getId() + "-eventbridge-kinesis");
        policyProperties.put("Roles", List.of("!Ref " + roleLogicalId));
        policyProperties.put("PolicyDocument", policyDocument(List.of(statement(
                "Allow",
                null,
                List.of("kinesis:PutRecord", "kinesis:PutRecords"),
                streamArns,
                null
        ))));
        policyResource.put("Properties", policyProperties);
        fragment.put(policyLogicalId, policyResource);
    }

    private void addTargetPermission(Map<String, Object> fragment, NodeDTO ruleNode, NodeDTO target) {
        switch (target.getType()) {
            case "LAMBDA" -> addLambdaPermission(fragment, ruleNode, target);
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

    private void addLambdaPermission(Map<String, Object> fragment, NodeDTO ruleNode, NodeDTO target) {
        Map<String, Object> resource = new LinkedHashMap<>();
        resource.put("Type", "AWS::Lambda::Permission");

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("Action", "lambda:InvokeFunction");
        properties.put("FunctionName", "!Ref " + target.getId());
        properties.put("Principal", "events.amazonaws.com");
        properties.put("SourceArn", "!GetAtt " + ruleNode.getId() + ".Arn");
        resource.put("Properties", properties);

        fragment.put(logicalId(ruleNode.getId(), target.getId(), "LambdaPermission"), resource);
    }

    private Map<String, Object> policyDocument(List<Map<String, Object>> statements) {
        Map<String, Object> policyDocument = new LinkedHashMap<>();
        policyDocument.put("Version", "2012-10-17");
        policyDocument.put("Statement", statements);
        return policyDocument;
    }

    private Map<String, Object> statement(String effect, Object principal, Object action, Object resource, Object condition) {
        Map<String, Object> statement = new LinkedHashMap<>();
        statement.put("Effect", effect);

        if (principal != null) {
            statement.put("Principal", principal);
        }

        statement.put("Action", action);

        if (resource != null) {
            statement.put("Resource", resource);
        }

        if (condition != null) {
            statement.put("Condition", condition);
        }

        return statement;
    }

    private List<Map<String, String>> parseTags(String tagsProperty, String defaultName) {
        List<Map<String, String>> tags = new ArrayList<>();

        if (tagsProperty == null || tagsProperty.isBlank()) {
            tags.add(tag("Name", defaultName));
            return tags;
        }

        for (String entry : tagsProperty.split(",")) {
            String[] parts = entry.trim().split("=", 2);
            if (parts.length == 2 && !parts[0].isBlank()) {
                tags.add(tag(parts[0].trim(), parts[1].trim()));
            }
        }

        if (tags.isEmpty()) {
            tags.add(tag("Name", defaultName));
        }

        return tags;
    }

    private Map<String, String> tag(String key, String value) {
        Map<String, String> tag = new LinkedHashMap<>();
        tag.put("Key", key);
        tag.put("Value", value);
        return tag;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String kinesisRoleLogicalId(NodeDTO ruleNode) {
        return logicalId(ruleNode.getId(), "EventBridgeTargetRole");
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

        return logicalId.length() == 0 ? "EventBridgeResource" : logicalId.toString();
    }
}

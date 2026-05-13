package com.awsBuilder.builder.cloudformation.templates;

import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EventBridgeCfnTemplateTest {
    private final EventBridgeCfnTemplate template = new EventBridgeCfnTemplate();

    @Test
    void testGetResourceType() {
        assertEquals("EVENTBRIDGE", template.getResourceType());
    }

    @Test
    void testGenerateEventBridgeRuleWithTargets() {
        NodeDTO eventBridge = node("eb-infra", "EVENTBRIDGE", Map.of(
            "rule_name", "ec2-state-monitor",
            "event_bus_name", "default",
            "description", "Monitors EC2 instance state changes",
            "event_pattern", "{\"source\":[\"aws.ec2\"],\"detail-type\":[\"EC2 Instance State-change Notification\"]}",
            "state", "ENABLED",
            "tags", "Name=my-event-rule"
        ));
        NodeDTO lambda = node("lambda-1", "LAMBDA", Map.of());
        NodeDTO sqs = node("sqs-1", "SQS", Map.of());
        NodeDTO kinesis = node("kinesis-1", "KINESIS", Map.of());
        NodeDTO sns = node("sns-1", "SNS", Map.of());

        ResourceGraph graph = new ResourceGraph();
        graph.addNode(eventBridge);
        graph.addNode(lambda);
        graph.addNode(sqs);
        graph.addNode(kinesis);
        graph.addNode(sns);
        graph.addEdge(eventBridge.getId(), lambda.getId());
        graph.addEdge(eventBridge.getId(), sqs.getId());
        graph.addEdge(eventBridge.getId(), kinesis.getId());
        graph.addEdge(eventBridge.getId(), sns.getId());

        String yamlFragment = template.generate(eventBridge, graph);

        Yaml yaml = new Yaml();
        Map<String, Object> parsed = yaml.load(yamlFragment);

        assertTrue(parsed.containsKey("eb-infra"));
        Map<String, Object> ruleResource = resource(parsed, "eb-infra");
        assertEquals("AWS::Events::Rule", ruleResource.get("Type"));

        Map<String, Object> properties = properties(ruleResource);
        assertEquals("ec2-state-monitor", properties.get("Name"));
        assertEquals("default", properties.get("EventBusName"));
        assertEquals("ENABLED", properties.get("State"));
        assertFalse(properties.containsKey("ScheduleExpression"));

        Map<String, Object> eventPattern = (Map<String, Object>) properties.get("EventPattern");
        assertEquals(List.of("aws.ec2"), eventPattern.get("source"));
        assertEquals(List.of("EC2 Instance State-change Notification"), eventPattern.get("detail-type"));

        List<Map<String, Object>> targets = (List<Map<String, Object>>) properties.get("Targets");
        assertEquals(4, targets.size());
        assertTarget(targets, "lambda-1", "!GetAtt lambda-1.Arn");
        assertTarget(targets, "sqs-1", "!GetAtt sqs-1.Arn");
        assertTarget(targets, "sns-1", "!Ref sns-1");
        Map<String, Object> kinesisTarget = assertTarget(targets, "kinesis-1", "!GetAtt kinesis-1.Arn");
        assertEquals("!GetAtt EbInfraEventBridgeTargetRole.Arn", kinesisTarget.get("RoleArn"));

        assertEquals("AWS::Lambda::Permission", resource(parsed, "EbInfraLambda1LambdaPermission").get("Type"));
        assertEquals("AWS::IAM::Role", resource(parsed, "EbInfraEventBridgeTargetRole").get("Type"));
        assertEquals("AWS::IAM::Policy", resource(parsed, "EbInfraKinesisTargetPolicy").get("Type"));
    }

    private Map<String, Object> resource(Map<String, Object> parsed, String key) {
        assertTrue(parsed.containsKey(key), "Missing resource: " + key);
        return (Map<String, Object>) parsed.get(key);
    }

    private Map<String, Object> properties(Map<String, Object> resource) {
        return (Map<String, Object>) resource.get("Properties");
    }

    private Map<String, Object> assertTarget(List<Map<String, Object>> targets, String id, String arn) {
        Map<String, Object> target = targets.stream()
            .filter(item -> id.equals(item.get("Id")))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Missing target: " + id));

        assertEquals(arn, target.get("Arn"));
        return target;
    }

    private NodeDTO node(String id, String type, Map<String, String> properties) {
        NodeDTO node = new NodeDTO();
        node.setId(id);
        node.setType(type);
        node.setProperties(new HashMap<>(properties));
        return node;
    }
}

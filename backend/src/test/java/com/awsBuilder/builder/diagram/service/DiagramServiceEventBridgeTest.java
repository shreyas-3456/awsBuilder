package com.awsBuilder.builder.diagram.service;

import com.awsBuilder.builder.cloudformation.model.CloudFormationResource;
import com.awsBuilder.builder.cloudformation.service.CloudFormationGenerator;
import com.awsBuilder.builder.cloudformation.service.CloudFormationTemplateRegistry;
import com.awsBuilder.builder.cloudformation.templates.EventBridgeCfnTemplate;
import com.awsBuilder.builder.cloudformation.templates.KinesisCfnTemplate;
import com.awsBuilder.builder.cloudformation.templates.LambdaCfnTemplate;
import com.awsBuilder.builder.cloudformation.templates.SnsCfnTemplate;
import com.awsBuilder.builder.cloudformation.templates.SqsCfnTemplate;
import com.awsBuilder.builder.diagram.model.DiagramDTO;
import com.awsBuilder.builder.diagram.model.EdgeDTO;
import com.awsBuilder.builder.diagram.model.GenerateResponse;
import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.terraform.model.TerraformResource;
import com.awsBuilder.builder.terraform.service.ResourceTemplateRegistry;
import com.awsBuilder.builder.terraform.service.TerraformGenerator;
import com.awsBuilder.builder.terraform.templates.EventBridgeResourceTemplate;
import com.awsBuilder.builder.terraform.templates.KinesisResourceTemplate;
import com.awsBuilder.builder.terraform.templates.LambdaResourceTemplate;
import com.awsBuilder.builder.terraform.templates.SnsResourceTemplate;
import com.awsBuilder.builder.terraform.templates.SqsResourceTemplate;
import com.awsBuilder.builder.validation.engine.DependencyResolver;
import com.awsBuilder.builder.validation.engine.ValidationEngine;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DiagramServiceEventBridgeTest {
    @Test
    void generateCodeSupportsEventBridgeRouterDiagram() {
        DiagramService service = diagramService();

        GenerateResponse response = service.generateCode(new DiagramDTO(
            List.of(
                node("eb-infra", "EVENTBRIDGE", Map.of(
                    "rule_name", "ec2-state-monitor",
                    "event_bus_name", "default",
                    "description", "Monitors EC2 instance state changes",
                    "schedule_expression", "",
                    "state", "ENABLED",
                    "tags", "Name=my-event-rule",
                    "event_pattern", "{\"source\":[\"aws.ec2\"],\"detail-type\":[\"EC2 Instance State-change Notification\"]}"
                )),
                node("eb-app", "EVENTBRIDGE", Map.of(
                    "rule_name", "app-order-events",
                    "event_bus_name", "custom-app-bus",
                    "description", "Custom application order events",
                    "schedule_expression", "",
                    "state", "ENABLED",
                    "tags", "Name=my-event-rule",
                    "event_pattern", "{\"source\":[\"com.myapp\"],\"detail-type\":[\"OrderCreated\"]}"
                )),
                node("lambda-1", "LAMBDA", Map.of(
                    "function_name", "infra-event-handler",
                    "runtime", "python3.11",
                    "handler", "index.handler",
                    "tags", "Name=my-function"
                )),
                node("sqs-1", "SQS", Map.of(
                    "queue_name", "order-processing",
                    "delay_seconds", "0",
                    "visibility_timeout", "30",
                    "message_retention_seconds", "345600",
                    "fifo_queue", "false",
                    "tags", "Name=my-queue"
                )),
                node("kinesis-1", "KINESIS", Map.of(
                    "stream_name", "event-archive-stream",
                    "shard_count", "1",
                    "retention_period", "24",
                    "stream_mode", "PROVISIONED",
                    "tags", "Name=my-stream"
                )),
                node("sns-1", "SNS", Map.of(
                    "topic_name", "infra-alerts",
                    "display_name", "Infrastructure Alerts",
                    "fifo_topic", "false",
                    "tags", "Name=my-topic"
                ))
            ),
            List.of(
                new EdgeDTO("e-eb1-lam", "eb-infra", "lambda-1", "smoothstep"),
                new EdgeDTO("e-eb2-sqs", "eb-app", "sqs-1", "smoothstep"),
                new EdgeDTO("e-eb1-kin", "eb-infra", "kinesis-1", "smoothstep"),
                new EdgeDTO("e-eb2-kin", "eb-app", "kinesis-1", "smoothstep"),
                new EdgeDTO("e-eb1-sns", "eb-infra", "sns-1", "smoothstep")
            ),
            "us-east-1"
        ));

        assertTrue(response.terraform().contains("resource \"aws_cloudwatch_event_rule\" \"eb-infra\""));
        assertTrue(response.terraform().contains("resource \"aws_cloudwatch_event_target\" \"eb-app_sqs-1\""));
        assertTrue(response.terraform().contains("resource \"aws_cloudwatch_event_target\" \"eb-infra_kinesis-1\""));
        assertTrue(response.cloudformation().contains("AWS::Events::Rule"));
        assertTrue(response.cloudformation().contains("AWS::Lambda::Permission"));
        assertTrue(response.cloudformation().contains("AWS::SQS::QueuePolicy"));
        assertTrue(response.cloudformation().contains("AWS::SNS::TopicPolicy"));
        assertTrue(response.cloudformation().contains("AWS::IAM::Role"));
    }

    private DiagramService diagramService() {
        List<TerraformResource> terraformTemplates = List.of(
            new EventBridgeResourceTemplate(),
            new LambdaResourceTemplate(),
            new SqsResourceTemplate(),
            new KinesisResourceTemplate(),
            new SnsResourceTemplate()
        );
        List<CloudFormationResource> cloudFormationTemplates = List.of(
            new EventBridgeCfnTemplate(),
            new LambdaCfnTemplate(),
            new SqsCfnTemplate(),
            new KinesisCfnTemplate(),
            new SnsCfnTemplate()
        );

        return new DiagramService(
            new ValidationEngine(),
            new DependencyResolver(),
            new TerraformGenerator(new ResourceTemplateRegistry(terraformTemplates)),
            new CloudFormationGenerator(new CloudFormationTemplateRegistry(cloudFormationTemplates))
        );
    }

    private NodeDTO node(String id, String type, Map<String, String> properties) {
        NodeDTO node = new NodeDTO();
        node.setId(id);
        node.setType(type);
        node.setProperties(new HashMap<>(properties));
        return node;
    }
}

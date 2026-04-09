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
public class SqsCfnTemplate implements CloudFormationResource {
    @Override
    public String generate(NodeDTO node, ResourceGraph graph) {
        String queueName = node.getProperties().getOrDefault("queue_name", node.getId());
        String delaySeconds = node.getProperties().getOrDefault("delay_seconds", "0");
        String visibilityTimeout = node.getProperties().getOrDefault("visibility_timeout", "30");
        String messageRetentionPeriod = node.getProperties().getOrDefault("message_retention_seconds", "345600");
        String fifoQueue = node.getProperties().getOrDefault("fifo_queue", "false");
        
        Map<String, Object> resource = new LinkedHashMap<>();
        resource.put("Type", "AWS::SQS::Queue");
        
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("QueueName", queueName);
        properties.put("DelaySeconds", Integer.parseInt(delaySeconds));
        properties.put("VisibilityTimeout", Integer.parseInt(visibilityTimeout));
        properties.put("MessageRetentionPeriod", Integer.parseInt(messageRetentionPeriod));
        
        if ("true".equals(fifoQueue)) {
            properties.put("FifoQueue", true);
            properties.put("ContentBasedDeduplication", true);
        }
        
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
        return "SQS";
    }
}

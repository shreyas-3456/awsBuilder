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
        
        DumperOptions opts = new DumperOptions();
        opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        opts.setPrettyFlow(true);
        return new Yaml(opts).dump(fragment);
    }

    @Override
    public String getResourceType() {
        return "SNS";
    }
}

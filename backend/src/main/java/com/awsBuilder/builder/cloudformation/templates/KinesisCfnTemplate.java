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
public class KinesisCfnTemplate implements CloudFormationResource {
    @Override
    public String generate(NodeDTO node, ResourceGraph graph) {
        String streamName = node.getProperties().getOrDefault("stream_name", node.getId());
        String shardCount = node.getProperties().getOrDefault("shard_count", "1");
        String retentionPeriod = node.getProperties().getOrDefault("retention_period", "24");
        
        Map<String, Object> resource = new LinkedHashMap<>();
        resource.put("Type", "AWS::Kinesis::Stream");
        
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("Name", streamName);
        properties.put("ShardCount", Integer.parseInt(shardCount));
        properties.put("RetentionPeriodHours", Integer.parseInt(retentionPeriod));
        
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
        return "KINESIS";
    }
}

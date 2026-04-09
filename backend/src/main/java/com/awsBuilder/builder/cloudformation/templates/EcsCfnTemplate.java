package com.awsBuilder.builder.cloudformation.templates;

import com.awsBuilder.builder.cloudformation.model.CloudFormationResource;
import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class EcsCfnTemplate implements CloudFormationResource {
    @Override
    public String generate(NodeDTO node, ResourceGraph graph) {
        String clusterName = node.getProperties().getOrDefault("cluster_name", node.getId());
        String containerInsights = node.getProperties().getOrDefault("container_insights", "enabled");

        Map<String, Object> resource = new LinkedHashMap<>();
        resource.put("Type", "AWS::ECS::Cluster");

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("ClusterName", clusterName);

        Map<String, Object> setting = new LinkedHashMap<>();
        setting.put("Name", "containerInsights");
        setting.put("Value", containerInsights);
        properties.put("ClusterSettings", List.of(setting));

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
        return "ECS";
    }
}

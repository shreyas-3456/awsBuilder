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
public class FargateCfnTemplate implements CloudFormationResource {
    @Override
    public String generate(NodeDTO node, ResourceGraph graph) {
        String family = node.getProperties().getOrDefault("family", node.getId());
        String cpu = node.getProperties().getOrDefault("cpu", "256");
        String memory = node.getProperties().getOrDefault("memory", "512");
        String containerName = node.getProperties().getOrDefault("container_name", node.getId() + "-container");
        String containerImage = node.getProperties().getOrDefault("container_image", "nginx:latest");
        String containerPort = node.getProperties().getOrDefault("container_port", "80");

        Map<String, Object> resource = new LinkedHashMap<>();
        resource.put("Type", "AWS::ECS::TaskDefinition");

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("Family", family);
        properties.put("Cpu", cpu);
        properties.put("Memory", memory);
        properties.put("NetworkMode", "awsvpc");
        properties.put("RequiresCompatibilities", List.of("FARGATE"));

        Map<String, Object> container = new LinkedHashMap<>();
        container.put("Name", containerName);
        container.put("Image", containerImage);
        container.put("Essential", true);

        Map<String, Object> portMapping = new LinkedHashMap<>();
        portMapping.put("ContainerPort", Integer.parseInt(containerPort));
        portMapping.put("Protocol", "tcp");
        container.put("PortMappings", List.of(portMapping));

        properties.put("ContainerDefinitions", List.of(container));

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
        return "FARGATE";
    }
}

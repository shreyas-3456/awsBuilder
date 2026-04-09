package com.awsBuilder.builder.cloudformation.templates;

import com.awsBuilder.builder.cloudformation.model.CloudFormationResource;
import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class EfsCfnTemplate implements CloudFormationResource {

    @Override
    public String generate(NodeDTO node, ResourceGraph graph) {
        Map<String, String> props = node.getProperties();

        String creationToken = props.getOrDefault("creation_token", node.getId().toLowerCase());
        String performanceMode = props.getOrDefault("performance_mode", "generalPurpose");
        String throughputMode = props.getOrDefault("throughput_mode", "bursting");
        String encrypted = props.getOrDefault("encrypted", "true");
        String provisionedThroughput = props.getOrDefault("provisioned_throughput", "");

        Map<String, Object> fragment = new LinkedHashMap<>();

        // EFS File System
        Map<String, Object> efsResource = new LinkedHashMap<>();
        efsResource.put("Type", "AWS::EFS::FileSystem");

        Map<String, Object> efsProperties = new LinkedHashMap<>();
        efsProperties.put("PerformanceMode", performanceMode);
        efsProperties.put("ThroughputMode", throughputMode);
        efsProperties.put("Encrypted", Boolean.parseBoolean(encrypted));

        if ("provisioned".equals(throughputMode) && !provisionedThroughput.isEmpty()) {
            efsProperties.put("ProvisionedThroughputInMibps", provisionedThroughput);
        }

        List<Map<String, String>> tags = new ArrayList<>();
        Map<String, String> nameTag = new LinkedHashMap<>();
        nameTag.put("Key", "Name");
        nameTag.put("Value", creationToken);
        tags.add(nameTag);
        efsProperties.put("FileSystemTags", tags);

        efsResource.put("Properties", efsProperties);
        fragment.put(node.getId(), efsResource);

        // Mount targets for connected subnets
        var subnetParents = graph.getParents(node.getId()).stream()
            .filter(parent -> "SUBNET".equals(parent.getType()))
            .toList();

        for (int i = 0; i < subnetParents.size(); i++) {
            NodeDTO subnet = subnetParents.get(i);
            Map<String, Object> mountResource = new LinkedHashMap<>();
            mountResource.put("Type", "AWS::EFS::MountTarget");

            Map<String, Object> mountProperties = new LinkedHashMap<>();
            mountProperties.put("FileSystemId", "!Ref " + node.getId());
            mountProperties.put("SubnetId", "!Ref " + subnet.getId());

            mountResource.put("Properties", mountProperties);
            fragment.put(node.getId() + "MountTarget" + i, mountResource);
        }

        DumperOptions opts = new DumperOptions();
        opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        opts.setPrettyFlow(true);
        Yaml yaml = new Yaml(opts);

        return yaml.dump(fragment);
    }

    @Override
    public String getResourceType() {
        return "EFS";
    }
}

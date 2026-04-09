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
public class EbsCfnTemplate implements CloudFormationResource {

    @Override
    public String generate(NodeDTO node, ResourceGraph graph) {
        Map<String, String> props = node.getProperties();

        String availabilityZone = props.getOrDefault("availability_zone", "us-east-1a");
        String sizeGb = props.getOrDefault("size_gb", "20");
        String volumeType = props.getOrDefault("volume_type", "gp3");
        String encrypted = props.getOrDefault("encrypted", "true");
        String iops = props.getOrDefault("iops", "");
        String throughput = props.getOrDefault("throughput", "");

        Map<String, Object> fragment = new LinkedHashMap<>();

        // EBS Volume
        Map<String, Object> volumeResource = new LinkedHashMap<>();
        volumeResource.put("Type", "AWS::EC2::Volume");

        Map<String, Object> volumeProperties = new LinkedHashMap<>();
        volumeProperties.put("AvailabilityZone", availabilityZone);
        volumeProperties.put("Size", sizeGb);
        volumeProperties.put("VolumeType", volumeType);
        volumeProperties.put("Encrypted", Boolean.parseBoolean(encrypted));

        if (!iops.isEmpty()) {
            volumeProperties.put("Iops", iops);
        }
        if (!throughput.isEmpty()) {
            volumeProperties.put("Throughput", throughput);
        }

        Map<String, Object> tag = new LinkedHashMap<>();
        tag.put("Key", "Name");
        tag.put("Value", node.getId());
        volumeProperties.put("Tags", java.util.List.of(tag));

        volumeResource.put("Properties", volumeProperties);
        fragment.put(node.getId(), volumeResource);

        // Volume Attachment if connected to EC2
        var ec2Parents = graph.getParents(node.getId()).stream()
            .filter(parent -> "EC2".equals(parent.getType()))
            .toList();

        if (!ec2Parents.isEmpty()) {
            NodeDTO ec2 = ec2Parents.get(0);
            String deviceName = props.getOrDefault("device_name", "/dev/sdf");

            Map<String, Object> attachResource = new LinkedHashMap<>();
            attachResource.put("Type", "AWS::EC2::VolumeAttachment");

            Map<String, Object> attachProps = new LinkedHashMap<>();
            attachProps.put("Device", deviceName);
            attachProps.put("VolumeId", "!Ref " + node.getId());
            attachProps.put("InstanceId", "!Ref " + ec2.getId());

            attachResource.put("Properties", attachProps);
            fragment.put(node.getId() + "Attachment", attachResource);
        }

        DumperOptions opts = new DumperOptions();
        opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        opts.setPrettyFlow(true);
        Yaml yaml = new Yaml(opts);

        return yaml.dump(fragment);
    }

    @Override
    public String getResourceType() {
        return "EBS";
    }
}

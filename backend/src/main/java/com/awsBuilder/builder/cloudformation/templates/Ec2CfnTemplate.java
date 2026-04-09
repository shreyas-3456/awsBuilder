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
public class Ec2CfnTemplate implements CloudFormationResource {
    
    @Override
    public String generate(NodeDTO node, ResourceGraph graph) {
        // Find parent SUBNET from graph
        List<NodeDTO> parents = graph.getParents(node.getId());
        NodeDTO subnetNode = parents.stream()
            .filter(n -> "SUBNET".equals(n.getType()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "EC2 instance must be connected to a Subnet"
            ));
        
        String imageId = node.getProperties().getOrDefault("image_id", "ami-0c55b159cbfafe1f0");
        String instanceType = node.getProperties().getOrDefault("instance_type", "t2.micro");
        
        // Build the EC2 resource fragment
        Map<String, Object> ec2Resource = new LinkedHashMap<>();
        ec2Resource.put("Type", "AWS::EC2::Instance");
        
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("ImageId", imageId);
        properties.put("InstanceType", instanceType);
        properties.put("SubnetId", "!Ref " + subnetNode.getId());
        
        ec2Resource.put("Properties", properties);
        
        // Create a map with the logical resource ID as the key
        Map<String, Object> fragment = new LinkedHashMap<>();
        fragment.put(node.getId(), ec2Resource);
        
        // Serialize to YAML using BLOCK style
        DumperOptions opts = new DumperOptions();
        opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        opts.setPrettyFlow(true);
        Yaml yaml = new Yaml(opts);
        
        return yaml.dump(fragment);
    }
    
    @Override
    public String getResourceType() {
        return "EC2";
    }
}

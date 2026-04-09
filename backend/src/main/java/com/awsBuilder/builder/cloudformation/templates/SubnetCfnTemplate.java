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
public class SubnetCfnTemplate implements CloudFormationResource {
    
    @Override
    public String generate(NodeDTO node, ResourceGraph graph) {
        // Find parent VPC from graph
        List<NodeDTO> parents = graph.getParents(node.getId());
        NodeDTO vpcNode = parents.stream()
            .filter(n -> "VPC".equals(n.getType()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "Subnet must be connected to a VPC"
            ));
        
        String cidrBlock = node.getProperties().getOrDefault("cidr_block", "10.0.1.0/24");
        String availabilityZone = node.getProperties().getOrDefault("availability_zone", "");
        
        // Build the Subnet resource fragment
        Map<String, Object> subnetResource = new LinkedHashMap<>();
        subnetResource.put("Type", "AWS::EC2::Subnet");
        
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("CidrBlock", cidrBlock);
        properties.put("AvailabilityZone", availabilityZone);
        properties.put("VpcId", "!Ref " + vpcNode.getId());
        
        subnetResource.put("Properties", properties);
        
        // Create a map with the logical resource ID as the key
        Map<String, Object> fragment = new LinkedHashMap<>();
        fragment.put(node.getId(), subnetResource);
        
        // Serialize to YAML using BLOCK style
        DumperOptions opts = new DumperOptions();
        opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        opts.setPrettyFlow(true);
        Yaml yaml = new Yaml(opts);
        
        return yaml.dump(fragment);
    }
    
    @Override
    public String getResourceType() {
        return "SUBNET";
    }
}

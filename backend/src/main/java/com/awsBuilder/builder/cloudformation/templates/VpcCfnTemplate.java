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
public class VpcCfnTemplate implements CloudFormationResource {
    
    @Override
    public String generate(NodeDTO node, ResourceGraph graph) {
        String cidrBlock = node.getProperties().getOrDefault("cidr_block", "10.0.0.0/16");
        String displayName = node.getDisplayName();
        
        // Build the VPC resource fragment
        Map<String, Object> vpcResource = new LinkedHashMap<>();
        vpcResource.put("Type", "AWS::EC2::VPC");
        
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("CidrBlock", cidrBlock);
        properties.put("EnableDnsSupport", true);
        properties.put("EnableDnsHostnames", true);
        
        Map<String, Object> tags = new LinkedHashMap<>();
        tags.put("Name", displayName);
        properties.put("Tags", new Object[]{tags});
        
        vpcResource.put("Properties", properties);
        
        // Create a map with the logical resource ID as the key
        Map<String, Object> fragment = new LinkedHashMap<>();
        fragment.put(node.getId(), vpcResource);
        
        // Serialize to YAML using BLOCK style
        DumperOptions opts = new DumperOptions();
        opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        opts.setPrettyFlow(true);
        Yaml yaml = new Yaml(opts);
        
        return yaml.dump(fragment);
    }
    
    @Override
    public String getResourceType() {
        return "VPC";
    }
}

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
public class IgwCfnTemplate implements CloudFormationResource {
    
    @Override
    public String generate(NodeDTO node, ResourceGraph graph) {
        // Find parent VPC from graph
        List<NodeDTO> parents = graph.getParents(node.getId());
        NodeDTO vpcNode = parents.stream()
            .filter(n -> "VPC".equals(n.getType()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "Internet Gateway must be connected to a VPC"
            ));
        
        // Create a map with both resources
        Map<String, Object> fragment = new LinkedHashMap<>();
        
        // Create InternetGateway resource
        Map<String, Object> igwResource = new LinkedHashMap<>();
        igwResource.put("Type", "AWS::EC2::InternetGateway");
        
        Map<String, Object> igwProperties = new LinkedHashMap<>();
        igwResource.put("Properties", igwProperties);
        fragment.put(node.getId(), igwResource);
        
        // Create VPCGatewayAttachment resource
        String attachmentId = node.getId() + "Attachment";
        Map<String, Object> attachmentResource = new LinkedHashMap<>();
        attachmentResource.put("Type", "AWS::EC2::VPCGatewayAttachment");
        
        Map<String, Object> attachmentProperties = new LinkedHashMap<>();
        attachmentProperties.put("InternetGatewayId", "!Ref " + node.getId());
        attachmentProperties.put("VpcId", "!Ref " + vpcNode.getId());
        
        attachmentResource.put("Properties", attachmentProperties);
        fragment.put(attachmentId, attachmentResource);
        
        // Serialize to YAML using BLOCK style
        DumperOptions opts = new DumperOptions();
        opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        opts.setPrettyFlow(true);
        Yaml yaml = new Yaml(opts);
        
        return yaml.dump(fragment);
    }
    
    @Override
    public String getResourceType() {
        return "INTERNET_GATEWAY";
    }
}

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
public class ApiGatewayCfnTemplate implements CloudFormationResource {
    @Override
    public String generate(NodeDTO node, ResourceGraph graph) {
        String name = node.getProperties().getOrDefault("name", node.getId());
        
        Map<String, Object> resource = new LinkedHashMap<>();
        resource.put("Type", "AWS::ApiGateway::RestApi");
        
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("Name", name);
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
        return "API_GATEWAY";
    }
}

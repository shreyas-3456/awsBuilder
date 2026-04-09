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
public class LambdaCfnTemplate implements CloudFormationResource {
    @Override
    public String generate(NodeDTO node, ResourceGraph graph) {
        String functionName = node.getProperties().getOrDefault("function_name", node.getId());
        String runtime = node.getProperties().getOrDefault("runtime", "nodejs18.x");
        String handler = node.getProperties().getOrDefault("handler", "index.handler");
        
        Map<String, Object> resource = new LinkedHashMap<>();
        resource.put("Type", "AWS::Lambda::Function");
        
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("FunctionName", functionName);
        properties.put("Runtime", runtime);
        properties.put("Handler", handler);
        // CloudFormation requires Role and Code for Lambda, providing dummy values for generation
        properties.put("Role", "arn:aws:iam::123456789012:role/dummy-role");
        
        Map<String, Object> code = new LinkedHashMap<>();
        code.put("ZipFile", "exports.handler = async (event) => { return 'Hello from Lambda!'; };");
        properties.put("Code", code);
        
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
        return "LAMBDA";
    }
}

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
public class XRayCfnTemplate implements CloudFormationResource {
    @Override
    public String generate(NodeDTO node, ResourceGraph graph) {
        String ruleName = node.getProperties().getOrDefault("rule_name", node.getId());
        String priority = node.getProperties().getOrDefault("priority", "1000");
        String fixedRate = node.getProperties().getOrDefault("fixed_rate", "0.05");
        String reservoirSize = node.getProperties().getOrDefault("reservoir_size", "1");
        String serviceName = node.getProperties().getOrDefault("service_name", "*");
        String serviceType = node.getProperties().getOrDefault("service_type", "*");
        String host = node.getProperties().getOrDefault("host", "*");
        String httpMethod = node.getProperties().getOrDefault("http_method", "*");
        String urlPath = node.getProperties().getOrDefault("url_path", "*");

        Map<String, Object> resource = new LinkedHashMap<>();
        resource.put("Type", "AWS::XRay::SamplingRule");

        Map<String, Object> samplingRule = new LinkedHashMap<>();
        samplingRule.put("RuleName", ruleName);
        samplingRule.put("Priority", Integer.parseInt(priority));
        samplingRule.put("FixedRate", Double.parseDouble(fixedRate));
        samplingRule.put("ReservoirSize", Integer.parseInt(reservoirSize));
        samplingRule.put("ServiceName", serviceName);
        samplingRule.put("ServiceType", serviceType);
        samplingRule.put("Host", host);
        samplingRule.put("HTTPMethod", httpMethod);
        samplingRule.put("URLPath", urlPath);
        samplingRule.put("ResourceARN", "*");
        samplingRule.put("Version", 1);

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("SamplingRule", samplingRule);

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
        return "XRAY";
    }
}

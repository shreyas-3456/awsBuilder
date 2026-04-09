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
public class CloudTrailCfnTemplate implements CloudFormationResource {
    @Override
    public String generate(NodeDTO node, ResourceGraph graph) {
        String trailName = node.getProperties().getOrDefault("trail_name", node.getId());
        String s3BucketName = node.getProperties().getOrDefault("s3_bucket_name", "my-cloudtrail-logs");
        String isMultiRegion = node.getProperties().getOrDefault("is_multi_region_trail", "true");
        String enableLogFileValidation = node.getProperties().getOrDefault("enable_log_file_validation", "true");
        String includeGlobalEvents = node.getProperties().getOrDefault("include_global_service_events", "true");
        String enableLogging = node.getProperties().getOrDefault("enable_logging", "true");

        Map<String, Object> resource = new LinkedHashMap<>();
        resource.put("Type", "AWS::CloudTrail::Trail");

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("TrailName", trailName);
        properties.put("S3BucketName", s3BucketName);
        properties.put("IsMultiRegionTrail", Boolean.parseBoolean(isMultiRegion));
        properties.put("EnableLogFileValidation", Boolean.parseBoolean(enableLogFileValidation));
        properties.put("IncludeGlobalServiceEvents", Boolean.parseBoolean(includeGlobalEvents));
        properties.put("IsLogging", Boolean.parseBoolean(enableLogging));

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
        return "CLOUDTRAIL";
    }
}

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
public class S3CfnTemplate implements CloudFormationResource {
    
    @Override
    public String generate(NodeDTO node, ResourceGraph graph) {
        // Validate bucket_name property
        String bucketName = node.getProperties().getOrDefault("bucket_name", "").toString().trim();
        if (bucketName.isEmpty()) {
            throw new IllegalArgumentException(
                "S3 bucket must have a non-empty bucket_name property"
            );
        }
        
        String versioning = node.getProperties().getOrDefault("versioning", "false").toString();
        
        // Build the S3 resource fragment
        Map<String, Object> s3Resource = new LinkedHashMap<>();
        s3Resource.put("Type", "AWS::S3::Bucket");
        
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("BucketName", bucketName);
        
        // Add versioning configuration if enabled
        if ("true".equals(versioning)) {
            Map<String, Object> versioningConfig = new LinkedHashMap<>();
            versioningConfig.put("Status", "Enabled");
            properties.put("VersioningConfiguration", versioningConfig);
        }
        
        s3Resource.put("Properties", properties);
        
        // Create a map with the logical resource ID as the key
        Map<String, Object> fragment = new LinkedHashMap<>();
        fragment.put(node.getId(), s3Resource);
        
        // Serialize to YAML using BLOCK style
        DumperOptions opts = new DumperOptions();
        opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        opts.setPrettyFlow(true);
        Yaml yaml = new Yaml(opts);
        
        return yaml.dump(fragment);
    }
    
    @Override
    public String getResourceType() {
        return "S3";
    }
}

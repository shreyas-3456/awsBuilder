package com.awsBuilder.builder.terraform.templates;

import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;
import com.awsBuilder.builder.terraform.model.TerraformResource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CloudTrailResourceTemplate implements TerraformResource {
    @Override
    public String getResourceType() {
        return "CLOUDTRAIL";
    }

    @Override
    public String generate(NodeDTO node, ResourceGraph graph, String regionAlias) {
        String id = node.getId();
        Map<String, String> props = node.getProperties();
        String trailName = props.getOrDefault("trail_name", id);
        String s3BucketName = props.getOrDefault("s3_bucket_name", "my-cloudtrail-logs");
        String isMultiRegion = props.getOrDefault("is_multi_region_trail", "true");
        String enableLogFileValidation = props.getOrDefault("enable_log_file_validation", "true");
        String includeGlobalEvents = props.getOrDefault("include_global_service_events", "true");
        String enableLogging = props.getOrDefault("enable_logging", "true");

        String provider = (regionAlias != null && !regionAlias.isEmpty()) ? "  provider = aws." + regionAlias + "\n" : "";

        return String.format("""
            resource "aws_cloudtrail" "%s" {
            %s  name                          = "%s"
              s3_bucket_name                = "%s"
              is_multi_region_trail         = %s
              enable_log_file_validation    = %s
              include_global_service_events = %s
              enable_logging                = %s

              tags = {
                Name = "%s"
              }
            }
            """, id, provider, trailName, s3BucketName, isMultiRegion,
                enableLogFileValidation, includeGlobalEvents, enableLogging, id);
    }
}

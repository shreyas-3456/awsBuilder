package com.awsBuilder.builder.terraform.templates;

import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;
import com.awsBuilder.builder.terraform.model.TerraformResource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class XRayResourceTemplate implements TerraformResource {
    @Override
    public String getResourceType() {
        return "XRAY";
    }

    @Override
    public String generate(NodeDTO node, ResourceGraph graph, String regionAlias) {
        String id = node.getId();
        Map<String, String> props = node.getProperties();
        String ruleName = props.getOrDefault("rule_name", id);
        String priority = props.getOrDefault("priority", "1000");
        String fixedRate = props.getOrDefault("fixed_rate", "0.05");
        String reservoirSize = props.getOrDefault("reservoir_size", "1");
        String serviceName = props.getOrDefault("service_name", "*");
        String serviceType = props.getOrDefault("service_type", "*");
        String host = props.getOrDefault("host", "*");
        String httpMethod = props.getOrDefault("http_method", "*");
        String urlPath = props.getOrDefault("url_path", "*");

        String provider = (regionAlias != null && !regionAlias.isEmpty()) ? "  provider = aws." + regionAlias + "\n" : "";

        return String.format("""
            resource "aws_xray_sampling_rule" "%s" {
            %s  rule_name      = "%s"
              priority       = %s
              version        = 1
              reservoir_size = %s
              fixed_rate     = %s
              url_path       = "%s"
              host           = "%s"
              http_method    = "%s"
              service_type   = "%s"
              service_name   = "%s"
              resource_arn   = "*"

              tags = {
                Name = "%s"
              }
            }
            """, id, provider, ruleName, priority, reservoirSize, fixedRate,
                urlPath, host, httpMethod, serviceType, serviceName, id);
    }
}

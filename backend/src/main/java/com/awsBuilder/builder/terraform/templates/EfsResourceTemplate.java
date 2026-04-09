package com.awsBuilder.builder.terraform.templates;

import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;
import com.awsBuilder.builder.terraform.model.TerraformResource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EfsResourceTemplate implements TerraformResource {
    @Override
    public String getResourceType() {
        return "EFS";
    }

    @Override
    public String generate(NodeDTO node, ResourceGraph graph, String regionAlias) {
        String id = node.getId();
        Map<String, String> props = node.getProperties();

        String creationToken = props.getOrDefault("creation_token", id.toLowerCase());
        String performanceMode = props.getOrDefault("performance_mode", "generalPurpose");
        String throughputMode = props.getOrDefault("throughput_mode", "bursting");
        String encrypted = props.getOrDefault("encrypted", "true");

        String provider = (regionAlias != null && !regionAlias.isEmpty()) ? "  provider = aws." + regionAlias + "\n" : "";

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("""
            resource "aws_efs_file_system" "%s" {
            %s  creation_token   = "%s"
              performance_mode = "%s"
              throughput_mode  = "%s"
              encrypted        = %s
            """, id, provider, creationToken, performanceMode, throughputMode, encrypted));

        String provisionedThroughput = props.getOrDefault("provisioned_throughput", "");
        if ("provisioned".equals(throughputMode) && !provisionedThroughput.isEmpty()) {
            sb.append(String.format("  provisioned_throughput_in_mibps = %s\n", provisionedThroughput));
        }

        sb.append(String.format("""

              tags = {
                Name = "%s"
              }
            }
            """, id));

        // Create mount targets for each connected subnet
        var subnetParents = graph.getParents(id).stream()
            .filter(parent -> "SUBNET".equals(parent.getType()))
            .toList();

        for (int i = 0; i < subnetParents.size(); i++) {
            NodeDTO subnet = subnetParents.get(i);
            sb.append(String.format("""

                resource "aws_efs_mount_target" "%s_mt_%d" {
                %s  file_system_id = aws_efs_file_system.%s.id
                  subnet_id      = aws_subnet.%s.id
                }
                """, id, i, provider, id, subnet.getId()));
        }

        return sb.toString();
    }
}

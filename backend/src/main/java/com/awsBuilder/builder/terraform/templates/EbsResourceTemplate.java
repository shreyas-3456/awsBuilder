package com.awsBuilder.builder.terraform.templates;

import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;
import com.awsBuilder.builder.terraform.model.TerraformResource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EbsResourceTemplate implements TerraformResource {
    @Override
    public String getResourceType() {
        return "EBS";
    }

    @Override
    public String generate(NodeDTO node, ResourceGraph graph, String regionAlias) {
        String id = node.getId();
        Map<String, String> props = node.getProperties();

        String availabilityZone = props.getOrDefault("availability_zone", "us-east-1a");
        String sizeGb = props.getOrDefault("size_gb", "20");
        String volumeType = props.getOrDefault("volume_type", "gp3");
        String encrypted = props.getOrDefault("encrypted", "true");
        String iops = props.getOrDefault("iops", "");
        String throughput = props.getOrDefault("throughput", "");

        String provider = (regionAlias != null && !regionAlias.isEmpty()) ? "  provider = aws." + regionAlias + "\n" : "";

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("""
            resource "aws_ebs_volume" "%s" {
            %s  availability_zone = "%s"
              size              = %s
              type              = "%s"
              encrypted         = %s
            """, id, provider, availabilityZone, sizeGb, volumeType, encrypted));

        if (!iops.isEmpty()) {
            sb.append(String.format("  iops              = %s\n", iops));
        }
        if (!throughput.isEmpty()) {
            sb.append(String.format("  throughput         = %s\n", throughput));
        }

        sb.append(String.format("""

              tags = {
                Name = "%s"
              }
            }
            """, id));

        // Attach to EC2 if connected
        var ec2Parents = graph.getParents(id).stream()
            .filter(parent -> "EC2".equals(parent.getType()))
            .toList();

        if (!ec2Parents.isEmpty()) {
            NodeDTO ec2 = ec2Parents.get(0);
            String deviceName = props.getOrDefault("device_name", "/dev/sdf");
            sb.append(String.format("""

                resource "aws_volume_attachment" "%s_attach" {
                %s  device_name = "%s"
                  volume_id   = aws_ebs_volume.%s.id
                  instance_id = aws_instance.%s.id
                }
                """, id, provider, deviceName, id, ec2.getId()));
        }

        return sb.toString();
    }
}

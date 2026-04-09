package com.awsBuilder.builder.terraform.templates;

import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;
import com.awsBuilder.builder.terraform.model.TerraformResource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class KinesisResourceTemplate implements TerraformResource {
    @Override
    public String getResourceType() {
        return "KINESIS";
    }

    @Override
    public String generate(NodeDTO node, ResourceGraph graph, String regionAlias) {
        String id = node.getId();
        Map<String, String> props = node.getProperties();
        String streamName = props.getOrDefault("stream_name", id);
        String shardCount = props.getOrDefault("shard_count", "1");
        String retentionPeriod = props.getOrDefault("retention_period", "24");
        String streamMode = props.getOrDefault("stream_mode", "PROVISIONED");
        
        String provider = (regionAlias != null && !regionAlias.isEmpty()) ? "  provider = aws." + regionAlias + "\n" : "";

        return String.format("""
            resource "aws_kinesis_stream" "%s" {
            %s  name             = "%s"
              shard_count      = %s
              retention_period = %s

              stream_mode_details {
                stream_mode = "%s"
              }

              tags = {
                Name = "%s"
              }
            }
            """, id, provider, streamName, shardCount, retentionPeriod, streamMode, id);
    }
}

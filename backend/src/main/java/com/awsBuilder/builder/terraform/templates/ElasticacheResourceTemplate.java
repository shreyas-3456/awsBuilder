package com.awsBuilder.builder.terraform.templates;

import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;
import com.awsBuilder.builder.terraform.model.TerraformResource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ElasticacheResourceTemplate implements TerraformResource {
    @Override
    public String getResourceType() {
        return "ELASTICACHE";
    }

    @Override
    public String generate(NodeDTO node, ResourceGraph graph, String regionAlias) {
        String id = node.getId();
        Map<String, String> props = node.getProperties();
        
        String clusterId = props.getOrDefault("cluster_id", id.toLowerCase());
        String engine = props.getOrDefault("engine", "redis");
        String nodeType = props.getOrDefault("node_type", "cache.t3.micro");
        String numCacheNodes = props.getOrDefault("num_cache_nodes", "1");
        String engineVersion = props.getOrDefault("engine_version", "7.0");
        String port = props.getOrDefault("port", "6379");
        
        String provider = (regionAlias != null && !regionAlias.isEmpty()) ? "  provider = aws." + regionAlias + "\n" : "";

        return String.format("""
            resource "aws_elasticache_cluster" "%s" {
            %s  cluster_id           = "%s"
              engine               = "%s"
              node_type            = "%s"
              num_cache_nodes      = %s
              engine_version       = "%s"
              port                 = %s

              tags = {
                Name = "%s"
              }
            }
            """, id, provider, clusterId, engine, nodeType, numCacheNodes, engineVersion, port, id);
    }
}

package com.awsBuilder.builder.cloudformation.templates;

import com.awsBuilder.builder.cloudformation.model.CloudFormationResource;
import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ElasticacheCfnTemplate implements CloudFormationResource {

    @Override
    public String generate(NodeDTO node, ResourceGraph graph) {
        List<NodeDTO> subnetNodes = graph.getParents(node.getId()).stream()
            .filter(parent -> "SUBNET".equals(parent.getType()))
            .toList();

        if (subnetNodes.isEmpty()) {
            throw new IllegalStateException(
                "ElastiCache cluster must be connected to at least one Subnet"
            );
        }

        Map<String, String> props = node.getProperties();
        String clusterId = props.getOrDefault("cluster_id", node.getId().toLowerCase());
        String engine = props.getOrDefault("engine", "redis");
        String nodeType = props.getOrDefault("node_type", "cache.t3.micro");
        String numCacheNodes = props.getOrDefault("num_cache_nodes", "1");
        String engineVersion = props.getOrDefault("engine_version", "7.0");
        String port = props.getOrDefault("port", "6379");

        Map<String, Object> fragment = new LinkedHashMap<>();

        String subnetGroupId = node.getId() + "SubnetGroup";
        Map<String, Object> subnetGroupResource = new LinkedHashMap<>();
        subnetGroupResource.put("Type", "AWS::ElastiCache::SubnetGroup");

        Map<String, Object> subnetGroupProperties = new LinkedHashMap<>();
        subnetGroupProperties.put("Description", "Subnet group for ElastiCache cluster");

        List<String> subnetIds = new ArrayList<>();
        for (NodeDTO subnetNode : subnetNodes) {
            subnetIds.add("!Ref " + subnetNode.getId());
        }
        subnetGroupProperties.put("SubnetIds", subnetIds);

        subnetGroupResource.put("Properties", subnetGroupProperties);
        fragment.put(subnetGroupId, subnetGroupResource);

        Map<String, Object> clusterResource = new LinkedHashMap<>();
        clusterResource.put("Type", "AWS::ElastiCache::CacheCluster");

        Map<String, Object> clusterProperties = new LinkedHashMap<>();
        clusterProperties.put("ClusterName", clusterId);
        clusterProperties.put("Engine", engine);
        clusterProperties.put("CacheNodeType", nodeType);
        clusterProperties.put("NumCacheNodes", numCacheNodes);
        clusterProperties.put("EngineVersion", engineVersion);
        clusterProperties.put("Port", port);
        clusterProperties.put("CacheSubnetGroupName", "!Ref " + subnetGroupId);

        clusterResource.put("Properties", clusterProperties);
        fragment.put(node.getId(), clusterResource);

        DumperOptions opts = new DumperOptions();
        opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        opts.setPrettyFlow(true);
        Yaml yaml = new Yaml(opts);

        return yaml.dump(fragment);
    }

    @Override
    public String getResourceType() {
        return "ELASTICACHE";
    }
}

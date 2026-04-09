package com.awsBuilder.builder.cloudformation.templates;

import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ElasticacheCfnTemplateTest {

    private final ElasticacheCfnTemplate template = new ElasticacheCfnTemplate();

    @Test
    void testGetResourceType() {
        assertEquals("ELASTICACHE", template.getResourceType());
    }

    @Test
    void testGenerateElasticacheFragment() {
        NodeDTO subnetNode = new NodeDTO();
        subnetNode.setId("AppSubnet");
        subnetNode.setType("SUBNET");
        subnetNode.setProperties(new HashMap<>());

        NodeDTO cacheNode = new NodeDTO();
        cacheNode.setId("AppCache");
        cacheNode.setType("ELASTICACHE");
        cacheNode.setProperties(new HashMap<>(Map.of(
            "cluster_id", "app-cache",
            "engine", "redis",
            "node_type", "cache.t3.small",
            "num_cache_nodes", "1",
            "engine_version", "7.0",
            "port", "6379"
        )));

        ResourceGraph graph = new ResourceGraph();
        graph.addNode(subnetNode);
        graph.addNode(cacheNode);
        graph.addEdge(cacheNode.getId(), subnetNode.getId());

        String yamlFragment = template.generate(cacheNode, graph);

        Yaml yaml = new Yaml();
        Map<String, Object> parsed = yaml.load(yamlFragment);

        assertTrue(parsed.containsKey("AppCacheSubnetGroup"));
        assertTrue(parsed.containsKey("AppCache"));

        Map<String, Object> subnetGroup = (Map<String, Object>) parsed.get("AppCacheSubnetGroup");
        assertEquals("AWS::ElastiCache::SubnetGroup", subnetGroup.get("Type"));
        Map<String, Object> subnetGroupProperties = (Map<String, Object>) subnetGroup.get("Properties");
        assertEquals(List.of("!Ref AppSubnet"), subnetGroupProperties.get("SubnetIds"));

        Map<String, Object> cluster = (Map<String, Object>) parsed.get("AppCache");
        assertEquals("AWS::ElastiCache::CacheCluster", cluster.get("Type"));
        Map<String, Object> clusterProperties = (Map<String, Object>) cluster.get("Properties");
        assertEquals("app-cache", clusterProperties.get("ClusterName"));
        assertEquals("redis", clusterProperties.get("Engine"));
        assertEquals("cache.t3.small", clusterProperties.get("CacheNodeType"));
        assertEquals("1", clusterProperties.get("NumCacheNodes"));
        assertEquals("7.0", clusterProperties.get("EngineVersion"));
        assertEquals("6379", clusterProperties.get("Port"));
        assertEquals("!Ref AppCacheSubnetGroup", clusterProperties.get("CacheSubnetGroupName"));
    }

    @Test
    void testElasticacheMissingSubnetParentThrows() {
        NodeDTO cacheNode = new NodeDTO();
        cacheNode.setId("OrphanCache");
        cacheNode.setType("ELASTICACHE");
        cacheNode.setProperties(new HashMap<>());

        ResourceGraph graph = new ResourceGraph();
        graph.addNode(cacheNode);

        assertThrows(IllegalStateException.class, () -> template.generate(cacheNode, graph));
    }
}

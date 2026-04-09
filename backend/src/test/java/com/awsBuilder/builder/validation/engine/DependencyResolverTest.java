package com.awsBuilder.builder.validation.engine;

import com.awsBuilder.builder.validation.exception.CircularDependencyException;
import com.awsBuilder.builder.validation.engine.DependencyResolver;
import com.awsBuilder.builder.diagram.model.DiagramDTO;
import com.awsBuilder.builder.diagram.model.EdgeDTO;
import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class DependencyResolverTest {

    private DependencyResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new DependencyResolver();
    }

    @Test
    @DisplayName("Build graph with nodes and edges")
    void buildGraphCreatesCorrectStructure() {
        // Edge: subnet1 -> vpc1 means "subnet depends on vpc"
        DiagramDTO diagram = new DiagramDTO(
            List.of(
                node("vpc1", "VPC"),
                node("subnet1", "SUBNET")
            ),
            List.of(new EdgeDTO("e1", "subnet1", "vpc1", "connects")),
            "us-east-1"
        );

        ResourceGraph graph = resolver.buildGraph(diagram);

        assertNotNull(graph.getNode("vpc1"));
        assertNotNull(graph.getNode("subnet1"));
        // getParents returns resource dependencies (outgoing edge targets)
        assertEquals(1, graph.getParents("subnet1").size());
        assertEquals("vpc1", graph.getParents("subnet1").get(0).getId());
    }

    @Test
    @DisplayName("Topological sort produces correct dependency order")
    void topologicalSortOrder() {
        // Edges: subnet1->vpc1 (subnet depends on vpc), ec2_1->subnet1 (ec2 depends on subnet)
        DiagramDTO diagram = new DiagramDTO(
            List.of(
                node("vpc1", "VPC"),
                node("subnet1", "SUBNET"),
                node("ec2_1", "EC2")
            ),
            List.of(
                new EdgeDTO("e1", "subnet1", "vpc1", "connects"),
                new EdgeDTO("e2", "ec2_1", "subnet1", "connects")
            ),
            "us-east-1"
        );

        ResourceGraph graph = resolver.buildGraph(diagram);
        List<NodeDTO> sorted = resolver.topologicalSort(graph);
        
        List<String> sortedIds = sorted.stream()
            .map(NodeDTO::getId)
            .collect(Collectors.toList());
        
        // VPC (no deps) must come before Subnet, Subnet must come before EC2
        assertTrue(sortedIds.indexOf("vpc1") < sortedIds.indexOf("subnet1"),
            "VPC should come before Subnet, but got: " + sortedIds);
        assertTrue(sortedIds.indexOf("subnet1") < sortedIds.indexOf("ec2_1"),
            "Subnet should come before EC2, but got: " + sortedIds);
    }

    @Test
    @DisplayName("Circular dependency throws CircularDependencyException")
    void circularDependencyThrows() {
        DiagramDTO diagram = new DiagramDTO(
            List.of(
                node("a", "VPC"),
                node("b", "VPC")
            ),
            List.of(
                new EdgeDTO("e1", "a", "b", "connects"),
                new EdgeDTO("e2", "b", "a", "connects")
            ),
            "us-east-1"
        );

        ResourceGraph graph = resolver.buildGraph(diagram);
        assertThrows(CircularDependencyException.class, () -> resolver.topologicalSort(graph));
    }

    @Test
    @DisplayName("Single node with no edges returns that node")
    void singleNodeSort() {
        DiagramDTO diagram = new DiagramDTO(
            List.of(node("vpc1", "VPC")),
            List.of(),
            "us-east-1"
        );

        ResourceGraph graph = resolver.buildGraph(diagram);
        List<NodeDTO> sorted = resolver.topologicalSort(graph);
        
        assertEquals(1, sorted.size());
        assertEquals("vpc1", sorted.get(0).getId());
    }

    @Test
    @DisplayName("Independent nodes are all present in sort result")
    void independentNodesAllPresent() {
        DiagramDTO diagram = new DiagramDTO(
            List.of(
                node("vpc1", "VPC"),
                node("s3_1", "S3"),
                node("vpc2", "VPC")
            ),
            List.of(),
            "us-east-1"
        );

        ResourceGraph graph = resolver.buildGraph(diagram);
        List<NodeDTO> sorted = resolver.topologicalSort(graph);
        
        assertEquals(3, sorted.size());
        Set<String> ids = sorted.stream().map(NodeDTO::getId).collect(Collectors.toSet());
        assertTrue(ids.containsAll(Set.of("vpc1", "s3_1", "vpc2")));
    }

    private NodeDTO node(String id, String type) {
        NodeDTO n = new NodeDTO();
        n.setId(id);
        n.setType(type);
        n.setProperties(new HashMap<>());
        return n;
    }
}

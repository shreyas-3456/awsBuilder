package com.awsBuilder.builder.diagram.model;

import java.util.*;
import java.util.stream.Collectors;

public class ResourceGraph {
    
    private final Map<String, NodeDTO> nodes;
    private final Map<String, Set<String>> adjacencyList; // nodeId -> outgoing edge targets
    private final Map<String, Set<String>> reverseAdjacencyList; // nodeId -> incoming edge sources
    
    public ResourceGraph() {
        this.nodes = new HashMap<>();
        this.adjacencyList = new HashMap<>();
        this.reverseAdjacencyList = new HashMap<>();
    }
    
    public void addNode(NodeDTO node) {
        nodes.put(node.getId(), node);
        adjacencyList.putIfAbsent(node.getId(), new HashSet<>());
        reverseAdjacencyList.putIfAbsent(node.getId(), new HashSet<>());
    }
    
    public void addEdge(String sourceId, String targetId) {
        adjacencyList.computeIfAbsent(sourceId, k -> new HashSet<>()).add(targetId);
        reverseAdjacencyList.computeIfAbsent(targetId, k -> new HashSet<>()).add(sourceId);
    }
    
    public NodeDTO getNode(String nodeId) {
        return nodes.get(nodeId);
    }
    
    /**
     * Returns the "resource parents" of a node.
     * An edge source→target means "source depends on target" (e.g., SUBNET→VPC),
     * so the targets of outgoing edges are the resource parents/dependencies.
     */
    public List<NodeDTO> getParents(String nodeId) {
        return adjacencyList.getOrDefault(nodeId, new HashSet<>()).stream()
                .map(nodes::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    /**
     * Returns the "resource children" of a node.
     * Nodes that depend on this node (have edges pointing to this node).
     */
    public List<NodeDTO> getChildren(String nodeId) {
        return reverseAdjacencyList.getOrDefault(nodeId, new HashSet<>()).stream()
                .map(nodes::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    /**
     * Returns IDs of outgoing edge targets for traversal (topological sort).
     */
    public Set<String> getChildrenIds(String nodeId) {
        return adjacencyList.getOrDefault(nodeId, new HashSet<>());
    }
    
    public Set<String> getAllNodeIds() {
        return nodes.keySet();
    }
}

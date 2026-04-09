package com.awsBuilder.builder.validation.engine;

import com.awsBuilder.builder.validation.exception.CircularDependencyException;
import com.awsBuilder.builder.diagram.model.DiagramDTO;
import com.awsBuilder.builder.diagram.model.EdgeDTO;
import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DependencyResolver {
    
    public ResourceGraph buildGraph(DiagramDTO diagram) {
        ResourceGraph graph = new ResourceGraph();
        
        // Add all nodes to graph
        for (NodeDTO node : diagram.getNodes()) {
            graph.addNode(node);
        }
        
        // Add edges representing dependencies
        for (EdgeDTO edge : diagram.getEdges()) {
            graph.addEdge(edge.getSource(), edge.getTarget());
        }
        
        return graph;
    }
    
    public List<NodeDTO> topologicalSort(ResourceGraph graph) 
            throws CircularDependencyException {
        
        List<NodeDTO> sorted = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> visiting = new HashSet<>();
        
        for (String nodeId : graph.getAllNodeIds()) {
            if (!visited.contains(nodeId)) {
                dfs(nodeId, graph, visited, visiting, sorted);
            }
        }
        
        return sorted;
    }
    
    private void dfs(String nodeId, ResourceGraph graph, 
                     Set<String> visited, Set<String> visiting,
                     List<NodeDTO> sorted) throws CircularDependencyException {
        
        if (visiting.contains(nodeId)) {
            throw new CircularDependencyException(
                "Circular dependency detected involving: " + nodeId
            );
        }
        
        if (visited.contains(nodeId)) {
            return;
        }
        
        visiting.add(nodeId);
        
        for (String childId : graph.getChildrenIds(nodeId)) {
            dfs(childId, graph, visited, visiting, sorted);
        }
        
        visiting.remove(nodeId);
        visited.add(nodeId);
        sorted.add(graph.getNode(nodeId));
    }
}

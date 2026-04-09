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
public class RdsCfnTemplate implements CloudFormationResource {
    
    @Override
    public String generate(NodeDTO node, ResourceGraph graph) {
        // Find all parent SUBNET nodes from graph
        List<NodeDTO> parents = graph.getParents(node.getId());
        List<NodeDTO> subnetNodes = parents.stream()
            .filter(n -> "SUBNET".equals(n.getType()))
            .toList();
        
        if (subnetNodes.isEmpty()) {
            throw new IllegalStateException(
                "RDS instance must be connected to at least one Subnet"
            );
        }
        
        String engine = node.getProperties().getOrDefault("engine", "mysql");
        String dbInstanceClass = node.getProperties().getOrDefault("db_instance_class", "db.t2.micro");
        String allocatedStorage = node.getProperties().getOrDefault("allocated_storage", "20");
        String dbName = node.getProperties().getOrDefault("db_name", "mydb");
        String masterUsername = node.getProperties().getOrDefault("master_username", "admin");
        String masterUserPassword = node.getProperties().getOrDefault("master_user_password", "password123");
        
        // Create a map with both resources
        Map<String, Object> fragment = new LinkedHashMap<>();
        
        // Create DBSubnetGroup resource
        String subnetGroupId = node.getId() + "SubnetGroup";
        Map<String, Object> subnetGroupResource = new LinkedHashMap<>();
        subnetGroupResource.put("Type", "AWS::RDS::DBSubnetGroup");
        
        Map<String, Object> subnetGroupProperties = new LinkedHashMap<>();
        subnetGroupProperties.put("DBSubnetGroupDescription", "Subnet group for RDS instance");
        
        List<String> subnetIds = new ArrayList<>();
        for (NodeDTO subnet : subnetNodes) {
            subnetIds.add("!Ref " + subnet.getId());
        }
        subnetGroupProperties.put("SubnetIds", subnetIds);
        
        subnetGroupResource.put("Properties", subnetGroupProperties);
        fragment.put(subnetGroupId, subnetGroupResource);
        
        // Create DBInstance resource
        Map<String, Object> dbInstanceResource = new LinkedHashMap<>();
        dbInstanceResource.put("Type", "AWS::RDS::DBInstance");
        
        Map<String, Object> dbInstanceProperties = new LinkedHashMap<>();
        dbInstanceProperties.put("Engine", engine);
        dbInstanceProperties.put("DBInstanceClass", dbInstanceClass);
        dbInstanceProperties.put("AllocatedStorage", allocatedStorage);
        dbInstanceProperties.put("DBName", dbName);
        dbInstanceProperties.put("MasterUsername", masterUsername);
        dbInstanceProperties.put("MasterUserPassword", masterUserPassword);
        dbInstanceProperties.put("DBSubnetGroupName", "!Ref " + subnetGroupId);
        
        dbInstanceResource.put("Properties", dbInstanceProperties);
        fragment.put(node.getId(), dbInstanceResource);
        
        // Serialize to YAML using BLOCK style
        DumperOptions opts = new DumperOptions();
        opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        opts.setPrettyFlow(true);
        Yaml yaml = new Yaml(opts);
        
        return yaml.dump(fragment);
    }
    
    @Override
    public String getResourceType() {
        return "RDS";
    }
}

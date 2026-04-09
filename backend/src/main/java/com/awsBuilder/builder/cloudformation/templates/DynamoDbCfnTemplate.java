package com.awsBuilder.builder.cloudformation.templates;

import com.awsBuilder.builder.cloudformation.model.CloudFormationResource;
import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class DynamoDbCfnTemplate implements CloudFormationResource {
    @Override
    public String generate(NodeDTO node, ResourceGraph graph) {
        String tableName = node.getProperties().getOrDefault("table_name", node.getId());
        String billingMode = node.getProperties().getOrDefault("billing_mode", "PAY_PER_REQUEST");
        String hashKey = node.getProperties().getOrDefault("hash_key", "id");

        Map<String, Object> resource = new LinkedHashMap<>();
        resource.put("Type", "AWS::DynamoDB::Table");
        
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("TableName", tableName);
        properties.put("BillingMode", billingMode);
        
        Map<String, Object> attributeDef = new LinkedHashMap<>();
        attributeDef.put("AttributeName", hashKey);
        attributeDef.put("AttributeType", "S");
        properties.put("AttributeDefinitions", Collections.singletonList(attributeDef));
        
        Map<String, Object> keySchema = new LinkedHashMap<>();
        keySchema.put("AttributeName", hashKey);
        keySchema.put("KeyType", "HASH");
        properties.put("KeySchema", Collections.singletonList(keySchema));
        
        resource.put("Properties", properties);
        
        Map<String, Object> fragment = new LinkedHashMap<>();
        fragment.put(node.getId(), resource);
        
        DumperOptions opts = new DumperOptions();
        opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        opts.setPrettyFlow(true);
        return new Yaml(opts).dump(fragment);
    }
    
    @Override
    public String getResourceType() {
        return "DYNAMODB";
    }
}

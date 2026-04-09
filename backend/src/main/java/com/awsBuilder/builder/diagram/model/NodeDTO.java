package com.awsBuilder.builder.diagram.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NodeDTO {
    
    @JsonProperty("id")
    @NotBlank(message = "node id must not be blank")
    private String id;
    
    @JsonProperty("type")
    @NotBlank(message = "node type must not be blank")
    private String type;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("properties")
    private Map<String, String> properties = new HashMap<>();
    
    /**
     * Returns the display name for this node, falling back to the id if name is not set.
     */
    public String getDisplayName() {
        return (name != null && !name.isBlank()) ? name : id;
    }
}

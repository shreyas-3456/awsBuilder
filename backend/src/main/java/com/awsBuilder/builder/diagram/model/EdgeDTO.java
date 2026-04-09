package com.awsBuilder.builder.diagram.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EdgeDTO {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("source")
    private String source;
    
    @JsonProperty("target")
    private String target;
    
    @JsonProperty("type")
    private String type;
}

package com.awsBuilder.builder.diagram.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiagramDTO {
    
    @JsonProperty("nodes")
    @NotNull(message = "nodes list must not be null")
    @Size(min = 1, message = "diagram must contain at least one node")
    @Valid
    private List<NodeDTO> nodes = new ArrayList<>();
    
    @JsonProperty("edges")
    @NotNull(message = "edges list must not be null")
    private List<EdgeDTO> edges = new ArrayList<>();

    @JsonProperty("region")
    @jakarta.validation.constraints.NotBlank(message = "region must be provided and not empty")
    private String region;
}

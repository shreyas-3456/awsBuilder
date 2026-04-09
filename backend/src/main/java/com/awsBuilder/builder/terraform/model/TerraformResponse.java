package com.awsBuilder.builder.terraform.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TerraformResponse {
    
    @JsonProperty("terraform")
    private String terraform;
}

package com.awsBuilder.builder.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("status")
    private Integer status;

    @JsonProperty("code")
    private String code;

    @JsonProperty("error")
    private String error;

    @JsonProperty("path")
    private String path;

    @JsonProperty("requestId")
    private String requestId;

    @JsonProperty("details")
    private List<String> details = new ArrayList<>();

    public ErrorResponse(String error) {
        this.error = error;
        this.details = new ArrayList<>();
    }

    public ErrorResponse(String error, List<String> details) {
        this.error = error;
        this.details = safeDetails(details);
    }

    public static ErrorResponse of(int status,
                                   ErrorCode code,
                                   String error,
                                   String path,
                                   String requestId,
                                   List<String> details) {
        ErrorResponse response = new ErrorResponse();
        response.timestamp = Instant.now().toString();
        response.status = status;
        response.code = code.name();
        response.error = error;
        response.path = path;
        response.requestId = requestId;
        response.details = safeDetails(details);
        return response;
    }

    private static List<String> safeDetails(List<String> details) {
        return details != null ? new ArrayList<>(details) : new ArrayList<>();
    }
}

package com.awsBuilder.builder.exception;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ValidationException extends RuntimeException {
    
    private final List<String> details;
    
    public ValidationException(String message) {
        super(message);
        this.details = new ArrayList<>();
    }
    
    public ValidationException(String message, List<String> details) {
        super(message);
        this.details = details != null ? details : new ArrayList<>();
    }
}

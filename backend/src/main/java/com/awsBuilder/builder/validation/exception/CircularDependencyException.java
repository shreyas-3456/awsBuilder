package com.awsBuilder.builder.validation.exception;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CircularDependencyException extends RuntimeException {
    
    private final List<String> cycle;
    
    public CircularDependencyException(String message) {
        super(message);
        this.cycle = new ArrayList<>();
    }
    
    public CircularDependencyException(String message, List<String> cycle) {
        super(message);
        this.cycle = cycle != null ? cycle : new ArrayList<>();
    }
}

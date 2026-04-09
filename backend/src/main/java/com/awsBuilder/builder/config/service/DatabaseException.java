package com.awsBuilder.builder.config.service;

/**
 * Custom exception for database-related errors in catalog operations.
 */
public class DatabaseException extends RuntimeException {
    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}

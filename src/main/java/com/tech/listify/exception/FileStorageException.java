package com.tech.listify.exception;

import lombok.Getter;

@Getter
public class FileStorageException extends RuntimeException {

    public enum ErrorType {
        CLIENT_ERROR,
        SERVER_ERROR
    }

    private final ErrorType errorType;

    public FileStorageException(String message, ErrorType errorType) {
        super(message);
        this.errorType = errorType;
    }

    public FileStorageException(String message, Throwable cause, ErrorType errorType) {
        super(message, cause);
        this.errorType = errorType;
    }
}
package com.rd162.web;

/**
 * Request model for publishing messages 
 */
public class PublishRequest {
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
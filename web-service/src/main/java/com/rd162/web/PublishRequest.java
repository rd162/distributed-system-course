package com.rd162.web;

public class PublishRequest {
    private String message;

    public PublishRequest(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
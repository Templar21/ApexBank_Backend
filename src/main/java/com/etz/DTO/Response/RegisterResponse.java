package com.etz.DTO.Response;

public class RegisterResponse {
    private String message;
    private String token;

    public RegisterResponse(String message, String token) {
        this.message = message;
        this.token = token;
    }

    public RegisterResponse() {
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }
}
package com.diskdesktop.model;

/**
 * Ответ аутентификации, содержит JWT-токен.
 */
public class AuthResponse {

    private String token;

    public AuthResponse() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}


package com.diskdesktop.service;

/**
 * Унифицированное исключение для ошибок при работе с backend API.
 */
public class ApiException extends Exception {

    private final int statusCode;
    private final String rawBody;

    public ApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
        this.rawBody = null;
    }

    public ApiException(String message, int statusCode, String rawBody) {
        super(message);
        this.statusCode = statusCode;
        this.rawBody = rawBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getRawBody() {
        return rawBody;
    }
}


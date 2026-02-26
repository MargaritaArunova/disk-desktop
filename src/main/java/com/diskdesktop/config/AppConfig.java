package com.diskdesktop.config;

/**
 * Конфигурация приложения (путь к backend API и т.п.).
 */
public class AppConfig {

    private String backendBaseUrl;

    public AppConfig() {
    }

    public AppConfig(String backendBaseUrl) {
        this.backendBaseUrl = backendBaseUrl;
    }

    public String getBackendBaseUrl() {
        return backendBaseUrl;
    }

    public void setBackendBaseUrl(String backendBaseUrl) {
        this.backendBaseUrl = backendBaseUrl;
    }
}


package com.diskdesktop.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Чтение и запись пользовательской конфигурации (~/.diskdesktop/config.properties).
 */
public class ConfigService {

    private static final String CONFIG_DIR_NAME = ".diskdesktop";
    private static final String CONFIG_FILE_NAME = "config.properties";
    private static final String KEY_BACKEND_BASE_URL = "backend.baseUrl";

    public AppConfig loadConfig() {
        File file = getConfigFile();
        Properties properties = new Properties();
        if (file.exists()) {
            try (FileInputStream in = new FileInputStream(file)) {
                properties.load(in);
            } catch (IOException ignored) {
            }
        }

        AppConfig config = new AppConfig();
        String backendUrl = properties.getProperty(KEY_BACKEND_BASE_URL);
        config.setBackendBaseUrl(backendUrl);
        return config;
    }

    public void saveConfig(AppConfig config) {
        File file = getConfigFile();
        File dir = file.getParentFile();
        if (!dir.exists() && !dir.mkdirs()) {
            return;
        }

        Properties properties = new Properties();
        if (config.getBackendBaseUrl() != null) {
            properties.setProperty(KEY_BACKEND_BASE_URL, config.getBackendBaseUrl());
        }

        try (FileOutputStream out = new FileOutputStream(file)) {
            properties.store(out, "Disk Desktop Client configuration");
        } catch (IOException ignored) {
        }
    }

    private File getConfigFile() {
        String userHome = System.getProperty("user.home");
        return new File(userHome, CONFIG_DIR_NAME + File.separator + CONFIG_FILE_NAME);
    }
}


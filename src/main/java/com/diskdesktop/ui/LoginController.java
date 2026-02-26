package com.diskdesktop.ui;

import com.diskdesktop.api.AuthApi;
import com.diskdesktop.config.ApiClientConfig;
import com.diskdesktop.config.AppConfig;
import com.diskdesktop.config.ConfigService;
import com.diskdesktop.model.AuthRequest;
import com.diskdesktop.model.AuthResponse;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import retrofit2.Response;

/**
 * Контроллер экрана логина.
 */
public class LoginController {

    @FXML
    private TextField baseUrlField;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private CheckBox rememberBaseUrlCheckBox;
    @FXML
    private Button loginButton;
    @FXML
    private Label statusLabel;

    private LoginListener loginListener;
    private final ConfigService configService = new ConfigService();

    public void setInitialBaseUrl(String baseUrl) {
        baseUrlField.setText(baseUrl);
    }

    public void setLoginListener(LoginListener loginListener) {
        this.loginListener = loginListener;
    }

    @FXML
    public void initialize() {
        statusLabel.setText("");
    }

    @FXML
    private void onLoginClicked() {
        String baseUrl = baseUrlField.getText() != null ? baseUrlField.getText().trim() : "";
        String username = usernameField.getText() != null ? usernameField.getText().trim() : "";
        String password = passwordField.getText() != null ? passwordField.getText() : "";

        if (baseUrl.isEmpty() || username.isEmpty() || password.isEmpty()) {
            showError("Все поля обязательны");
            return;
        }

        setControlsDisabled(true);
        statusLabel.setText("Выполняется вход...");

        Task<String> loginTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                ApiClientConfig apiClientConfig = new ApiClientConfig(baseUrl, null);
                AuthApi authApi = apiClientConfig.getAuthApi();

                Response<AuthResponse> response =
                        authApi.login(new AuthRequest(username, password)).execute();
                if (!response.isSuccessful() || response.body() == null) {
                    throw new RuntimeException("Не удалось выполнить вход: " + response.code());
                }
                return response.body().getToken();
            }
        };

        loginTask.setOnSucceeded(e -> {
            String token = loginTask.getValue();
            if (rememberBaseUrlCheckBox.isSelected()) {
                AppConfig appConfig = new AppConfig(baseUrl);
                configService.saveConfig(appConfig);
            }
            if (loginListener != null) {
                loginListener.onLoginSuccess(baseUrl, token);
            }
        });

        loginTask.setOnFailed(e -> {
            Throwable ex = loginTask.getException();
            showError(ex != null ? ex.getMessage() : "Неизвестная ошибка при входе");
            setControlsDisabled(false);
        });

        new Thread(loginTask, "login-task").start();
    }

    private void setControlsDisabled(boolean disabled) {
        baseUrlField.setDisable(disabled);
        usernameField.setDisable(disabled);
        passwordField.setDisable(disabled);
        rememberBaseUrlCheckBox.setDisable(disabled);
        loginButton.setDisable(disabled);
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            statusLabel.setText(message);
        });
    }

    @FunctionalInterface
    public interface LoginListener {
        void onLoginSuccess(String baseUrl, String token);
    }
}


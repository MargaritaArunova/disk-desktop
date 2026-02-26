package com.diskdesktop;

import com.diskdesktop.config.ApiClientConfig;
import com.diskdesktop.config.AppConfig;
import com.diskdesktop.config.ConfigService;
import com.diskdesktop.service.BackendServiceImpl;
import com.diskdesktop.ui.LoginController;
import com.diskdesktop.ui.MainController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Главный класс JavaFX-приложения.
 */
public class MainApp extends Application {

    private final ConfigService configService = new ConfigService();

    @Override
    public void start(Stage primaryStage) throws IOException {
        AppConfig storedConfig = configService.loadConfig();
        String envBaseUrl = System.getenv("BACKEND_BASE_URL");
        String initialBaseUrl;
        if (envBaseUrl != null && !envBaseUrl.isBlank()) {
            initialBaseUrl = envBaseUrl;
        } else if (storedConfig.getBackendBaseUrl() != null && !storedConfig.getBackendBaseUrl().isBlank()) {
            initialBaseUrl = storedConfig.getBackendBaseUrl();
        } else {
            initialBaseUrl = "http://localhost:8080/api";
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/login_view.fxml"));
        Scene scene = new Scene(loader.load());

        LoginController loginController = loader.getController();
        loginController.setInitialBaseUrl(initialBaseUrl);
        loginController.setLoginListener((baseUrl, token) ->
                Platform.runLater(() -> openMainWindow(primaryStage, baseUrl, token)));

        primaryStage.setTitle("Disk Desktop Client - Вход");
        primaryStage.setScene(scene);
        primaryStage.setWidth(480);
        primaryStage.setHeight(320);
        primaryStage.show();
    }

    private void openMainWindow(Stage stage, String baseUrl, String token) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/main_view.fxml"));
            Scene scene = new Scene(loader.load());

            MainController controller = loader.getController();

            ApiClientConfig config = new ApiClientConfig(baseUrl, token);
            BackendServiceImpl backendService =
                    new BackendServiceImpl(config.getFileApi(), config.getDirectoryApi());
            controller.setBackendService(backendService);
            controller.init();

            stage.setTitle("Disk Desktop Client");
            stage.setScene(scene);
            stage.setWidth(900);
            stage.setHeight(600);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException("Не удалось открыть главное окно", e);
        }
    }
}


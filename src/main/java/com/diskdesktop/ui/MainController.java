package com.diskdesktop.ui;

import com.diskdesktop.model.DirectoryInfo;
import com.diskdesktop.model.FileInfo;
import com.diskdesktop.service.ApiException;
import com.diskdesktop.service.BackendService;
import javafx.application.Platform;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Главный контроллер JavaFX UI.
 * Отвечает за отображение дерева директорий, списка файлов и пользовательские действия.
 */
public class MainController {

    @FXML
    private TextField pathField;
    @FXML
    private TreeView<DirectoryInfo> directoryTree;
    @FXML
    private TableView<FileInfo> fileTable;
    @FXML
    private TableColumn<FileInfo, String> nameColumn;
    @FXML
    private TableColumn<FileInfo, Number> sizeColumn;
    @FXML
    private TableColumn<FileInfo, String> modifiedColumn;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label statusLabel;

    private BackendService backendService;
    private String currentDirectory = ".";

    public void setBackendService(BackendService backendService) {
        this.backendService = backendService;
    }

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        sizeColumn.setCellValueFactory(data -> new SimpleLongProperty(data.getValue().getSize()));
        modifiedColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLastModified()));

        progressBar.setProgress(0);

        directoryTree.setShowRoot(true);
        directoryTree.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(DirectoryInfo item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });

        directoryTree.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem != null && newItem.getValue() != null) {
                String path = newItem.getValue().getPath();
                if (path != null && !path.isEmpty()) {
                    loadDirectory(path);
                }
            }
        });

        fileTable.setRowFactory(tv -> {
            TableRow<FileInfo> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    FileInfo file = row.getItem();
                    onDownloadFile(file);
                }
            });
            return row;
        });
    }

    /**
     * Инициализация контроллера после установки BackendService.
     */
    public void init() {
        DirectoryInfo rootInfo = new DirectoryInfo();
        rootInfo.setName("/");
        rootInfo.setPath(".");
        TreeItem<DirectoryInfo> rootItem = createDirectoryItem(rootInfo);
        directoryTree.setRoot(rootItem);
        // лениво подгружаем детей корня при первом раскрытии
        loadDirectory(rootInfo.getPath());
        loadChildren(rootItem);
    }

    @FXML
    private void onUpClicked() {
        if (".".equals(currentDirectory) || currentDirectory.isEmpty()) {
            return;
        }
        int idx = currentDirectory.lastIndexOf('/');
        String parent = idx > 0 ? currentDirectory.substring(0, idx) : ".";
        loadDirectory(parent);
    }

    @FXML
    private void onCreateDirClicked() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Создать директорию");
        dialog.setContentText("Имя директории:");
        dialog.showAndWait().ifPresent(name -> runTask(
                "Создание директории...",
                () -> {
                    backendService.createDirectory(currentDirectory, name);
                    return null;
                },
                r -> loadDirectory(currentDirectory)
        ));
    }

    @FXML
    private void onUploadClicked() {
        FileChooser chooser = new FileChooser();
        File file = chooser.showOpenDialog(fileTable.getScene().getWindow());
        if (file == null) {
            return;
        }
        runTask(
                "Загрузка файла...",
                () -> {
                    backendService.uploadFile(currentDirectory, file);
                    return null;
                },
                r -> loadDirectory(currentDirectory)
        );
    }

    @FXML
    private void onRefreshClicked() {
        loadDirectory(currentDirectory);
    }

    private void onDownloadFile(FileInfo fileInfo) {
        FileChooser chooser = new FileChooser();
        chooser.setInitialFileName(fileInfo.getName());
        File target = chooser.showSaveDialog(fileTable.getScene().getWindow());
        if (target == null) {
            return;
        }

        runTask(
                "Скачивание файла...",
                () -> {
                    backendService.downloadFile(currentDirectory, fileInfo.getName(), target);
                    return null;
                },
                r -> {
                    // можно обновить статус или показать уведомление
                }
        );
    }

    private void loadDirectory(String directory) {
        runTask(
                "Загрузка директории...",
                () -> {
                    List<FileInfo> files = backendService.listFiles(directory);
                    Platform.runLater(() -> {
                        fileTable.getItems().setAll(files);
                        pathField.setText(directory);
                        currentDirectory = directory;
                    });
                    return null;
                },
                r -> {
                }
        );
    }

    private TreeItem<DirectoryInfo> createDirectoryItem(DirectoryInfo directoryInfo) {
        TreeItem<DirectoryInfo> item = new TreeItem<>(directoryInfo);
        // добавляем "пустышку", чтобы у элемента был маркер разворачиваемости
        item.getChildren().add(new TreeItem<>());
        item.expandedProperty().addListener((obs, wasExpanded, isExpanded) -> {
            if (isExpanded) {
                loadChildren(item);
            }
        });
        return item;
    }

    private void loadChildren(TreeItem<DirectoryInfo> parentItem) {
        DirectoryInfo dir = parentItem.getValue();
        if (dir == null) {
            return;
        }

        // если дети уже были загружены (не "пустышка"), не перезагружаем
        if (!parentItem.getChildren().isEmpty()
                && parentItem.getChildren().get(0).getValue() != null) {
            return;
        }

        String path = dir.getPath();
        runTask(
                "Загрузка поддиректорий...",
                () -> {
                    List<DirectoryInfo> dirs = backendService.listDirectories(path);
                    Platform.runLater(() -> {
                        parentItem.getChildren().clear();
                        for (DirectoryInfo child : dirs) {
                            parentItem.getChildren().add(createDirectoryItem(child));
                        }
                    });
                    return null;
                },
                r -> {
                }
        );
    }

    private <T> void runTask(String status,
                             Callable<T> callable,
                             Consumer<T> onSuccess) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                updateMessage(status);
                updateProgress(-1, 1); // индикатор "indeterminate"
                try {
                    return callable.call();
                } catch (ApiException e) {
                    throw e;
                } catch (Exception e) {
                    throw e;
                }
            }
        };

        task.setOnSucceeded(e -> {
            progressBar.progressProperty().unbind();
            progressBar.setProgress(0);
            statusLabel.textProperty().unbind();
            statusLabel.setText("Готово");
            onSuccess.accept(task.getValue());
        });

        task.setOnFailed(e -> {
            progressBar.progressProperty().unbind();
            progressBar.setProgress(0);
            statusLabel.textProperty().unbind();
            statusLabel.setText("Ошибка");
            Throwable ex = task.getException();
            showErrorDialog("Ошибка операции", ex != null ? ex.getMessage() : "Неизвестная ошибка");
        });

        progressBar.progressProperty().bind(task.progressProperty());
        statusLabel.textProperty().bind(task.messageProperty());

        Thread thread = new Thread(task, "backend-task");
        thread.setDaemon(true);
        thread.start();
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}


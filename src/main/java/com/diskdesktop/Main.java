package com.diskdesktop;

import javafx.application.Application;

/**
 * Точка входа для JavaFX-приложения.
 * Делегирует запуск классу {@link MainApp}, чтобы корректно работать с JavaFX launcher.
 */
public class Main {

    public static void main(String[] args) {
        Application.launch(MainApp.class, args);
    }
}

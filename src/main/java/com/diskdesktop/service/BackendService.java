package com.diskdesktop.service;

import com.diskdesktop.model.DirectoryInfo;
import com.diskdesktop.model.FileInfo;

import java.io.File;
import java.util.List;

/**
 * Высокоуровневый сервис для работы с файловым backend-ом.
 */
public interface BackendService {

    List<FileInfo> listFiles(String directory) throws ApiException;

    List<DirectoryInfo> listDirectories(String directory) throws ApiException;

    FileInfo uploadFile(String directory, File localFile) throws ApiException;

    void downloadFile(String directory, String filename, File targetFile) throws ApiException;

    DirectoryInfo createDirectory(String parentDirectory, String name) throws ApiException;
}


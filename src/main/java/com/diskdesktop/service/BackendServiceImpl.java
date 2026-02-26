package com.diskdesktop.service;

import com.diskdesktop.api.DirectoryApi;
import com.diskdesktop.api.FileApi;
import com.diskdesktop.model.DirectoryInfo;
import com.diskdesktop.model.FileInfo;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Реализация {@link BackendService} на основе Retrofit-клиентов.
 */
public class BackendServiceImpl implements BackendService {

    private final FileApi fileApi;
    private final DirectoryApi directoryApi;

    public BackendServiceImpl(FileApi fileApi, DirectoryApi directoryApi) {
        this.fileApi = fileApi;
        this.directoryApi = directoryApi;
    }

    @Override
    public List<FileInfo> listFiles(String directory) throws ApiException {
        try {
            Response<List<FileInfo>> response = fileApi.listFiles(encodePath(directory)).execute();
            return handleResponse(response);
        } catch (IOException e) {
            throw new ApiException("Network error while listing files", e);
        }
    }

    @Override
    public List<DirectoryInfo> listDirectories(String directory) throws ApiException {
        try {
            Response<List<DirectoryInfo>> response = directoryApi.listDirectories(encodePath(directory)).execute();
            return handleResponse(response);
        } catch (IOException e) {
            throw new ApiException("Network error while listing directories", e);
        }
    }

    @Override
    public FileInfo uploadFile(String directory, File localFile) throws ApiException {
        try {
            RequestBody fileBody = RequestBody.create(localFile, MediaType.parse("application/octet-stream"));
            MultipartBody.Part part = MultipartBody.Part.createFormData("file", localFile.getName(), fileBody);

            Response<FileInfo> response = fileApi.uploadFile(encodePath(directory), part).execute();
            return handleResponse(response);
        } catch (IOException e) {
            throw new ApiException("Network error while uploading file", e);
        }
    }

    @Override
    public void downloadFile(String directory, String filename, File targetFile) throws ApiException {
        try {
            Response<ResponseBody> response =
                    fileApi.downloadFile(encodePath(directory), filename).execute();
            ResponseBody body = handleResponse(response);

            try (ResponseBody ignored = body;
                 InputStream in = body.byteStream();
                 FileOutputStream out = new FileOutputStream(targetFile)) {
                byte[] buffer = new byte[8 * 1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
            }
        } catch (IOException e) {
            throw new ApiException("Network error while downloading file", e);
        }
    }

    @Override
    public DirectoryInfo createDirectory(String parentDirectory, String name) throws ApiException {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("name", name);
            Response<DirectoryInfo> response =
                    directoryApi.createDirectory(encodePath(parentDirectory), body).execute();
            return handleResponse(response);
        } catch (IOException e) {
            throw new ApiException("Network error while creating directory", e);
        }
    }

    private String encodePath(String path) {
        if (path == null || path.isEmpty() || ".".equals(path)) {
            return ".";
        }
        // Здесь можно добавить URL-кодирование, если backend этого требует.
        return path;
    }

    private static <T> T handleResponse(Response<T> response) throws ApiException {
        if (response.isSuccessful()) {
            return response.body();
        }
        String errorBody = "";
        try {
            if (response.errorBody() != null) {
                errorBody = response.errorBody().string();
            }
        } catch (IOException ignored) {
        }
        throw new ApiException(
                "API error: " + response.code() + " " + response.message(),
                response.code(),
                errorBody
        );
    }
}


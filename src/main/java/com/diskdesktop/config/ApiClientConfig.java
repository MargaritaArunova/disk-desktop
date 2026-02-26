package com.diskdesktop.config;

import com.diskdesktop.api.AuthApi;
import com.diskdesktop.api.DirectoryApi;
import com.diskdesktop.api.FileApi;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Конфигурация Retrofit-клиента для взаимодействия с backend API.
 */
public class ApiClientConfig {

    private final FileApi fileApi;
    private final DirectoryApi directoryApi;
    private final AuthApi authApi;

    public ApiClientConfig(String baseUrl, String token) {
        String resolvedBaseUrl = resolveBaseUrl(baseUrl);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

        Interceptor authInterceptor = chain -> {
            Request original = chain.request();
            Request.Builder builder = original.newBuilder();
            if (token != null && !token.isEmpty()) {
                builder.header("Authorization", "Bearer " + token);
            }
            return chain.proceed(builder.build());
        };

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(resolvedBaseUrl.endsWith("/") ? resolvedBaseUrl : resolvedBaseUrl + "/")
                .client(client)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        this.fileApi = retrofit.create(FileApi.class);
        this.directoryApi = retrofit.create(DirectoryApi.class);
        this.authApi = retrofit.create(AuthApi.class);
    }

    /**
     * Разрешает базовый URL с учётом переменной окружения BACKEND_BASE_URL.
     */
    public static String resolveBaseUrl(String explicitBaseUrl) {
        String envBaseUrl = System.getenv("BACKEND_BASE_URL");
        if (envBaseUrl != null && !envBaseUrl.isBlank()) {
            return envBaseUrl;
        }
        if (explicitBaseUrl != null && !explicitBaseUrl.isBlank()) {
            return explicitBaseUrl;
        }
        return "http://localhost:8080/api";
    }

    public FileApi getFileApi() {
        return fileApi;
    }

    public DirectoryApi getDirectoryApi() {
        return directoryApi;
    }

    public AuthApi getAuthApi() {
        return authApi;
    }
}


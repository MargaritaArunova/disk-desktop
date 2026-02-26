package com.diskdesktop.api;

import com.diskdesktop.model.DirectoryInfo;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

import java.util.List;
import java.util.Map;

/**
 * Retrofit-интерфейс для работы с директориями.
 */
public interface DirectoryApi {

    @GET("directories/{directory}")
    Call<List<DirectoryInfo>> listDirectories(@Path("directory") String directory);

    @POST("directories/{directory}")
    Call<DirectoryInfo> createDirectory(@Path("directory") String directory,
                                        @Body Map<String, String> body);
}


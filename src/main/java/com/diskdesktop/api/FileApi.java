package com.diskdesktop.api;

import com.diskdesktop.model.FileInfo;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

import java.util.List;

/**
 * Retrofit-интерфейс для работы с файлами.
 */
public interface FileApi {

    @GET("files/{directory}")
    Call<List<FileInfo>> listFiles(@Path("directory") String directory);

    @Multipart
    @POST("files/{directory}")
    Call<FileInfo> uploadFile(@Path("directory") String directory,
                              @Part MultipartBody.Part file);

    @GET("files/{directory}/{filename}")
    Call<ResponseBody> downloadFile(@Path("directory") String directory,
                                    @Path("filename") String filename);
}


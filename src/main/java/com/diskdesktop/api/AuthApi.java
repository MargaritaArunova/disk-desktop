package com.diskdesktop.api;

import com.diskdesktop.model.AuthRequest;
import com.diskdesktop.model.AuthResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Retrofit-интерфейс для аутентификации.
 */
public interface AuthApi {

    @POST("auth/login")
    Call<AuthResponse> login(@Body AuthRequest request);
}


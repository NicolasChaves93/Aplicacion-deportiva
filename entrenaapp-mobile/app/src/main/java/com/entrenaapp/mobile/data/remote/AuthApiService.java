package com.entrenaapp.mobile.data.remote;

import com.entrenaapp.mobile.data.remote.model.AuthResponse;
import com.entrenaapp.mobile.data.remote.model.LoginRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApiService {
    @POST("api/v1/auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);
}

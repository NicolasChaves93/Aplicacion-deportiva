package com.entrenaapp.mobile.data.remote.repository;

import android.content.Context;

import com.entrenaapp.mobile.data.remote.model.AuthResponse;
import com.entrenaapp.mobile.data.remote.model.LoginRequest;
import com.entrenaapp.mobile.data.remote.AuthApiService;
import com.entrenaapp.mobile.data.remote.RetrofitClient;

import retrofit2.Call;

public class AuthRepository {
    private final AuthApiService service;

    public AuthRepository(Context context) {
        service = RetrofitClient.getAuthService(context);
    }

    public Call<AuthResponse> login(String email, String password) {
        return service.login(new LoginRequest(email, password));
    }
}

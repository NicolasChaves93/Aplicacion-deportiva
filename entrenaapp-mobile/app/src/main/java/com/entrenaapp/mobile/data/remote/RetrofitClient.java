package com.entrenaapp.mobile.data.remote;

import java.util.concurrent.TimeUnit;

import com.entrenaapp.mobile.BuildConfig;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    // Definida en app/build.gradle.kts a partir de local.properties (API_BASE_URL).
    // Por defecto apunta a 10.0.2.2 (emulador); para un dispositivo fisico usar la URL
    // publica de ngrok (o la IP de la maquina en la red local).
    private static final String BASE_URL = BuildConfig.API_BASE_URL;

    private static Retrofit retrofit;

    private RetrofitClient() {
    }

    public static AuthApiService getAuthService() {
        return getRetrofit().create(AuthApiService.class);
    }

    private static Retrofit getRetrofit() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}

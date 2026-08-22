package com.entrenaapp.mobile.data.remote;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import com.entrenaapp.mobile.BuildConfig;
import com.entrenaapp.mobile.data.session.SessionManager;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
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

    public static AuthApiService getAuthService(Context context) {
        return getRetrofit(context).create(AuthApiService.class);
    }

    public static SyncApiService getSyncService(Context context) {
        return getRetrofit(context).create(SyncApiService.class);
    }

    private static Retrofit getRetrofit(Context context) {
        if (retrofit == null) {
            Context appContext = context.getApplicationContext();
            SessionManager sessionManager = new SessionManager(appContext);

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

            // Agrega el JWT a toda peticion cuando hay sesion guardada. Los
            // endpoints publicos (/auth/**) ignoran el header si llega, asi
            // que no hace falta excluirlos aqui.
            Interceptor authInterceptor = chain -> {
                Request original = chain.request();
                String token = sessionManager.getToken();
                if (token == null) {
                    return chain.proceed(original);
                }
                Request autenticada = original.newBuilder()
                        .header("Authorization", "Bearer " + token)
                        .build();
                return chain.proceed(autenticada);
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .addInterceptor(authInterceptor)
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

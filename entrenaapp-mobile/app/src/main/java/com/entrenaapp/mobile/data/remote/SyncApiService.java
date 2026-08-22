package com.entrenaapp.mobile.data.remote;

import com.entrenaapp.mobile.data.remote.model.AsistenciaSyncRequest;
import com.entrenaapp.mobile.data.remote.model.AsistenciaSyncResponse;
import com.entrenaapp.mobile.data.remote.model.DeportistaSyncRequest;
import com.entrenaapp.mobile.data.remote.model.DeportistaSyncResponse;
import com.entrenaapp.mobile.data.remote.model.EntrenamientoSyncRequest;
import com.entrenaapp.mobile.data.remote.model.EntrenamientoSyncResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface SyncApiService {
    // Los tres POST son upsert del lado servidor: si el id ya existe,
    // actualiza esa fila en vez de crear una nueva.
    @POST("api/v1/deportistas")
    Call<Void> sincronizarDeportista(@Body DeportistaSyncRequest request);

    @POST("api/v1/entrenamientos")
    Call<Void> sincronizarEntrenamiento(@Body EntrenamientoSyncRequest request);

    @POST("api/v1/asistencias")
    Call<Void> sincronizarAsistencia(@Body AsistenciaSyncRequest request);

    // Sincronizacion de bajada: reconciliar cambios hechos del lado servidor
    // (ej. directo en la base de datos) que la app nunca empujo.
    @GET("api/v1/deportistas")
    Call<List<DeportistaSyncResponse>> obtenerDeportistas();

    @GET("api/v1/deportistas/{id}")
    Call<DeportistaSyncResponse> obtenerDeportistaPorId(@Path("id") String id);

    @GET("api/v1/entrenamientos")
    Call<List<EntrenamientoSyncResponse>> obtenerEntrenamientos();

    @GET("api/v1/asistencias/entrenamiento/{entrenamientoId}")
    Call<List<AsistenciaSyncResponse>> obtenerAsistenciasPorEntrenamiento(@Path("entrenamientoId") String entrenamientoId);
}

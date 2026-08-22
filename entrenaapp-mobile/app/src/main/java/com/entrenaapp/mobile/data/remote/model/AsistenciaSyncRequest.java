package com.entrenaapp.mobile.data.remote.model;

/**
 * Refleja AsistenciaRequest de la API. entrenamientoId/deportistaId deben
 * ser los mismos ids ya sincronizados de esas entidades (por eso el
 * SyncRepository sube Deportistas y Entrenamientos antes que Asistencias).
 */
public class AsistenciaSyncRequest {
    private final String id;
    private final String entrenamientoId;
    private final String deportistaId;
    private final Boolean asistio;
    private final String observacion;

    public AsistenciaSyncRequest(String id, String entrenamientoId, String deportistaId,
                                  Boolean asistio, String observacion) {
        this.id = id;
        this.entrenamientoId = entrenamientoId;
        this.deportistaId = deportistaId;
        this.asistio = asistio;
        this.observacion = observacion;
    }
}

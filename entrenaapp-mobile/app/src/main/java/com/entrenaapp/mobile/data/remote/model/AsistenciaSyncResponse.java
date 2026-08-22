package com.entrenaapp.mobile.data.remote.model;

// Refleja AsistenciaResponse de la API. Solo para la sincronizacion de
// bajada.
public class AsistenciaSyncResponse {
    private String id;
    private String entrenamientoId;
    private String deportistaId;
    private String deportistaNombre;
    private Boolean asistio;
    private String observacion;

    public String getId() {
        return id;
    }

    public String getEntrenamientoId() {
        return entrenamientoId;
    }

    public String getDeportistaId() {
        return deportistaId;
    }

    public Boolean getAsistio() {
        return asistio;
    }

    public String getObservacion() {
        return observacion;
    }
}

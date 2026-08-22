package com.entrenaapp.mobile.data.remote.model;

// Refleja EntrenamientoResponse de la API (fecha llega como String
// yyyy-MM-dd, que es como Gson serializa un LocalDate). Solo para la
// sincronizacion de bajada.
public class EntrenamientoSyncResponse {
    private String id;
    private String fecha;
    private String tipo;
    private Integer duracionMin;
    private String intensidad;
    private Double latitud;
    private Double longitud;
    private String observacionAudioPath;

    public String getId() {
        return id;
    }

    public String getFecha() {
        return fecha;
    }

    public String getTipo() {
        return tipo;
    }

    public Integer getDuracionMin() {
        return duracionMin;
    }

    public String getIntensidad() {
        return intensidad;
    }

    public Double getLatitud() {
        return latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public String getObservacionAudioPath() {
        return observacionAudioPath;
    }
}

package com.entrenaapp.mobile.data.remote.model;

/**
 * Refleja EntrenamientoRequest de la API. "fecha" viaja como String
 * yyyy-MM-dd (formato ISO), que Spring deserializa directo a LocalDate.
 */
public class EntrenamientoSyncRequest {
    private final String id;
    private final String fecha;
    private final String tipo;
    private final Integer duracionMin;
    private final String intensidad;
    private final Double latitud;
    private final Double longitud;
    private final String observacionAudioPath;

    public EntrenamientoSyncRequest(String id, String fecha, String tipo, Integer duracionMin,
                                     String intensidad, Double latitud, Double longitud,
                                     String observacionAudioPath) {
        this.id = id;
        this.fecha = fecha;
        this.tipo = tipo;
        this.duracionMin = duracionMin;
        this.intensidad = intensidad;
        this.latitud = latitud;
        this.longitud = longitud;
        this.observacionAudioPath = observacionAudioPath;
    }
}

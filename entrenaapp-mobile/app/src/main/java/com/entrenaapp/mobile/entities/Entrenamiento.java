package com.entrenaapp.mobile.entities;

public class Entrenamiento {
    private String id;
    private String fecha;
    private String tipo;
    private int duracionMin;
    private String intensidad;
    private Double latitud;
    private Double longitud;
    private String audioPath;
    private String syncStatus;

    public Entrenamiento() {}

    public Entrenamiento(String id, String fecha, String tipo, int duracionMin, String intensidad,
                          Double latitud, Double longitud, String audioPath) {
        this.id = id;
        this.fecha = fecha;
        this.tipo = tipo;
        this.duracionMin = duracionMin;
        this.intensidad = intensidad;
        this.latitud = latitud;
        this.longitud = longitud;
        this.audioPath = audioPath;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getDuracionMin() {
        return duracionMin;
    }

    public void setDuracionMin(int duracionMin) {
        this.duracionMin = duracionMin;
    }

    public String getIntensidad() {
        return intensidad;
    }

    public void setIntensidad(String intensidad) {
        this.intensidad = intensidad;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public String getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(String syncStatus) {
        this.syncStatus = syncStatus;
    }

    @Override
    public String toString() {
        return fecha + " - " + tipo + " (" + duracionMin + " min)";
    }
}

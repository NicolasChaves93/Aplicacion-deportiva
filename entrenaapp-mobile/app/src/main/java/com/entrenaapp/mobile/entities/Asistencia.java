package com.entrenaapp.mobile.entities;

public class Asistencia {
    private String id;
    private String entrenamientoId;
    private String deportistaId;
    private boolean asistio;
    private String observacion;
    private String syncStatus;

    public Asistencia() {}

    public Asistencia(String id, String entrenamientoId, String deportistaId, boolean asistio, String observacion) {
        this.id = id;
        this.entrenamientoId = entrenamientoId;
        this.deportistaId = deportistaId;
        this.asistio = asistio;
        this.observacion = observacion;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEntrenamientoId() {
        return entrenamientoId;
    }

    public void setEntrenamientoId(String entrenamientoId) {
        this.entrenamientoId = entrenamientoId;
    }

    public String getDeportistaId() {
        return deportistaId;
    }

    public void setDeportistaId(String deportistaId) {
        this.deportistaId = deportistaId;
    }

    public boolean isAsistio() {
        return asistio;
    }

    public void setAsistio(boolean asistio) {
        this.asistio = asistio;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public String getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(String syncStatus) {
        this.syncStatus = syncStatus;
    }

    @Override
    public String toString() {
        return (asistio ? "Presente" : "Ausente") + (observacion != null && !observacion.isEmpty() ? " - " + observacion : "");
    }
}

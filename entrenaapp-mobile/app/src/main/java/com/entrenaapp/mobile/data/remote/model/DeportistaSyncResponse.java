package com.entrenaapp.mobile.data.remote.model;

// Refleja DeportistaResponse de la API. Se usa solo para la sincronizacion
// de bajada (reconciliar lo que cambio del lado servidor).
public class DeportistaSyncResponse {
    private String id;
    private String nombre;
    private String documento;
    private Integer edad;
    private String disciplina;
    private String fotoPath;
    private Boolean activo;

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDocumento() {
        return documento;
    }

    public Integer getEdad() {
        return edad;
    }

    public String getDisciplina() {
        return disciplina;
    }

    public String getFotoPath() {
        return fotoPath;
    }

    public Boolean getActivo() {
        return activo;
    }
}

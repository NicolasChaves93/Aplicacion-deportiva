package com.entrenaapp.mobile.data.remote.model;

/**
 * Refleja DeportistaRequest de la API. El id viaja siempre: es el mismo
 * UUID generado en el celular al crear el registro localmente, para que la
 * fila en el servidor tenga exactamente el mismo id que la fila local.
 */
public class DeportistaSyncRequest {
    private final String id;
    private final String nombre;
    private final String documento;
    private final Integer edad;
    private final String disciplina;
    private final String fotoPath;

    public DeportistaSyncRequest(String id, String nombre, String documento, Integer edad,
                                  String disciplina, String fotoPath) {
        this.id = id;
        this.nombre = nombre;
        this.documento = documento;
        this.edad = edad;
        this.disciplina = disciplina;
        this.fotoPath = fotoPath;
    }
}

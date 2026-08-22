package com.entrenaapp.mobile.data.remote.model;

public class AuthResponse {
    private String token;
    private String id;
    private String nombre;
    private String email;
    private String rol;

    public String getToken() {
        return token;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public String getRol() {
        return rol;
    }
}

package com.ammaia_ispc.sangreyamobile.model;

public class AuthUser {

    private int id;
    private String email;
    private String nombre;
    private String apellido;
    private String rol;

    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getDisplayName() {
        return nombre == null ? "" : nombre.trim();
    }

    public String getRol() {
        return rol;
    }
}

package com.ammaia_ispc.sangreyamobile.model;

public class AuthUser {

    private int id;
    private String username;
    private String email;
    private String dni;
    private String nombre;
    private String apellido;
    private String fecha_nacimiento;
    private String fecha_registro;
    private String rol;

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getDni() {
        return dni;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getFechaNacimiento() {
        return fecha_nacimiento;
    }

    public String getFechaRegistro() {
        return fecha_registro;
    }

    public String getDisplayName() {
        return nombre == null ? "" : nombre.trim();
    }

    public String getRol() {
        return rol;
    }
}

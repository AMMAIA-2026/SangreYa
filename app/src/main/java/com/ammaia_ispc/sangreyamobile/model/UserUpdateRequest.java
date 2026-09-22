package com.ammaia_ispc.sangreyamobile.model;

public class UserUpdateRequest {
    private final String username;
    private final String email;
    private final String dni;
    private final String nombre;
    private final String apellido;
    private final String fecha_nacimiento;

    public UserUpdateRequest(
            String username,
            String email,
            String dni,
            String nombre,
            String apellido,
            String fecha_nacimiento) {
        this.username = username;
        this.email = email;
        this.dni = dni;
        this.nombre = nombre;
        this.apellido = apellido;
        this.fecha_nacimiento = fecha_nacimiento;
    }
}

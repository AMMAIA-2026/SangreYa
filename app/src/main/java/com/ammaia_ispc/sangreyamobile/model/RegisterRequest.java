package com.ammaia_ispc.sangreyamobile.model;

public class RegisterRequest {

    private String username;
    private String email;
    private String password;
    private String dni;
    private String nombre;
    private String apellido;
    private String fecha_nacimiento;

    public RegisterRequest(
            String username,
            String email,
            String password,
            String dni,
            String nombre,
            String apellido,
            String fecha_nacimiento) {

        this.username = username;
        this.email = email;
        this.password = password;
        this.dni = dni;
        this.nombre = nombre;
        this.apellido = apellido;
        this.fecha_nacimiento = fecha_nacimiento;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
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

    public String getFecha_nacimiento() {
        return fecha_nacimiento;
    }
}

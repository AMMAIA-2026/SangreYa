package com.ammaia_ispc.sangreyamobile.model;

import com.google.gson.annotations.SerializedName;

public class AuthUser {

    private int id;
    private String username;
    private String email;
    private String dni;
    @SerializedName("nombre")
    private String name;
    @SerializedName("apellido")
    private String lastName;
    @SerializedName("fecha_nacimiento")
    private String birthDate;
    @SerializedName("fecha_registro")
    private String registrationDate;
    @SerializedName("rol")
    private String role;

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

    public String getName() {
        return name;
    }

    public String getLastName() {
        return lastName;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public String getDisplayName() {
        return name == null ? "" : name.trim();
    }

    public String getRole() {
        return role;
    }
}

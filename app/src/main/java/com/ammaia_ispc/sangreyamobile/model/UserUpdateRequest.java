package com.ammaia_ispc.sangreyamobile.model;

import com.google.gson.annotations.SerializedName;

public class UserUpdateRequest {
    private final String username;
    private final String email;
    private final String dni;
    @SerializedName("nombre")
    private final String name;
    @SerializedName("apellido")
    private final String lastName;
    @SerializedName("fecha_nacimiento")
    private final String birthDate;

    public UserUpdateRequest(
            String username,
            String email,
            String dni,
            String name,
            String lastName,
            String birthDate) {
        this.username = username;
        this.email = email;
        this.dni = dni;
        this.name = name;
        this.lastName = lastName;
        this.birthDate = birthDate;
    }
}

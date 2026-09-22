package com.ammaia_ispc.sangreyamobile.model;

public class EnrollmentUser {
    public final int id;
    public final String firstName;
    public final String lastName;
    public final String dni;
    public final String email;

    public EnrollmentUser(
            int id,
            String firstName,
            String lastName,
            String dni,
            String email) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dni = dni;
        this.email = email;
    }
}

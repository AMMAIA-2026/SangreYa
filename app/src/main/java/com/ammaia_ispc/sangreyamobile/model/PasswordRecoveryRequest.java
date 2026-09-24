package com.ammaia_ispc.sangreyamobile.model;

public class PasswordRecoveryRequest {

    private final String email;
    private final String password;

    public PasswordRecoveryRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
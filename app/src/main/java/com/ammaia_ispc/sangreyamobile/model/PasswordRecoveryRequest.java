package com.ammaia_ispc.sangreyamobile.model;

import com.google.gson.annotations.SerializedName;

public class PasswordRecoveryRequest {

    private final String email;
    private final String password;

    @SerializedName("password_confirmation")
    private final String passwordConfirmation;

    public PasswordRecoveryRequest(String email, String password, String passwordConfirmation) {
        this.email = email;
        this.password = password;
        this.passwordConfirmation = passwordConfirmation;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getPasswordConfirmation() {
        return passwordConfirmation;
    }
}
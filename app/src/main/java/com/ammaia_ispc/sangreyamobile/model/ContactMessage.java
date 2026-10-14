package com.ammaia_ispc.sangreyamobile.model;

import com.google.gson.annotations.SerializedName;

public class ContactMessage {

    private int id;

    @SerializedName("nombre_completo")
    private String name;

    @SerializedName("correo_electronico")
    private String email;

    @SerializedName("motivo")
    private String reason;

    @SerializedName("mensaje")
    private String message;

    private boolean tracked;

    @SerializedName("fecha_creacion")
    private String createdAt;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getReason() {
        return reason;
    }

    public String getMessage() {
        return message;
    }

    public boolean isTracked() {
        return tracked;
    }

    public void setTracked(boolean tracked) {
        this.tracked = tracked;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
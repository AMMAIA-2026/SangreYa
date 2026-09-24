package com.ammaia_ispc.sangreyamobile.model;

public class ContactRequest {

    private final String nombre_completo;
    private final String correo_electronico;
    private final String motivo;
    private final String mensaje;

    public ContactRequest(String nombre_completo, String correo_electronico,
                          String motivo, String mensaje) {
        this.nombre_completo = nombre_completo;
        this.correo_electronico = correo_electronico;
        this.motivo = motivo;
        this.mensaje = mensaje;
    }

    public String getNombre_completo() {
        return nombre_completo;
    }

    public String getCorreo_electronico() {
        return correo_electronico;
    }

    public String getMotivo() {
        return motivo;
    }

    public String getMensaje() {
        return mensaje;
    }
}
package com.ammaia_ispc.sangreyamobile.model;

import java.util.regex.Pattern;

public class ContactRequest {
    public static final int MAX_NOMBRE_LENGTH = 20;
    public static final int MAX_MENSAJE_LENGTH = 500;
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9+._%\\-]{1,256}@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}"
                    + "(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+");

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

    public boolean isValid() {
        return present(nombre_completo) && nombre_completo.length() <= MAX_NOMBRE_LENGTH
                && present(correo_electronico) && EMAIL_PATTERN.matcher(correo_electronico).matches()
                && present(motivo)
                && present(mensaje) && mensaje.length() <= MAX_MENSAJE_LENGTH;
    }

    private static boolean present(String value) {
        return value != null && !value.trim().isEmpty();
    }
}

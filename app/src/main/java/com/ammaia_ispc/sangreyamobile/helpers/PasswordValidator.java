package com.ammaia_ispc.sangreyamobile.helpers;

import java.util.regex.Pattern;

public final class PasswordValidator {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])"
                    + "(?=.*[^A-Za-zÁÉÍÓÚáéíóúÑñÜü0-9\\s]).{10,}$");

    private PasswordValidator() {
    }

    public static boolean isValid(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    public static boolean matchesConfirmation(String password, String confirmation) {
        return password != null && password.equals(confirmation);
    }
}
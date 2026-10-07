package com.ammaia_ispc.sangreyamobile.helpers;

import java.util.regex.Pattern;

public final class ProfileValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9+._%\\-]{1,256}@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}"
                    + "(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+");
    private static final Pattern DNI_PATTERN = Pattern.compile("[0-9]{7,8}");

    private ProfileValidator() {
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidDni(String dni) {
        return dni != null && DNI_PATTERN.matcher(dni).matches();
    }
}


package com.ammaia_ispc.sangreyamobile;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.ammaia_ispc.sangreyamobile.helpers.SessionManager;
import com.ammaia_ispc.sangreyamobile.model.AuthUser;
import com.google.gson.Gson;

/**
 * Simula una sesion iniciada para poder abrir pantallas protegidas (admin / usuario)
 */
public final class TestSession {

    private static final String ADMIN_JSON =
            "{\"id\":1,\"email\":\"qa.admin@test.com\",\"nombre\":\"QA\","
                    + "\"apellido\":\"Admin\",\"rol\":\"Administrador\"}";

    private TestSession() {
    }

    public static void signInAsAdmin() {
        Context context = ApplicationProvider.getApplicationContext();
        AuthUser admin = new Gson().fromJson(ADMIN_JSON, AuthUser.class);
        SessionManager.saveSession(context, "test-token", "test-refresh-token", admin);
    }

    public static void clearSession() {
        SessionManager.clearSession(ApplicationProvider.getApplicationContext());
    }
}

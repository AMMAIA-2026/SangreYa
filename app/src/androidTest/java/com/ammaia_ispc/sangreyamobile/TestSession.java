package com.ammaia_ispc.sangreyamobile;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.ammaia_ispc.sangreyamobile.helpers.ApiClient;
import com.ammaia_ispc.sangreyamobile.helpers.SessionManager;
import com.ammaia_ispc.sangreyamobile.model.AuthUser;
import com.google.gson.Gson;

import java.lang.reflect.Field;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Simula una sesion iniciada para poder abrir pantallas protegidas (admin / usuario)
 */
public final class TestSession {

    private static final String UNREACHABLE_BASE_URL = "http://127.0.0.1:9/";

    private static final String ADMIN_JSON =
            "{\"id\":1,\"email\":\"qa.admin@test.com\",\"nombre\":\"QA\","
                    + "\"apellido\":\"Admin\",\"rol\":\"Administrador\"}";

    private static final String USER_JSON =
            "{\"id\":2,\"email\":\"qa.user@test.com\",\"nombre\":\"QA\","
                    + "\"apellido\":\"User\",\"rol\":\"Usuario\"}";

    private TestSession() {
    }

    public static void signInAsAdmin() {
        signIn(ADMIN_JSON);
    }

    public static void signInAsUser() {
        signIn(USER_JSON);
    }

    public static void clearSession() {
        SessionManager.clearSession(ApplicationProvider.getApplicationContext());
    }

    private static void signIn(String userJson) {
        useUnreachableBackend();
        Context context = ApplicationProvider.getApplicationContext();
        AuthUser user = new Gson().fromJson(userJson, AuthUser.class);
        SessionManager.saveSession(context, "test-token", "test-refresh-token", user);
    }

    // ApiClient guarda el Retrofit en un campo estatico privado: se pisa por reflexion
    private static void useUnreachableBackend() {
        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(UNREACHABLE_BASE_URL)
                    .client(new OkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            Field field = ApiClient.class.getDeclaredField("retrofit");
            field.setAccessible(true);
            field.set(null, retrofit);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("No se pudo reemplazar el cliente de red", e);
        }
    }
}

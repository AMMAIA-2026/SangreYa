package com.ammaia_ispc.sangreyamobile.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.ammaia_ispc.sangreyamobile.R;
import com.ammaia_ispc.sangreyamobile.activities.LoginActivity;
import com.ammaia_ispc.sangreyamobile.activities.MainActivity;
import com.ammaia_ispc.sangreyamobile.model.AuthUser;

import java.io.IOException;
import java.security.GeneralSecurityException;

public final class SessionManager {

    private static final String PREFS_NAME = "session_prefs";
    private static final String SECURE_PREFS_NAME = "secure_session_prefs";
    private static final String LEGACY_KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_ROLE = "user_role";

    private static final String ROLE_ADMIN = "Administrador";
    private static volatile String accessToken;

    private SessionManager() {
    }

    public static void saveSession(Context context, String accessToken, String refreshToken, AuthUser user) {
        SessionManager.accessToken = accessToken;
        putRefreshToken(context, refreshToken);
        prefs(context).edit()
                .remove(LEGACY_KEY_ACCESS_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .putInt(KEY_USER_ID, user.getId())
                .putString(KEY_USER_EMAIL, user.getEmail())
                .putString(KEY_USER_NAME, user.getDisplayName())
                .putString(KEY_USER_ROLE, user.getRol())
                .apply();
    }

    public static String getAccessToken(Context context) {
        securePrefs(context);
        return accessToken;
    }

    public static int getUserId(Context context) {
        return prefs(context).getInt(KEY_USER_ID, -1);
    }

    public static String getRefreshToken(Context context) {
        return securePrefs(context).getString(KEY_REFRESH_TOKEN, null);
    }

    public static String getUserName(Context context) {
        return prefs(context).getString(KEY_USER_NAME, "");
    }

    public static void updateUserName(Context context, String userName) {
        prefs(context).edit()
                .putString(KEY_USER_NAME, userName == null ? "" : userName)
                .apply();
    }

    public static void updateTokens(Context context, String accessToken, String refreshToken) {
        SessionManager.accessToken = accessToken;
        putRefreshToken(context, refreshToken);
    }

    private static void putRefreshToken(Context context, String refreshToken) {
        SharedPreferences securePreferences = securePrefs(context);
        SharedPreferences.Editor editor = securePreferences.edit();
        if (refreshToken == null || refreshToken.isEmpty()) {
            editor.remove(KEY_REFRESH_TOKEN);
        } else {
            editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        }
        editor.apply();
    }

    public static String getUserRole(Context context) {
        return prefs(context).getString(KEY_USER_ROLE, null);
    }

    public static boolean isAdmin(Context context) {
        return ROLE_ADMIN.equals(getUserRole(context));
    }

    public static boolean requireAdmin(Activity activity) {
        if (!isAdmin(activity)) {
            Toast.makeText(activity, R.string.error_unauthorized_access, Toast.LENGTH_SHORT).show();
            activity.finish();
            return false;
        }
        return true;
    }

    public static void clearSession(Context context) {
        accessToken = null;
        prefs(context).edit()
                .clear()
                .apply();
        securePrefs(context).edit().clear().apply();
    }

    // Called only when the backend explicitly rejected the refresh (401/400) — a plain
    // network error while refreshing must NOT end up here (see TokenAuthenticator).
    public static void expireSession(Context context) {
        clearSession(context);
        Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(ExtraKeys.EXTRA_SESSION_EXPIRED, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    public static void logout(Activity activity) {
        clearSession(activity);
        Intent intent = new Intent(activity, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private static SharedPreferences securePrefs(Context context) {
        Context applicationContext = context.getApplicationContext();
        try {
            MasterKey masterKey = new MasterKey.Builder(applicationContext)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            SharedPreferences encryptedPreferences = EncryptedSharedPreferences.create(
                    applicationContext,
                    SECURE_PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
            migrateLegacyTokens(applicationContext, encryptedPreferences);
            return encryptedPreferences;
        } catch (GeneralSecurityException | IOException exception) {
            throw new IllegalStateException("No se pudo inicializar el almacenamiento seguro", exception);
        }
    }

    private static void migrateLegacyTokens(
            Context context,
            SharedPreferences encryptedPreferences) {
        SharedPreferences legacyPreferences = prefs(context);
        if (!legacyPreferences.contains(LEGACY_KEY_ACCESS_TOKEN)
                && !legacyPreferences.contains(KEY_REFRESH_TOKEN)) {
            return;
        }

        String legacyRefreshToken = legacyPreferences.getString(KEY_REFRESH_TOKEN, null);
        if (!encryptedPreferences.contains(KEY_REFRESH_TOKEN) && legacyRefreshToken != null) {
            encryptedPreferences.edit()
                    .putString(KEY_REFRESH_TOKEN, legacyRefreshToken)
                    .apply();
        }

        legacyPreferences.edit()
                .remove(LEGACY_KEY_ACCESS_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .apply();
    }
}

package com.ammaia_ispc.sangreyamobile.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.ammaia_ispc.sangreyamobile.model.AuthUser;

public final class SessionManager {

    private static final String PREFS_NAME = "session_prefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_ROLE = "user_role";

    private SessionManager() {
    }

    public static void saveSession(Context context, String accessToken, String refreshToken, AuthUser user) {
        prefs(context).edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .putInt(KEY_USER_ID, user.getId())
                .putString(KEY_USER_EMAIL, user.getEmail())
                .putString(KEY_USER_ROLE, user.getRol())
                .apply();
    }

    public static String getAccessToken(Context context) {
        return prefs(context).getString(KEY_ACCESS_TOKEN, null);
    }

    public static String getRefreshToken(Context context) {
        return prefs(context).getString(KEY_REFRESH_TOKEN, null);
    }

    public static void clearSession(Context context) {
        prefs(context).edit().clear().apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}

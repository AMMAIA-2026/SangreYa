package com.ammaia_ispc.sangreyamobile.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.ammaia_ispc.sangreyamobile.R;
import com.ammaia_ispc.sangreyamobile.activities.LoginActivity;
import com.ammaia_ispc.sangreyamobile.model.AuthUser;

public final class SessionManager {

    private static final String PREFS_NAME = "session_prefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_ROLE = "user_role";

    private static final String ROLE_ADMIN = "Administrador";

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

    public static void updateTokens(Context context, String accessToken, String refreshToken) {
        prefs(context).edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .apply();
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
        prefs(context).edit().clear().apply();
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

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}

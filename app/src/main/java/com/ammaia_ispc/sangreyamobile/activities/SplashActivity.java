package com.ammaia_ispc.sangreyamobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ammaia_ispc.sangreyamobile.R;
import com.ammaia_ispc.sangreyamobile.helpers.ApiClient;
import com.ammaia_ispc.sangreyamobile.helpers.SessionManager;
import com.ammaia_ispc.sangreyamobile.model.RefreshRequest;
import com.ammaia_ispc.sangreyamobile.model.RefreshResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends AppCompatActivity {
    private static final long SPLASH_DELAY_MS = 2000;
    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        updateSessionStatus(getSplashUserName());

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            restoreSession();
        }, SPLASH_DELAY_MS);
    }

    private void restoreSession() {
        String refreshToken;
        try {
            refreshToken = SessionManager.getRefreshToken(this);
        } catch (RuntimeException exception) {
            Log.e(TAG, "No se pudo leer la sesión persistida", exception);
            openGuestHome();
            return;
        }

        if (TextUtils.isEmpty(refreshToken)) {
            openGuestHome();
            return;
        }

        ApiClient.getPlainApiService()
                .refreshToken(new RefreshRequest(refreshToken))
                .enqueue(new Callback<RefreshResponse>() {
                    @Override
                    public void onResponse(
                            Call<RefreshResponse> call,
                            Response<RefreshResponse> response) {
                        RefreshResponse body = response.body();
                        if (response.isSuccessful()
                                && body != null
                                && !TextUtils.isEmpty(body.getAccess())) {
                            SessionManager.updateTokens(
                                    SplashActivity.this,
                                    body.getAccess(),
                                    body.getRefresh());
                            openAuthenticatedHome();
                            return;
                        }

                        if (response.code() == 400 || response.code() == 401) {
                            SessionManager.clearSession(SplashActivity.this);
                        }
                        openGuestHome();
                    }

                    @Override
                    public void onFailure(Call<RefreshResponse> call, Throwable throwable) {
                        Log.w(TAG, "No se pudo restaurar la sesión por un error de red", throwable);
                        openGuestHome();
                    }
                });
    }

    private String getSplashUserName() {
        try {
            if (TextUtils.isEmpty(SessionManager.getRefreshToken(this))) {
                return null;
            }
            return SessionManager.getUserName(this);
        } catch (RuntimeException exception) {
            Log.e(TAG, "No se pudo leer el nombre de la sesión persistida", exception);
            return null;
        }
    }

    private void updateSessionStatus(String userName) {
        TextView status = findViewById(R.id.tvSessionStatus);
        if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(userName.trim())) {
            status.setText(R.string.splash_guest_status);
        } else {
            status.setText(getString(R.string.splash_checking_session, userName.trim()));
        }
    }

    private void openAuthenticatedHome() {
        Class<?> destination = SessionManager.isAdmin(this)
                ? AdminDashboardActivity.class
                : MainActivity.class;
        open(destination);
    }

    private void openGuestHome() {
        updateSessionStatus(null);
        open(MainActivity.class);
    }

    private void open(Class<?> destination) {
        Intent intent = new Intent(this, destination);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

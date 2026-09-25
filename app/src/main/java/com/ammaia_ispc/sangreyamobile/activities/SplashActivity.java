package com.ammaia_ispc.sangreyamobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.ammaia_ispc.sangreyamobile.R;

public class SplashActivity extends AppCompatActivity {
    private static final long SPLASH_DELAY_MS = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // TODO: cuando exista lógica real de sesión (token guardado, JWT, etc.),
        // acá se decide si ir a MainActivity (usuario logueado) o a LoginActivity
        // (sin sesión). Por ahora, según CA-50, siempre va a Inicio sin pedir login;
        // MainActivity ya maneja el estado "invitado" vs "usuario logueado".
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_DELAY_MS);
    }
}
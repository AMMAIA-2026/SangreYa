package com.ammaia_ispc.sangreyamobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ammaia_ispc.sangreyamobile.R;
import com.ammaia_ispc.sangreyamobile.helpers.NavigationHelper;

public class ForgotPasswordActivity extends AppCompatActivity {

    // TODO opcional: evaluar mover EMAIL_KEY a ExtraKeys como EXTRA_EMAIL.
    // Parte exacta a mover: esta constante y sus dos usos del flujo de recuperación.
    // Pasos si se decide centralizarla:
    // 1. Agregar en ExtraKeys: public static final String EXTRA_EMAIL = "email";
    // 2. Reemplazar putExtra(EMAIL_KEY, email) por putExtra(ExtraKeys.EXTRA_EMAIL, email).
    // 3. Reemplazar getStringExtra(ForgotPasswordActivity.EMAIL_KEY) por
    //    getStringExtra(ExtraKeys.EXTRA_EMAIL) en ResetPasswordActivity.
    // 4. Eliminar EMAIL_KEY de esta Activity.
    // Razones para moverla: centraliza los contratos entre Activities, evita duplicar
    // nombres y deja todas las keys de navegación en un único lugar.
    // Razones para mantenerla aquí: sólo la comparten este flujo y su destino, por lo
    // que su ubicación local expresa suficientemente bien el alcance.
    // Elija y borre este TODO, jaja.
    public static final String EMAIL_KEY = "email";

    private EditText emailInput;
    private Button continueButton;
    private ImageButton backButton;
    private TextView backToLoginText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        NavigationHelper.configureBackButton(this, R.id.backButton);

        emailInput = findViewById(R.id.emailInput);
        continueButton = findViewById(R.id.continueButton);
        backButton = findViewById(R.id.backButton);
        backToLoginText = findViewById(R.id.backToLoginText);

        backToLoginText.setOnClickListener(v -> finish());
        continueButton.setOnClickListener(v -> continueToResetPassword());
    }

    private void continueToResetPassword() {
        String email = emailInput.getText().toString().trim();

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError(getString(R.string.error_invalid_email));
            emailInput.requestFocus();
            return;
        }

        Intent intent = new Intent(
                ForgotPasswordActivity.this,
                ResetPasswordActivity.class
        );

        intent.putExtra(EMAIL_KEY, email);
        startActivity(intent);
    }
}

package com.ammaia_ispc.sangreyamobile;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.regex.Pattern;

public class ResetPasswordActivity extends AppCompatActivity {

    // Al menos 8 caracteres, al menos 1 mayúscula, al menos 1 número
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Z])(?=.*[0-9]).{8,}$");

    private EditText newPasswordInput;
    private EditText confirmPasswordInput;
    private Button saveButton;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        newPasswordInput = findViewById(R.id.newPasswordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        saveButton = findViewById(R.id.saveButton);
        backButton = findViewById(R.id.backButton);

        // Viene desde ForgotPasswordActivity. No se usa todavía, pero queda
        // listo para cuando se conecte la llamada real a la API.
        String email = getIntent().getStringExtra(ForgotPasswordActivity.EMAIL_KEY);

        backButton.setOnClickListener(v -> finish());
        saveButton.setOnClickListener(v -> attemptSave());
    }

    private void attemptSave() {
        String newPassword = newPasswordInput.getText().toString();
        String confirmPassword = confirmPasswordInput.getText().toString();

        // CA-13: contraseña débil -> se informa el error y no se actualiza
        if (TextUtils.isEmpty(newPassword) || !PASSWORD_PATTERN.matcher(newPassword).matches()) {
            newPasswordInput.setError(getString(R.string.error_weak_password));
            newPasswordInput.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            confirmPasswordInput.setError(getString(R.string.error_password_mismatch));
            confirmPasswordInput.requestFocus();
            return;
        }

        // TODO: por ahora no hay llamada real a la API. Cuando se conecte
        // el backend, acá va la llamada Retrofit (ej. POST con la nueva
        // contraseña) y solo si la respuesta es OK se navega al Login.
        Toast.makeText(this, "Contraseña guardada (simulado)", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}

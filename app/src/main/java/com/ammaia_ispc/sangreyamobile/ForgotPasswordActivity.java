package com.ammaia_ispc.sangreyamobile;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordActivity extends AppCompatActivity {

    // TODO: si ya existe una constante equivalente en ExtraKeys.java, usar esa
    // en vez de esta y borrar esta línea (ej. ExtraKeys.EMAIL).
    public static final String EMAIL_KEY = "email";

    private EditText emailInput;
    private EditText dniInput;
    private Button verifyButton;
    private ImageView backButton;
    private TextView backToLoginText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        emailInput = findViewById(R.id.emailInput);
        dniInput = findViewById(R.id.dniInput);
        verifyButton = findViewById(R.id.verifyButton);
        backButton = findViewById(R.id.backButton);
        backToLoginText = findViewById(R.id.backToLoginText);

        backButton.setOnClickListener(v -> finish());
        backToLoginText.setOnClickListener(v -> finish());
        verifyButton.setOnClickListener(v -> attemptVerify());
    }

    private void attemptVerify() {
        String email = emailInput.getText().toString().trim();
        String dni = dniInput.getText().toString().trim();

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError(getString(R.string.error_invalid_email));
            emailInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(dni) || dni.length() < 7 || dni.length() > 8) {
            dniInput.setError(getString(R.string.error_invalid_dni));
            dniInput.requestFocus();
            return;
        }

        // TODO: por ahora no hay llamada real a la API. Cuando se conecte
        // el backend, acá va la llamada Retrofit (ej. POST a un endpoint
        // de recuperación de contraseña) y solo si la respuesta es OK se
        // navega a ResetPasswordActivity.
        Toast.makeText(this, "Identidad verificada (simulado)", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class);
        intent.putExtra(EMAIL_KEY, email);
        startActivity(intent);
    }
}
package com.ammaia_ispc.sangreyamobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;

import com.ammaia_ispc.sangreyamobile.R;
import com.ammaia_ispc.sangreyamobile.helpers.AdminDashboardHelper;
import com.ammaia_ispc.sangreyamobile.helpers.ExtraKeys;
import com.ammaia_ispc.sangreyamobile.helpers.NavigationHelper;
import android.util.Patterns;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private Button btnIngresar;

    private TextView tvRegistrate;
    private TextView tvOlvidasteContrasena;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        NavigationHelper.configureBackButton(this, R.id.btnBack);

        // Enlazamos exactamente con los IDs de tu layout actual
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnIngresar = findViewById(R.id.btnIngresar);

        tvRegistrate = findViewById(R.id.tvRegistrate);
        tvOlvidasteContrasena = findViewById(R.id.tvOlvidasteContrasena);

        tvRegistrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        tvOlvidasteContrasena.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

        btnIngresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validarEIngresar();
            }
        });
    }

    private void validarEIngresar() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(LoginActivity.this, "Por favor, completá todos los campos", Toast.LENGTH_SHORT).show();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(
                    LoginActivity.this,
                    "Ingresá un email válido",
                    Toast.LENGTH_SHORT
            ).show();
        }

        else if (AdminDashboardHelper.isMockAdminEmail(email)
                && !AdminDashboardHelper.isMockAdmin(email, password)) {
            Toast.makeText(
                    LoginActivity.this,
                    R.string.invalid_admin_credentials,
                    Toast.LENGTH_SHORT).show();
        } else {
            boolean admin = AdminDashboardHelper.isMockAdmin(email, password);
            Intent intent = new Intent(
                    LoginActivity.this,
                    admin ? AdminDashboardActivity.class : MainActivity.class);
            intent.putExtra(ExtraKeys.EXTRA_STANDARD_USER, !admin);
            intent.putExtra(ExtraKeys.EXTRA_USER, email);
            if (admin) {
                intent.putExtra(ExtraKeys.EXTRA_USER_ROLE, ExtraKeys.ROLE_ADMIN);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

    }
}

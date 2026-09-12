package com.ammaia_ispc.sangreyamobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.ammaia_ispc.sangreyamobile.helpers.NavigationHelper;
import androidx.appcompat.app.AppCompatActivity;
import com.ammaia_ispc.sangreyamobile.R;
import android.text.TextUtils;
import com.ammaia_ispc.sangreyamobile.data.MockUserRepository;
import com.ammaia_ispc.sangreyamobile.model.User;

public class RegisterActivity extends AppCompatActivity {

    private Button btnCreateAccount;
    private TextView tvIniciarSesion;
    private Spinner etBloodGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        NavigationHelper.configureBackButton(this, R.id.btnBack);

        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        tvIniciarSesion = findViewById(R.id.tvIniciarSesion);
        etBloodGroup = findViewById(R.id.etBloodGroup);

        String[] bloodGroups = {
                "Select",
                "A+",
                "A-",
                "B+",
                "B-",
                "AB+",
                "AB-",
                "O+",
                "O-"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                bloodGroups
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        etBloodGroup.setAdapter(adapter);

        btnCreateAccount.setOnClickListener(v -> validarRegistro());

        tvIniciarSesion.setOnClickListener(v -> {
            Intent intent = new Intent(
                    RegisterActivity.this,
                    LoginActivity.class
            );
            startActivity(intent);
            finish();
        });
    }

    private void validarRegistro() {

        String name = ((android.widget.EditText) findViewById(R.id.etName))
                .getText().toString().trim();

        String email = ((android.widget.EditText) findViewById(R.id.etRegisterEmail))
                .getText().toString().trim();

        String password = ((android.widget.EditText) findViewById(R.id.etRegisterPassword))
                .getText().toString();

        String bloodGroup = etBloodGroup.getSelectedItem().toString();

        // Campos obligatorios
        if (TextUtils.isEmpty(name) ||
                TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(password) ||
                bloodGroup.equals("Select")) {

            Toast.makeText(this,
                    "Completá todos los campos",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Email válido
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this,
                    "Ingresá un email válido",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Email único
        for (User user : MockUserRepository.getUsers()) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                Toast.makeText(this,
                        "El email ya está registrado",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Contraseña
        if (password.length() < 8 ||
                !password.matches(".*[A-Z].*") ||
                !password.matches(".*[a-z].*") ||
                !password.matches(".*[0-9].*") ||
                !password.matches(".*[^a-zA-Z0-9].*")) {

            Toast.makeText(this,
                    "La contraseña debe tener 8 caracteres, mayúscula, minúscula, número y símbolo",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this,
                "Cuenta creada correctamente",
                Toast.LENGTH_SHORT).show();
    }

}







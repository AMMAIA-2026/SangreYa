package com.ammaia_ispc.sangreyamobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.ammaia_ispc.sangreyamobile.helpers.NavigationHelper;
import androidx.appcompat.app.AppCompatActivity;
import com.ammaia_ispc.sangreyamobile.R;
import android.text.TextUtils;
import android.widget.EditText;

import com.ammaia_ispc.sangreyamobile.data.RegisterApiRepository;
import com.ammaia_ispc.sangreyamobile.model.RegisterRequest;
public class RegisterActivity extends AppCompatActivity {

private Button btnCreateAccount;
private TextView tvIniciarSesion;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_register);
    NavigationHelper.configureBackButton(this, R.id.btnBack);

    btnCreateAccount = findViewById(R.id.btnCreateAccount);
    tvIniciarSesion = findViewById(R.id.tvIniciarSesion);

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

    String apellido = ((EditText) findViewById(R.id.etApellido))
            .getText().toString().trim();

    String username = ((EditText) findViewById(R.id.etUsername))
            .getText().toString().trim();

    String fechaNacimiento = ((EditText) findViewById(R.id.etFechaNacimiento))
            .getText().toString().trim();

    String[] partesFecha = fechaNacimiento.split("/");

    if (partesFecha.length == 3) {
        fechaNacimiento = partesFecha[2] + "-"
                + partesFecha[1] + "-"
                + partesFecha[0];
    }

    String dni = ((android.widget.EditText) findViewById(R.id.etDni))
            .getText().toString().trim();

    String email = ((android.widget.EditText) findViewById(R.id.etRegisterEmail))
            .getText().toString().trim();

    String password = ((android.widget.EditText) findViewById(R.id.etRegisterPassword))
            .getText().toString();

    String confirmPassword = ((android.widget.EditText) findViewById(R.id.etConfirmPassword))
            .getText().toString();


    // Campos obligatorios
    if (TextUtils.isEmpty(name) ||
            TextUtils.isEmpty(dni) ||
            TextUtils.isEmpty(email) ||
            TextUtils.isEmpty(password) ||
            TextUtils.isEmpty(confirmPassword) ||
            TextUtils.isEmpty(apellido) ||
            TextUtils.isEmpty(username) ||
            TextUtils.isEmpty(fechaNacimiento)) {

        //TODO. Este toast se puede pasar a helper, Toast.makeText
        Toast.makeText(this,
                "Completá todos los campos",
                Toast.LENGTH_SHORT).show();
        return;
    }


    if (dni.length() < 7 || dni.length() > 8) {
        //TODO. Este toast se puede pasar a helper, Toast.makeText
        Toast.makeText(this,
                "Ingresá un DNI válido",
                Toast.LENGTH_SHORT).show();
        return;
    }

    // Email válido
    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        //TODO. Este toast se puede pasar a helper, Toast.makeText
        Toast.makeText(this,
                "Ingresá un email válido",
                Toast.LENGTH_SHORT).show();
        return;
    }


    // Contraseña
    if (password.length() < 10 ||
            !password.matches(".*[A-Z].*") ||
            !password.matches(".*[a-z].*") ||
            !password.matches(".*[0-9].*") ||
            !password.matches(".*[^a-zA-Z0-9].*")) {

        //TODO. Este toast se puede pasar a helper, Toast.makeText
        Toast.makeText(this,
                "La contraseña debe tener 10 caracteres, mayúscula, minúscula, número y símbolo",
                Toast.LENGTH_SHORT).show();
        return;
    }
    if (!password.equals(confirmPassword)) {
        //TODO. Este toast se puede pasar a helper, Toast.makeText
        Toast.makeText(this,
                "Las contraseñas no coinciden",
                Toast.LENGTH_SHORT).show();
        return;
    }

    RegisterRequest request = new RegisterRequest(
            username,
            email,
            password,
            dni,
            name,
            apellido,
            fechaNacimiento
    );

    RegisterApiRepository.register(
            this,
            request,
            new RegisterApiRepository.RegisterCallback() {

                @Override
                public void onSuccess() {
                    Toast.makeText(
                            RegisterActivity.this,
                            "Cuenta creada correctamente",
                            Toast.LENGTH_SHORT
                    ).show();
                }

                @Override
                public void onValidationError(
                        String field,
                        String message) {

                    Toast.makeText(
                            RegisterActivity.this,
                            message,
                            Toast.LENGTH_SHORT
                    ).show();
                }

                @Override
                public void onError(int messageRes) {
                    Toast.makeText(
                            RegisterActivity.this,
                            messageRes,
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
    );
}

}







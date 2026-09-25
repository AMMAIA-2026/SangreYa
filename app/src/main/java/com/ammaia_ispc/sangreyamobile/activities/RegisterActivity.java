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
            TextUtils.isEmpty(confirmPassword))
             {

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

    //TODO. Este toast se puede pasar a helper, Toast.makeText
    Toast.makeText(this,
            "Cuenta creada correctamente",
            Toast.LENGTH_SHORT).show();
}

}







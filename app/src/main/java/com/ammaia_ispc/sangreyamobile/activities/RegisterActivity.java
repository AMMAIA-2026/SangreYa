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

import android.text.method.PasswordTransformationMethod;
import android.text.method.HideReturnsTransformationMethod;
import android.widget.ImageButton;
import android.app.DatePickerDialog;
import java.util.Calendar;
public class RegisterActivity extends AppCompatActivity {

private Button btnCreateAccount;
private TextView tvIniciarSesion;
private EditText etName;
private EditText etApellido;
private EditText etUsername;
private EditText etDni;
private EditText etRegisterEmail;
private EditText etFechaNacimiento;
private EditText etRegisterPassword;
private EditText etConfirmPassword;
private ImageButton btnTogglePassword;
private ImageButton btnToggleConfirmPassword;
private boolean passwordVisible = false;
private boolean confirmPasswordVisible = false;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_register);
    NavigationHelper.configureBackButton(this, R.id.btnBack);

    btnCreateAccount = findViewById(R.id.btnCreateAccount);
    tvIniciarSesion = findViewById(R.id.tvIniciarSesion);

    etName = findViewById(R.id.etName);
    etApellido = findViewById(R.id.etApellido);
    etUsername = findViewById(R.id.etUsername);
    etDni = findViewById(R.id.etDni);
    etRegisterEmail = findViewById(R.id.etRegisterEmail);
    etFechaNacimiento = findViewById(R.id.etFechaNacimiento);
    etRegisterPassword = findViewById(R.id.etRegisterPassword);
    etConfirmPassword = findViewById(R.id.etConfirmPassword);

    btnTogglePassword = findViewById(R.id.btnTogglePassword);
    btnToggleConfirmPassword = findViewById(R.id.btnToggleConfirmPassword);

    etFechaNacimiento.setOnClickListener(v -> {
        Calendar calendario = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                RegisterActivity.this,
                (view, year, month, dayOfMonth) -> {
                    String fecha = String.format(
                            "%02d/%02d/%04d",
                            dayOfMonth,
                            month + 1,
                            year
                    );

                    etFechaNacimiento.setText(fecha);
                },
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    });

    btnTogglePassword.setOnClickListener(v -> {
        passwordVisible = !passwordVisible;

        if (passwordVisible) {
            etRegisterPassword.setTransformationMethod(
                    HideReturnsTransformationMethod.getInstance());
        } else {
            etRegisterPassword.setTransformationMethod(
                    PasswordTransformationMethod.getInstance());
        }

        etRegisterPassword.setSelection(etRegisterPassword.length());
    });

    btnToggleConfirmPassword.setOnClickListener(v -> {
        confirmPasswordVisible = !confirmPasswordVisible;

        if (confirmPasswordVisible) {
            etConfirmPassword.setTransformationMethod(
                    HideReturnsTransformationMethod.getInstance());
        } else {
            etConfirmPassword.setTransformationMethod(
                    PasswordTransformationMethod.getInstance());
        }

        etConfirmPassword.setSelection(etConfirmPassword.length());
    });

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

    etName.setError(null);
    etApellido.setError(null);
    etUsername.setError(null);
    etDni.setError(null);
    etRegisterEmail.setError(null);
    etFechaNacimiento.setError(null);
    etRegisterPassword.setError(null);
    etConfirmPassword.setError(null);

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
                public void onValidationError(String field, String message) {
                    switch (field) {
                        case "email":
                            etRegisterEmail.setError(message);
                            etRegisterEmail.requestFocus();
                            break;

                        case "dni":
                            etDni.setError(message);
                            etDni.requestFocus();
                            break;

                        case "username":
                            etUsername.setError(message);
                            etUsername.requestFocus();
                            break;

                        case "nombre":
                            etName.setError(message);
                            etName.requestFocus();
                            break;

                        case "apellido":
                            etApellido.setError(message);
                            etApellido.requestFocus();
                            break;

                        case "fecha_nacimiento":
                            etFechaNacimiento.setError(message);
                            etFechaNacimiento.requestFocus();
                            break;

                        default:
                            Toast.makeText(
                                    RegisterActivity.this,
                                    message,
                                    Toast.LENGTH_SHORT
                            ).show();
                            break;
                    }
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







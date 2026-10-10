package com.ammaia_ispc.sangreyamobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ammaia_ispc.sangreyamobile.R;
import com.ammaia_ispc.sangreyamobile.data.RegisterApiRepository;
import com.ammaia_ispc.sangreyamobile.helpers.DateHelper;
import com.ammaia_ispc.sangreyamobile.helpers.NavigationHelper;
import com.ammaia_ispc.sangreyamobile.helpers.PasswordValidator;
import com.ammaia_ispc.sangreyamobile.helpers.UiHelper;
import com.ammaia_ispc.sangreyamobile.model.RegisterRequest;

import android.app.AlertDialog;

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
    private CheckBox cbAceptarTerminos;
    private ImageButton btnTogglePassword;
    private ImageButton btnToggleConfirmPassword;

    private static final String TEXTO_TERMINOS =
            "Términos y Condiciones y Política de Privacidad de SangreYa\n\n" +
                    "1. Datos que recolectamos: al registrarte, almacenamos tu nombre, apellido, " +
                    "DNI, email, fecha de nacimiento y grupo sanguíneo (si lo indicás), con el único " +
                    "fin de gestionar tu participación en campañas de donación de sangre.\n\n" +
                    "2. Uso de tus datos: tus datos no se comparten con terceros ajenos al proyecto " +
                    "ni se utilizan con fines comerciales. Solo se usan para identificarte y " +
                    "gestionar tus inscripciones a campañas.\n\n" +
                    "3. Tus derechos (Ley 25.326 de Protección de Datos Personales): podés acceder, " +
                    "rectificar o solicitar la eliminación de tus datos personales en cualquier " +
                    "momento desde la opción correspondiente en tu perfil.\n\n" +
                    "4. Seguridad: tu contraseña se almacena de forma cifrada y nunca es visible " +
                    "para el equipo de SangreYa ni se comparte por ningún medio.\n\n" +
                    "Al aceptar, confirmás que leíste y entendés estos términos.";

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
        cbAceptarTerminos = findViewById(R.id.cbAceptarTerminos);

        btnTogglePassword = findViewById(R.id.btnTogglePassword);
        btnToggleConfirmPassword = findViewById(R.id.btnToggleConfirmPassword);

        UiHelper.configurePasswordToggle(etRegisterPassword, btnTogglePassword);
        UiHelper.configurePasswordToggle(etConfirmPassword, btnToggleConfirmPassword,
                R.string.show_confirm_password, R.string.hide_confirm_password);

        cbAceptarTerminos.setOnClickListener(v -> mostrarTerminos());

        // TODO(date-input): habilitar escritura manual en etFechaNacimiento.
        // 1. En activity_register.xml, quitar android:focusable="false" del EditText.
        // 2. Agregar android:drawableEnd="@drawable/ic_calendar" al mismo EditText.
        // 3. Reemplazar este setOnClickListener por:
        //    DateHelper.configureDateInput(this, etFechaNacimiento);

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

    private void mostrarTerminos() {
        new AlertDialog.Builder(this)
                .setTitle("Términos y Condiciones")
                .setMessage(TEXTO_TERMINOS)
                .setPositiveButton("Cerrar", null)
                .show();
    }

    private void validarRegistro() {

        EditText[] fields = {etName, etApellido, etUsername, etDni, etRegisterEmail,
                etFechaNacimiento, etRegisterPassword, etConfirmPassword};
        for (EditText field : fields) {
            field.setError(null);
            String value = field.getText().toString();
            boolean passwordField = field == etRegisterPassword || field == etConfirmPassword;
            if (TextUtils.isEmpty(passwordField ? value : value.trim())) {
                field.setError(getString(R.string.error_required_field));
            }
        }
        cbAceptarTerminos.setError(null);

        String name = ((EditText) findViewById(R.id.etName))
                .getText().toString().trim();

        String apellido = ((EditText) findViewById(R.id.etApellido))
                .getText().toString().trim();

        String username = ((EditText) findViewById(R.id.etUsername))
                .getText().toString().trim();

        String fechaNacimientoDisplay = ((EditText) findViewById(R.id.etFechaNacimiento))
                .getText().toString().trim();
        String fechaNacimiento = DateHelper.toIsoDate(fechaNacimientoDisplay);

        String dni = ((EditText) findViewById(R.id.etDni))
                .getText().toString().trim();

        String email = ((EditText) findViewById(R.id.etRegisterEmail))
                .getText().toString().trim();

        String password = ((EditText) findViewById(R.id.etRegisterPassword))
                .getText().toString();

        String confirmPassword = ((EditText) findViewById(R.id.etConfirmPassword))
                .getText().toString();

        if (!TextUtils.isEmpty(fechaNacimientoDisplay) && TextUtils.isEmpty(fechaNacimiento)) {
            etFechaNacimiento.setError(getString(R.string.profile_birth_date_invalid));
        }

        // DNI válido: exactamente 7 u 8 dígitos
        if (!TextUtils.isEmpty(dni) && !dni.matches("\\d{7,8}")) {
            etDni.setError(getString(R.string.error_invalid_dni));
        }

        // Email válido
        if (!TextUtils.isEmpty(email)
                && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etRegisterEmail.setError(getString(R.string.error_invalid_email));
        }

        // Contraseña
        if (!TextUtils.isEmpty(password) && !PasswordValidator.isValid(password)) {
            etRegisterPassword.setError(getString(R.string.error_weak_password));
        }

        // Confirmación de contraseña
        if (!TextUtils.isEmpty(confirmPassword)
                && !PasswordValidator.matchesConfirmation(password, confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.error_password_mismatch));
        }

        if (!cbAceptarTerminos.isChecked()) {
            cbAceptarTerminos.setError(getString(R.string.error_terms_required));
        }

        if (UiHelper.focusFirstError(fields)) {
            return;
        }
        if (UiHelper.focusFirstError(cbAceptarTerminos)) {
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
                        clearForm();
                        UiHelper.showToast(
                                RegisterActivity.this,
                                "Cuenta creada correctamente",
                                Toast.LENGTH_SHORT
                        );
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

                            case "password":
                                etRegisterPassword.setError(message);
                                etRegisterPassword.requestFocus();
                                break;

                            default:
                                UiHelper.showToast(
                                        RegisterActivity.this,
                                        message,
                                        Toast.LENGTH_SHORT
                                );
                                break;
                        }
                    }

                    @Override
                    public void onError(int messageRes) {
                        UiHelper.showToast(
                                RegisterActivity.this,
                                getString(messageRes),
                                Toast.LENGTH_SHORT
                        );
                    }
                }
        );
    }

    private void clearForm() {
        etName.setText("");
        etApellido.setText("");
        etUsername.setText("");
        etDni.setText("");
        etRegisterEmail.setText("");
        etFechaNacimiento.setText("");
        etRegisterPassword.setText("");
        etConfirmPassword.setText("");
    }
}

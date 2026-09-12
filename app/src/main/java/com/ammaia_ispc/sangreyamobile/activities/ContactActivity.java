package com.ammaia_ispc.sangreyamobile.activities;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ammaia_ispc.sangreyamobile.R;
import com.ammaia_ispc.sangreyamobile.helpers.NavigationHelper;


public class ContactActivity extends AppCompatActivity {

    private static final int MAX_NOMBRE_LENGTH = 20;
    private static final int MAX_MENSAJE_LENGTH = 500;

    private EditText etNombre, etEmail, etMensaje;
    private Spinner spinnerMotivo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        NavigationHelper.configureBackButton(this, R.id.btnBack);

        etNombre = findViewById(R.id.etNombre);
        etEmail = findViewById(R.id.etEmail);
        etMensaje = findViewById(R.id.etMensaje);
        spinnerMotivo = findViewById(R.id.spinnerMotivo);
        Button btnEnviar = findViewById(R.id.btnEnviar);

        etNombre.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_NOMBRE_LENGTH)});
        etMensaje.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_MENSAJE_LENGTH)});

        String[] motivos = {"Consulta general", "Problema técnico", "Sugerencia", "Otro"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, motivos);
        spinnerMotivo.setAdapter(adapter);

        btnEnviar.setOnClickListener(v -> enviarMensaje());
    }

    private void enviarMensaje() {
        String nombre = etNombre.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String mensaje = etMensaje.getText().toString().trim();

        boolean valido = true;

        if (TextUtils.isEmpty(nombre)) {
            etNombre.setError(getString(R.string.error_required_fields));
            valido = false;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError(getString(R.string.error_required_fields));
            valido = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.error_invalid_email));
            valido = false;
        }

        if (TextUtils.isEmpty(mensaje)) {
            etMensaje.setError(getString(R.string.error_required_fields));
            valido = false;
        }

        if (!valido) {
            return;
        }

        // TODO Sprint 2: POST real a /contacto/ (CA-31: se registra con tracked=false)
        Toast.makeText(this, getString(R.string.contact_sent_message), Toast.LENGTH_SHORT).show();
        finish();
    }
}



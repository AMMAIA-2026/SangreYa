package com.ammaia_ispc.sangreyamobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ammaia_ispc.sangreyamobile.R;

public class RegisterActivity extends AppCompatActivity {

    private Button btnCreateAccount;
    private TextView tvIniciarSesion;
    private Spinner etBloodGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

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

        btnCreateAccount.setOnClickListener(v -> {
            Toast.makeText(
                    RegisterActivity.this,
                    "Account created successfully",
                    Toast.LENGTH_SHORT
            ).show();
        });

        tvIniciarSesion.setOnClickListener(v -> {
            Intent intent = new Intent(
                    RegisterActivity.this,
                    LoginActivity.class
            );
            startActivity(intent);
            finish();
        });
    }
}







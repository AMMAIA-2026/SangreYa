package com.ammaia_ispc.sangreyamobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ammaia_ispc.sangreyamobile.R;
import com.ammaia_ispc.sangreyamobile.helpers.NavigationHelper;

public class ForgotPasswordActivity extends AppCompatActivity {

    public static final String EMAIL_KEY = "email";

    private EditText emailInput;
    private Button continueButton;
    private ImageButton backButton;
    private TextView backToLoginText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        NavigationHelper.configureBackButton(this, R.id.backButton);

        emailInput = findViewById(R.id.emailInput);
        continueButton = findViewById(R.id.continueButton);
        backButton = findViewById(R.id.backButton);
        backToLoginText = findViewById(R.id.backToLoginText);

        backToLoginText.setOnClickListener(v -> finish());
        continueButton.setOnClickListener(v -> continueToResetPassword());
    }

    private void continueToResetPassword() {
        String email = emailInput.getText().toString().trim();

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError(getString(R.string.error_invalid_email));
            emailInput.requestFocus();
            return;
        }

        Intent intent = new Intent(
                ForgotPasswordActivity.this,
                ResetPasswordActivity.class
        );

        intent.putExtra(EMAIL_KEY, email);
        startActivity(intent);
    }
}
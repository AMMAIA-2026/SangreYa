package com.ammaia_ispc.sangreyamobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ammaia_ispc.sangreyamobile.R;
import com.ammaia_ispc.sangreyamobile.helpers.ApiClient;
import com.ammaia_ispc.sangreyamobile.helpers.ExtraKeys;
import com.ammaia_ispc.sangreyamobile.helpers.PasswordValidator;
import com.ammaia_ispc.sangreyamobile.helpers.UiHelper;
import com.ammaia_ispc.sangreyamobile.model.PasswordRecoveryRequest;
import com.ammaia_ispc.sangreyamobile.model.PasswordRecoveryResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText newPasswordInput;
    private EditText confirmPasswordInput;
    private Button saveButton;
    private ImageView backButton;

    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        newPasswordInput = findViewById(R.id.newPasswordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        saveButton = findViewById(R.id.saveButton);
        backButton = findViewById(R.id.backButton);

        email = getIntent().getStringExtra(ExtraKeys.EXTRA_EMAIL);

        backButton.setOnClickListener(v -> finish());
        saveButton.setOnClickListener(v -> attemptSave());
    }

    private void attemptSave() {
        String newPassword = newPasswordInput.getText().toString();
        String confirmPassword = confirmPasswordInput.getText().toString();

        if (!PasswordValidator.isValid(newPassword)) {

            newPasswordInput.setError(getString(R.string.error_weak_password));
            newPasswordInput.requestFocus();
            return;
        }

        if (!PasswordValidator.matchesConfirmation(newPassword, confirmPassword)) {
            confirmPasswordInput.setError(getString(R.string.error_password_mismatch));
            confirmPasswordInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            UiHelper.showToast(
                    this,
                    "No se pudo recuperar el email.",
                    Toast.LENGTH_SHORT
            );
            finish();
            return;
        }

        saveButton.setEnabled(false);

        PasswordRecoveryRequest request =
                new PasswordRecoveryRequest(email, newPassword, confirmPassword);

        ApiClient.getPlainApiService()
                .recoverPassword(request)
                .enqueue(new Callback<PasswordRecoveryResponse>() {

                    @Override
                    public void onResponse(
                            Call<PasswordRecoveryResponse> call,
                            Response<PasswordRecoveryResponse> response) {

                        saveButton.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null) {

                            UiHelper.showToast(
                                    ResetPasswordActivity.this,
                                    response.body().getMessage(),
                                    Toast.LENGTH_LONG
                            );

                            Intent intent = new Intent(
                                    ResetPasswordActivity.this,
                                    LoginActivity.class
                            );

                            intent.setFlags(
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                                            | Intent.FLAG_ACTIVITY_NEW_TASK
                            );

                            startActivity(intent);
                            finish();

                        } else {
                            String errorMessage = "No se pudo actualizar la contraseña.";
                            if (response.errorBody() != null) {
                                try {
                                    String body = response.errorBody().string();
                                    if (!TextUtils.isEmpty(body)) {
                                        errorMessage = body;
                                    }
                                } catch (Exception ignored) {
                                    // Si no se puede leer el cuerpo del error, se mantiene
                                    // el mensaje genérico.
                                }
                            }
                            UiHelper.showToast(
                                    ResetPasswordActivity.this,
                                    errorMessage,
                                    Toast.LENGTH_LONG
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<PasswordRecoveryResponse> call,
                            Throwable t) {

                        saveButton.setEnabled(true);

                        UiHelper.showToast(
                                ResetPasswordActivity.this,
                                "No se pudo conectar con el servidor.",
                                Toast.LENGTH_LONG
                        );
                    }
                });
    }
}
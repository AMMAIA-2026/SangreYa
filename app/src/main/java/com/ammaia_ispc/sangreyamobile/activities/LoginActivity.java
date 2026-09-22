package com.ammaia_ispc.sangreyamobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;

import com.ammaia_ispc.sangreyamobile.R;
import com.ammaia_ispc.sangreyamobile.helpers.ApiClient;
import com.ammaia_ispc.sangreyamobile.helpers.ExtraKeys;
import com.ammaia_ispc.sangreyamobile.helpers.NavigationHelper;
import com.ammaia_ispc.sangreyamobile.helpers.SessionManager;
import com.ammaia_ispc.sangreyamobile.model.LoginRequest;
import com.ammaia_ispc.sangreyamobile.model.LoginResponse;
import com.ammaia_ispc.sangreyamobile.model.AuthUser;
import android.util.Log;
import android.util.Patterns;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

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

        if (getIntent().getBooleanExtra(ExtraKeys.EXTRA_SESSION_EXPIRED, false)) {
            showMessage(getString(R.string.session_expired_message));
        }

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
            showMessage(getString(R.string.error_required_fields));
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showMessage(getString(R.string.error_invalid_email));
        } else {
            performLogin(email, password);
        }
    }

    private void performLogin(String email, String password) {
        setLoading(true);
        ApiClient.getApiService(this).login(new LoginRequest(email, password)).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    handleLoginSuccess(response.body());
                } else {
                    handleLoginError(response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                setLoading(false);
                showMessage(getString(R.string.error_connection));
            }
        });
    }

    private void handleLoginSuccess(LoginResponse body) {
        SessionManager.saveSession(this, body.getAccess(), body.getRefresh(), body.getUser());
        setLoading(true);
        ApiClient.getApiService(this)
                .getUserProfile(body.getUser().getId())
                .enqueue(new Callback<AuthUser>() {
                    @Override
                    public void onResponse(Call<AuthUser> call, Response<AuthUser> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            SessionManager.updateUserName(
                                    LoginActivity.this,
                                    response.body().getDisplayName());
                        }
                        continueLogin(body);
                    }

                    @Override
                    public void onFailure(Call<AuthUser> call, Throwable t) {
                        continueLogin(body);
                    }
                });
    }

    private void continueLogin(LoginResponse body) {
        setLoading(false);
        boolean admin = SessionManager.isAdmin(this);
        Intent intent = new Intent(
                LoginActivity.this,
                admin ? AdminDashboardActivity.class : MainActivity.class);
        intent.putExtra(ExtraKeys.EXTRA_STANDARD_USER, !admin);
        intent.putExtra(ExtraKeys.EXTRA_USER, SessionManager.getUserName(this));
        if (admin) {
            intent.putExtra(ExtraKeys.EXTRA_USER_ROLE, ExtraKeys.ROLE_ADMIN);
            intent.putExtra(ExtraKeys.EXTRA_ACCESS_TOKEN, body.getAccess());
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void handleLoginError(ResponseBody errorBody) {
        Map<String, List<String>> errors = parseFieldErrors(errorBody);
        List<String> messages = new ArrayList<>();
        if (errors.containsKey("email")) {
            messages.addAll(errors.get("email"));
        }
        if (errors.containsKey("password")) {
            messages.addAll(errors.get("password"));
        }
        if (errors.containsKey("non_field_errors")) {
            messages.addAll(errors.get("non_field_errors"));
        }

        if (messages.isEmpty()) {
            showMessage(getString(R.string.error_login_generico));
        } else {
            showMessage(TextUtils.join(" ", messages));
        }
    }

    private Map<String, List<String>> parseFieldErrors(ResponseBody errorBody) {
        Map<String, List<String>> errors = new HashMap<>();
        if (errorBody == null) {
            return errors;
        }
        try {
            JsonObject json = new JsonParser().parse(errorBody.string()).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                String field = entry.getKey();
                if ("codigo".equals(field) || "status_code".equals(field)) {
                    continue;
                }
                List<String> messages = new ArrayList<>();
                JsonElement value = entry.getValue();
                if (value.isJsonArray()) {
                    for (JsonElement item : value.getAsJsonArray()) {
                        messages.add(item.getAsString());
                    }
                } else if (value.isJsonPrimitive()) {
                    messages.add(value.getAsString());
                }
                if (!messages.isEmpty()) {
                    errors.put(field, messages);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to parse login error body", e);
        }
        return errors;
    }

    private void showMessage(String message) {
        View toastView = getLayoutInflater().inflate(R.layout.toast_enrollment, null);
        ((TextView) toastView.findViewById(R.id.toast_message)).setText(message);

        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        toast.setGravity(
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL,
                0,
                getResources().getDimensionPixelSize(R.dimen.toast_bottom_offset));
        toast.setView(toastView);
        toast.show();
    }

    private void setLoading(boolean loading) {
        btnIngresar.setEnabled(!loading);
        btnIngresar.setText(loading ? R.string.ingresando : R.string.btn_ingresar);
    }
}

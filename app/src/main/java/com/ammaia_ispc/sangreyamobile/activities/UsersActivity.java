package com.ammaia_ispc.sangreyamobile.activities;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.ammaia_ispc.sangreyamobile.R;

import com.ammaia_ispc.sangreyamobile.data.UserApiRepository;
import com.ammaia_ispc.sangreyamobile.helpers.ApiClient;
import com.ammaia_ispc.sangreyamobile.helpers.ApiService;
import com.ammaia_ispc.sangreyamobile.helpers.UiHelper;
import com.ammaia_ispc.sangreyamobile.model.AuthUser;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


import android.widget.ImageButton;
import com.ammaia_ispc.sangreyamobile.helpers.NavigationHelper;
import com.ammaia_ispc.sangreyamobile.helpers.SessionManager;

public class UsersActivity extends AppCompatActivity {

    private LinearLayout usersContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!SessionManager.requireAdmin(this)) {
            return;
        }
        setContentView(R.layout.activity_users);

        NavigationHelper.configureBackButton(this, R.id.btnBack);

        // TODO. Conectar el componente admin bottom navigation, como en las otras
        // activities principales de Admin.



        usersContainer = findViewById(R.id.users_container);

        loadUsers();
    }

    private void loadUsers() {


        ApiService apiService = ApiClient.getApiService(this);


        apiService.getUsers().enqueue(new Callback<List<AuthUser>>() {

            @Override
            public void onResponse(
                    Call<List<AuthUser>> call,
                    Response<List<AuthUser>> response
            ) {
                if (!response.isSuccessful() || response.body() == null) {
                    return;
                }

                usersContainer.removeAllViews();

                for (AuthUser user : response.body()) {

                    LinearLayout userCard = new LinearLayout(UsersActivity.this);
                    userCard.setOrientation(LinearLayout.VERTICAL);
                    userCard.setPadding(24, 20, 24, 20);

                    GradientDrawable cardBackground = new GradientDrawable();
                    cardBackground.setColor(Color.WHITE);
                    cardBackground.setStroke(
                            3,
                            Color.parseColor("#C7C7C7")
                    );
                    cardBackground.setCornerRadius(18);

                    userCard.setBackground(cardBackground);

                    TextView name = new TextView(UsersActivity.this);
                    name.setText(
                            user.getNombre() + " " + user.getApellido()
                    );
                    name.setTextSize(18);
                    name.setTextColor(Color.parseColor("#262022"));
                    name.setTypeface(
                            null,
                            android.graphics.Typeface.BOLD
                    );

                    userCard.addView(name);

                    TextView email = new TextView(UsersActivity.this);
                    email.setText(user.getEmail());
                    email.setTextSize(14);
                    email.setTextColor(Color.parseColor("#70686B"));

                    userCard.addView(email);

                    TextView dni = new TextView(UsersActivity.this);
                    dni.setText("DNI: " + user.getDni());
                    dni.setTextSize(14);
                    dni.setTextColor(Color.parseColor("#70686B"));

                    userCard.addView(dni);

                    // =========================
                    // BOTÓN EDITAR (TK-59)
                    // =========================

                    Button editButton = new Button(UsersActivity.this);
                    editButton.setText("Editar");
                    editButton.setTextColor(Color.WHITE);
                    editButton.setAllCaps(false);
                    editButton.setGravity(Gravity.CENTER);

                    GradientDrawable editBackground = new GradientDrawable();
                    editBackground.setColor(Color.parseColor("#2E8B57"));
                    editBackground.setCornerRadius(18);
                    editButton.setBackground(editBackground);

                    editButton.setOnClickListener(v -> showEditUserDialog(user));

                    LinearLayout.LayoutParams editParams =
                            new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                    editParams.setMargins(0, 18, 0, 0);

                    userCard.addView(editButton, editParams);

                    userCard.setOnClickListener(v -> {

                        new android.app.AlertDialog.Builder(
                                UsersActivity.this
                        )
                                .setTitle("Datos del usuario")
                                .setMessage(
                                        "Nombre: "
                                                + user.getNombre()
                                                + " "
                                                + user.getApellido()
                                                + "\n\nEmail: "
                                                + user.getEmail()
                                                + "\n\nDNI: "
                                                + user.getDni()
                                                + "\n\nRol: "
                                                + user.getRol()
                                )
                                .setPositiveButton(
                                        "Cerrar",
                                        null
                                )
                                .show();
                    });

                    LinearLayout.LayoutParams cardParams =
                            new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );

                    cardParams.setMargins(0, 0, 0, 18);

                    usersContainer.addView(
                            userCard,
                            cardParams
                    );
                }
            }

            @Override
            public void onFailure(
                    Call<List<AuthUser>> call,
                    Throwable t
            ) {
                // La conexión con la API falló.
            }
        });
    }

    // =========================
    // EDITAR USUARIO (TK-59, conexión real al backend)
    // =========================

    private void showEditUserDialog(AuthUser user) {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 10, 40, 10);

        android.widget.EditText usernameInput = new android.widget.EditText(this);
        usernameInput.setHint("Usuario");
        usernameInput.setText(user.getUsername());

        android.widget.EditText emailInput = new android.widget.EditText(this);
        emailInput.setHint("Email");
        emailInput.setText(user.getEmail());

        android.widget.EditText nombreInput = new android.widget.EditText(this);
        nombreInput.setHint("Nombre");
        nombreInput.setText(user.getNombre());

        android.widget.EditText apellidoInput = new android.widget.EditText(this);
        apellidoInput.setHint("Apellido");
        apellidoInput.setText(user.getApellido());

        android.widget.EditText dniInput = new android.widget.EditText(this);
        dniInput.setHint("DNI");
        dniInput.setText(user.getDni());

        android.widget.EditText fechaNacimientoInput = new android.widget.EditText(this);
        fechaNacimientoInput.setHint("Fecha de nacimiento (AAAA-MM-DD)");
        fechaNacimientoInput.setText(user.getFechaNacimiento());

        layout.addView(usernameInput);
        layout.addView(emailInput);
        layout.addView(nombreInput);
        layout.addView(apellidoInput);
        layout.addView(dniInput);
        layout.addView(fechaNacimientoInput);

        new android.app.AlertDialog.Builder(this)
                .setTitle("Editar usuario")
                .setView(layout)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Guardar", (dialog, which) -> {

                    UserApiRepository.updateUser(
                            this,
                            user.getId(),
                            usernameInput.getText().toString().trim(),
                            emailInput.getText().toString().trim(),
                            dniInput.getText().toString().trim(),
                            nombreInput.getText().toString().trim(),
                            apellidoInput.getText().toString().trim(),
                            fechaNacimientoInput.getText().toString().trim(),
                            new UserApiRepository.UpdateUserCallback() {
                                @Override
                                public void onSuccess(AuthUser updatedUser) {
                                    UiHelper.showToast(
                                            UsersActivity.this,
                                            "Usuario actualizado correctamente.",
                                            Toast.LENGTH_SHORT
                                    );
                                    loadUsers();
                                }

                                @Override
                                public void onError(String message) {
                                    UiHelper.showToast(
                                            UsersActivity.this,
                                            message,
                                            Toast.LENGTH_LONG
                                    );
                                }
                            }
                    );
                })
                .show();
    }

}
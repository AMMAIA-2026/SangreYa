package com.ammaia_ispc.sangreyamobile.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.ammaia_ispc.sangreyamobile.R;
import com.ammaia_ispc.sangreyamobile.helpers.ApiClient;
import com.ammaia_ispc.sangreyamobile.helpers.ApiService;
import com.ammaia_ispc.sangreyamobile.helpers.ExtraKeys;
import com.ammaia_ispc.sangreyamobile.helpers.NavigationDrawerHelper;
import com.ammaia_ispc.sangreyamobile.model.AuthUser;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.ammaia_ispc.sangreyamobile.helpers.SessionManager;

public class UsersActivity extends AppCompatActivity {

    private static final int REQUEST_EDIT_USER = 1001;
    private LinearLayout usersContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.requireAdmin(this)) {
            return;
        }

        setContentView(R.layout.activity_users);

        DrawerLayout drawerLayout = findViewById(R.id.admin_users_drawer);
        NavigationView navigationView = findViewById(R.id.admin_users_navigation_view);
        NavigationDrawerHelper.configure(this, drawerLayout, navigationView);

        findViewById(R.id.admin_nav_dashboard).setOnClickListener(view -> openDashboard());
        findViewById(R.id.admin_nav_campaigns).setOnClickListener(view -> openCampaigns());
        findViewById(R.id.admin_nav_messages).setOnClickListener(view -> openMessages());

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
                            user.getName() + " " + user.getLastName()
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

                    userCard.setOnClickListener(v -> {

                        new android.app.AlertDialog.Builder(
                                UsersActivity.this
                        )
                                .setTitle("Datos del usuario")
                                .setMessage(
                                        "Nombre: "
                                                + user.getName()
                                                + " "
                                                + user.getLastName()
                                                + "\n\nEmail: "
                                                + user.getEmail()
                                                + "\n\nDNI: "
                                                + user.getDni()
                                                + "\n\nRol: "
                                                + user.getRole()
                                 )
                                 .setNeutralButton(
                                         "Editar",
                                         (dialog, which) -> openEditUser(user)
                                 )
                                 .setPositiveButton(
                                         "Cerrar",
                                         null
                                )
                                .setNegativeButton(
                                        "Eliminar",
                                        (dialog, which) -> {

                                            new android.app.AlertDialog.Builder(
                                                    UsersActivity.this
                                            )
                                                    .setTitle("Eliminar usuario")
                                                    .setMessage(
                                                            "¿Estás seguro de que querés eliminar a "
                                                                    + user.getName()
                                                                    + " "
                                                                    + user.getLastName()
                                                                    + "?"
                                                    )
                                                    .setNegativeButton(
                                                            "Cancelar",
                                                            null
                                                    )
                                                    .setPositiveButton(
                                                            "Eliminar",
                                                            (confirmDialog, confirmWhich) -> {

                                                                ApiService apiService =
                                                                        ApiClient.getApiService(
                                                                                UsersActivity.this
                                                                        );

                                                                apiService.deleteUser(
                                                                        user.getId()
                                                                ).enqueue(new Callback<Void>() {

                                                                    @Override
                                                                    public void onResponse(
                                                                            Call<Void> call,
                                                                            Response<Void> response
                                                                    ) {
                                                                        if (response.isSuccessful()) {
                                                                            loadUsers();
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onFailure(
                                                                            Call<Void> call,
                                                                            Throwable t
                                                                    ) {
                                                                        // La conexión con la API falló.
                                                                    }
                                                                });
                                                            }
                                                    )
                                                    .show();
                                        }
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

    private void openEditUser(AuthUser user) {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra(ExtraKeys.EXTRA_ADMIN_EDIT_USER_ID, user.getId());
        startActivityForResult(intent, REQUEST_EDIT_USER);
    }

    private void openDashboard() {
        Intent intent = new Intent(this, AdminDashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void openCampaigns() {
        navigateTo(AdminCampaignListActivity.class);
    }

    private void openMessages() {
        navigateTo(AdminContactListActivity.class);
    }

    private void navigateTo(Class<?> destination) {
        Intent intent = new Intent(this, destination);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT_USER && resultCode == RESULT_OK) {
            loadUsers();
        }
    }
}


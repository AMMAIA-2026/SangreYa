package com.ammaia_ispc.sangreyamobile.activities;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.ammaia_ispc.sangreyamobile.R;

import com.ammaia_ispc.sangreyamobile.helpers.ApiClient;
import com.ammaia_ispc.sangreyamobile.helpers.ApiService;
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





}



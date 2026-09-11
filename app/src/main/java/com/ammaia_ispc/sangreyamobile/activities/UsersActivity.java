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
import com.ammaia_ispc.sangreyamobile.data.MockUserRepository;
import com.ammaia_ispc.sangreyamobile.model.User;
import android.widget.ImageButton;
import com.ammaia_ispc.sangreyamobile.helpers.NavigationHelper;

public class UsersActivity extends AppCompatActivity {

private LinearLayout usersContainer;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_users);

    NavigationHelper.configureBackButton(this, R.id.btnBack);



    usersContainer = findViewById(R.id.users_container);

    loadUsers();
}

private void loadUsers() {

    for (User user : MockUserRepository.getUsers()) {

        // =========================
        // TARJETA DEL USUARIO
        // =========================

        LinearLayout userCard = new LinearLayout(this);
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


        // =========================
        // NOMBRE
        // =========================

        TextView name = new TextView(this);
        name.setText(user.getName());
        name.setTextSize(18);
        name.setTextColor(Color.parseColor("#262022"));
        name.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        LinearLayout.LayoutParams nameParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        nameParams.setMargins(0, 0, 0, 6);

        userCard.addView(name, nameParams);


        // =========================
        // EMAIL
        // =========================

        TextView email = new TextView(this);
        email.setText(user.getEmail());
        email.setTextSize(14);
        email.setTextColor(Color.parseColor("#70686B"));

        LinearLayout.LayoutParams emailParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        emailParams.setMargins(0, 0, 0, 6);

        userCard.addView(email, emailParams);


        // =========================
        // GRUPO SANGUÍNEO
        // =========================

        TextView bloodGroup = new TextView(this);
        bloodGroup.setText(
                "Grupo sanguíneo: " + user.getBloodGroup()
        );
        bloodGroup.setTextSize(14);
        bloodGroup.setTextColor(Color.parseColor("#70686B"));

        userCard.addView(bloodGroup);


        // =========================
        // ESPACIO ANTES DE BOTONES
        // =========================

        View buttonSpacer = new View(this);

        LinearLayout.LayoutParams spacerParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        28
                );

        userCard.addView(buttonSpacer, spacerParams);


        // =========================
        // CONTENEDOR DE BOTONES
        // =========================

        LinearLayout buttonsLayout = new LinearLayout(this);
        buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonsLayout.setGravity(Gravity.CENTER_VERTICAL);


        // =========================
        // BOTÓN EDITAR
        // =========================

        Button editButton = new Button(this);
        editButton.setText("Editar");
        editButton.setTextColor(Color.WHITE);
        editButton.setTextSize(20);
        editButton.setMinHeight(100);
        editButton.setPadding(20, 0, 20, 0);
        editButton.setAllCaps(false);
        editButton.setGravity(Gravity.CENTER);

        GradientDrawable editBackground =
                new GradientDrawable();

        editBackground.setColor(
                Color.parseColor("#2E8B57")
        );

        editBackground.setCornerRadius(18);

        editButton.setBackground(editBackground);

        editButton.setOnClickListener(v -> {
            showEditUserDialog(user);
        });

        LinearLayout.LayoutParams editParams =
                new LinearLayout.LayoutParams(
                        0,
                        100,
                        1.4f
                );

        buttonsLayout.addView(
                editButton,
                editParams
        );


        // =========================
        // ESPACIO ENTRE BOTONES
        // =========================

        View buttonGap = new View(this);

        LinearLayout.LayoutParams gapParams =
                new LinearLayout.LayoutParams(
                        18,
                        1
                );

        buttonsLayout.addView(
                buttonGap,
                gapParams
        );


        // =========================
        // BOTÓN ELIMINAR
        // =========================

        Button deleteButton = new Button(this);
        deleteButton.setText("Eliminar");
        deleteButton.setTextColor(Color.BLACK);
        deleteButton.setTextSize(18);
        deleteButton.setMinHeight(90);
        deleteButton.setPadding(16, 0, 16, 0);
        deleteButton.setAllCaps(false);
        deleteButton.setGravity(Gravity.CENTER);

        GradientDrawable deleteBackground =
                new GradientDrawable();

        deleteBackground.setColor(
                Color.parseColor("#D32F2F")
        );

        deleteBackground.setCornerRadius(18);

        deleteButton.setBackground(
                deleteBackground
        );

        deleteButton.setOnClickListener(v -> {
            showDeleteUserDialog(user);
        });

        LinearLayout.LayoutParams deleteParams =
                new LinearLayout.LayoutParams(
                        0,
                        90,
                        1f
                );

        buttonsLayout.addView(
                deleteButton,
                deleteParams
        );


        // Agregar botones a la tarjeta
        userCard.addView(buttonsLayout);


        // =========================
        // MARGEN ENTRE TARJETAS
        // =========================

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(
                0,
                0,
                0,
                18
        );


        // IMPORTANTE:
        // Agregar la tarjeta al contenedor
        usersContainer.addView(
                userCard,
                cardParams
        );


        // =========================
        // CONSULTAR DATOS
        // =========================

        userCard.setOnClickListener(v -> {

            new android.app.AlertDialog.Builder(
                    UsersActivity.this
            )
                    .setTitle("Datos del usuario")
                    .setMessage(
                            "Nombre: " + user.getName()
                                    + "\n\nEmail: " + user.getEmail()
                                    + "\n\nGrupo sanguíneo: "
                                    + user.getBloodGroup()
                    )
                    .setPositiveButton(
                            "Cerrar",
                            null
                    )
                    .show();
        });
    }
}


// =========================
// EDITAR USUARIO
// =========================

private void showEditUserDialog(User user) {

    LinearLayout layout = new LinearLayout(this);
    layout.setOrientation(
            LinearLayout.VERTICAL
    );
    layout.setPadding(
            40,
            10,
            40,
            10
    );


    android.widget.EditText nameInput =
            new android.widget.EditText(this);

    nameInput.setHint("Nombre");
    nameInput.setText(user.getName());


    android.widget.EditText emailInput =
            new android.widget.EditText(this);

    emailInput.setHint("Email");
    emailInput.setText(user.getEmail());


    android.widget.EditText bloodGroupInput =
            new android.widget.EditText(this);

    bloodGroupInput.setHint(
            "Grupo sanguíneo"
    );

    bloodGroupInput.setText(
            user.getBloodGroup()
    );


    layout.addView(nameInput);
    layout.addView(emailInput);
    layout.addView(bloodGroupInput);


    new android.app.AlertDialog.Builder(this)
            .setTitle("Editar usuario")
            .setView(layout)
            .setNegativeButton(
                    "Cancelar",
                    null
            )
            .setPositiveButton(
                    "Guardar",
                    (dialog, which) -> {

                        user.setName(
                                nameInput
                                        .getText()
                                        .toString()
                                        .trim()
                        );

                        user.setEmail(
                                emailInput
                                        .getText()
                                        .toString()
                                        .trim()
                        );

                        user.setBloodGroup(
                                bloodGroupInput
                                        .getText()
                                        .toString()
                                        .trim()
                        );

                        MockUserRepository.updateUser(
                                user
                        );

                        usersContainer.removeAllViews();

                        loadUsers();
                    }
            )
            .show();
}


// =========================
// ELIMINAR USUARIO
// =========================

private void showDeleteUserDialog(User user) {

    new android.app.AlertDialog.Builder(this)
            .setTitle("Eliminar usuario")
            .setMessage(
                    "¿Seguro que querés eliminar a "
                            + user.getName()
                            + "?"
            )
            .setNegativeButton(
                    "Cancelar",
                    null
            )
            .setPositiveButton(
                    "Eliminar",
                    (dialog, which) -> {

                        MockUserRepository.deleteUser(
                                user.getId()
                        );

                        usersContainer.removeAllViews();

                        loadUsers();
                    }
            )
            .show();
}
}



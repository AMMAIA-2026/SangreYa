package com.ammaia_ispc.sangreyamobile.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.widget.Toast;

import com.ammaia_ispc.sangreyamobile.R;
import com.ammaia_ispc.sangreyamobile.helpers.NavigationHelper;
import com.ammaia_ispc.sangreyamobile.helpers.SessionManager;
import com.ammaia_ispc.sangreyamobile.model.ContactMessage;
import com.google.android.material.button.MaterialButton;
import com.ammaia_ispc.sangreyamobile.helpers.ApiClient;
import com.ammaia_ispc.sangreyamobile.model.ContactTrackedRequest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminContactListActivity extends AppCompatActivity {

    private List<ContactMessage> contacts = new ArrayList<>();
    private LinearLayout contactContainer;
    private MaterialButton allFilter;
    private MaterialButton unansweredFilter;
    private MaterialButton answeredFilter;
    private String currentFilter = "Todos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.requireAdmin(this)) {
            return;
        }

        setContentView(R.layout.activity_admin_contact_list);

        bindViews();
        configureFilters();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadContacts();
    }

    private void loadContacts() {
        ApiClient.getApiService(this)
                .getContacts()
                .enqueue(new Callback<List<ContactMessage>>() {

                    @Override
                    public void onResponse(
                            Call<List<ContactMessage>> call,
                            Response<List<ContactMessage>> response) {

                        if (response.isSuccessful() && response.body() != null) {
                            contacts = response.body();
                            showContacts();
                        } else {
                            Toast.makeText(
                                    AdminContactListActivity.this,
                                    "No se pudieron cargar los contactos.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<ContactMessage>> call,
                            Throwable t) {

                        Toast.makeText(
                                AdminContactListActivity.this,
                                "No se pudo conectar con el servidor.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }



    private void bindViews() {
        contactContainer = findViewById(R.id.admin_contact_container);
        allFilter = findViewById(R.id.admin_contact_filter_all);
        unansweredFilter = findViewById(R.id.admin_contact_filter_unanswered);
        answeredFilter = findViewById(R.id.admin_contact_filter_answered);

        NavigationHelper.configureBackButton(this, R.id.btnBack);

        findViewById(R.id.admin_nav_dashboard).setOnClickListener(view -> openDashboard());
        findViewById(R.id.admin_nav_campaigns).setOnClickListener(view -> openCampaigns());
        findViewById(R.id.admin_nav_users).setOnClickListener(view -> openUsers());
    }

    private void configureFilters() {
        allFilter.setOnClickListener(view -> selectFilter("Todos"));
        unansweredFilter.setOnClickListener(view -> selectFilter("Sin responder"));
        answeredFilter.setOnClickListener(view -> selectFilter("Respondidos"));
    }

    private void selectFilter(String filter) {
        currentFilter = filter;
        showContacts();
    }

    private void showContacts() {
        contactContainer.removeAllViews();

        List<ContactMessage> ordered = new ArrayList<>(contacts);
        // CA-33: pendientes (sin seguimiento) primero
        ordered.sort(Comparator.comparing(ContactMessage::isTracked));

        for (ContactMessage contact : ordered) {
            if (currentFilter.equals("Sin responder") && contact.isTracked()) {
                continue;
            }
            if (currentFilter.equals("Respondidos") && !contact.isTracked()) {
                continue;
            }

            View card = createContactCard(contact);
            LinearLayout.LayoutParams cardParams =
                    (LinearLayout.LayoutParams) card.getLayoutParams();

            cardParams.setMargins(
                    0,
                    0,
                    0,
                    getResources().getDimensionPixelSize(R.dimen.card_spacing)
            );

            contactContainer.addView(card, cardParams);
        }

        updateFilterStyles();
    }

    private void updateFilterStyles() {
        styleFilter(allFilter, currentFilter.equals("Todos"));
        styleFilter(unansweredFilter, currentFilter.equals("Sin responder"));
        styleFilter(answeredFilter, currentFilter.equals("Respondidos"));
    }

    private void styleFilter(MaterialButton filter, boolean selected) {
        filter.setTextColor(ContextCompat.getColor(this, selected ? R.color.white : R.color.secondary_text));
        filter.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(this, selected ? R.color.primary_red : R.color.surface)));
    }

    private View createContactCard(ContactMessage contact) {
        View card = getLayoutInflater().inflate(
                R.layout.item_admin_contact,
                contactContainer,
                false
        );

        card.findViewById(R.id.contact_dot).setVisibility(
                contact.isTracked() ? View.INVISIBLE : View.VISIBLE
        );

        ((TextView) card.findViewById(R.id.contact_name))
                .setText(contact.getName());

        ((TextView) card.findViewById(R.id.contact_time))
                .setText(contact.getCreatedAt());

        ((TextView) card.findViewById(R.id.contact_reason))
                .setText(contact.getReason());

        ((TextView) card.findViewById(R.id.contact_message))
                .setText(contact.getMessage());

        card.setOnClickListener(view -> updateContactTracked(contact));

        return card;
    }

    private void updateContactTracked(ContactMessage contact) {
        ContactTrackedRequest request = new ContactTrackedRequest(true);

        ApiClient.getApiService(this)
                .updateContactTracked(contact.getId(), request)
                .enqueue(new Callback<ContactMessage>() {

                    @Override
                    public void onResponse(
                            Call<ContactMessage> call,
                            Response<ContactMessage> response) {

                        if (response.isSuccessful() && response.body() != null) {
                            contacts.remove(contact);
                            contacts.add(response.body());
                            showContacts();
                        } else {
                            Toast.makeText(
                                    AdminContactListActivity.this,
                                    "No se pudo actualizar el seguimiento.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ContactMessage> call,
                            Throwable t) {

                        Toast.makeText(
                                AdminContactListActivity.this,
                                "No se pudo conectar con el servidor.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }



    private void openDashboard() {
        Intent intent = new Intent(this, AdminDashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void openCampaigns() {
        Intent intent = new Intent(this, AdminCampaignListActivity.class);
        startActivity(intent);
    }

    private void openUsers() {
        Intent intent = new Intent(this, UsersActivity.class);
        startActivity(intent);
    }
}

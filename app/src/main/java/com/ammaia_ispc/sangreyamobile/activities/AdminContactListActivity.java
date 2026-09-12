package com.ammaia_ispc.sangreyamobile.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.ammaia_ispc.sangreyamobile.R;
import com.ammaia_ispc.sangreyamobile.data.MockContactRepository;
import com.ammaia_ispc.sangreyamobile.helpers.AdminDashboardHelper;
import com.ammaia_ispc.sangreyamobile.helpers.ExtraKeys;
import com.ammaia_ispc.sangreyamobile.helpers.NavigationHelper;
import com.ammaia_ispc.sangreyamobile.model.ContactMessage;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AdminContactListActivity extends AppCompatActivity {

    private List<ContactMessage> contacts;
    private LinearLayout contactContainer;
    private MaterialButton allFilter;
    private MaterialButton unansweredFilter;
    private MaterialButton answeredFilter;
    private String currentFilter = "Todos";
    private String user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user = getIntent().getStringExtra(ExtraKeys.EXTRA_USER);
        String role = getIntent().getStringExtra(ExtraKeys.EXTRA_USER_ROLE);

        // CA-35: acceso no autorizado (simula el 403 hasta que exista el backend real)
        if (!ExtraKeys.ROLE_ADMIN.equals(role)) {
            Toast.makeText(this, getString(R.string.error_unauthorized_access), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (user == null) {
            user = AdminDashboardHelper.MOCK_ADMIN_EMAIL;
        }

        setContentView(R.layout.activity_admin_contact_list);

        bindViews();
        configureFilters();
    }

    @Override
    protected void onResume() {
        super.onResume();
        contacts = MockContactRepository.getContacts();
        showContacts();
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
        ordered.sort(Comparator.comparing(contact -> contact.tracked));

        for (ContactMessage contact : ordered) {
            if (currentFilter.equals("Sin responder") && contact.tracked) {
                continue;
            }
            if (currentFilter.equals("Respondidos") && !contact.tracked) {
                continue;
            }

            View card = createContactCard(contact);
            LinearLayout.LayoutParams cardParams = (LinearLayout.LayoutParams) card.getLayoutParams();
            cardParams.setMargins(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.card_spacing));
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
        View card = getLayoutInflater().inflate(R.layout.item_admin_contact, contactContainer, false);

        card.findViewById(R.id.contact_dot).setVisibility(contact.tracked ? View.INVISIBLE : View.VISIBLE);
        ((TextView) card.findViewById(R.id.contact_name)).setText(contact.name);
        ((TextView) card.findViewById(R.id.contact_time)).setText(contact.time);
        ((TextView) card.findViewById(R.id.contact_reason)).setText(contact.reason);
        ((TextView) card.findViewById(R.id.contact_message)).setText(contact.message);

        // CA-34 (simulado): tocar la card marca el contacto como "seguido"
        card.setOnClickListener(view -> {
            contact.tracked = true;
            showContacts();
        });

        return card;
    }

    private void openDashboard() {
        Intent intent = new Intent(this, AdminDashboardActivity.class);
        intent.putExtra(ExtraKeys.EXTRA_USER, user);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void openCampaigns() {
        Intent intent = new Intent(this, AdminCampaignListActivity.class);
        intent.putExtra(ExtraKeys.EXTRA_USER, user);
        startActivity(intent);
    }

    private void openUsers() {
        Intent intent = new Intent(this, UsersActivity.class);
        intent.putExtra(ExtraKeys.EXTRA_USER, user);
        startActivity(intent);
    }
}
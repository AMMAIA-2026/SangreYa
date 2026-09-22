package com.ammaia_ispc.sangreyamobile.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.ammaia_ispc.sangreyamobile.R;
import com.ammaia_ispc.sangreyamobile.data.CampaignApiRepository;
import com.ammaia_ispc.sangreyamobile.helpers.CampaignHelper;
import com.ammaia_ispc.sangreyamobile.helpers.ExtraKeys;
import com.ammaia_ispc.sangreyamobile.helpers.NavigationDrawerHelper;
import com.ammaia_ispc.sangreyamobile.helpers.SessionManager;
import com.ammaia_ispc.sangreyamobile.model.Campaign;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class CampaignListActivity extends AppCompatActivity {
    private List<Campaign> campaigns = new ArrayList<>();
    private LinearLayout campaignContainer;
    private ProgressBar loadingIndicator;
    private ScrollView campaignScroll;
    private MaterialButton allFilter;
    private MaterialButton activeFilter;
    private MaterialButton upcomingFilter;
    private EditText searchInput;
    private boolean standardUser;
    private String user;
    private String role;
    private String currentFilter = "Todas";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CampaignHelper.configureSystemBars(this);
        standardUser = getIntent().getBooleanExtra(ExtraKeys.EXTRA_STANDARD_USER, false);
        user = getIntent().getStringExtra(ExtraKeys.EXTRA_USER);
        role = getIntent().getStringExtra(ExtraKeys.EXTRA_USER_ROLE);
        if (TextUtils.isEmpty(user)) {
            user = SessionManager.getUserName(this);
        }
        if (!standardUser && !TextUtils.isEmpty(user) && !SessionManager.isAdmin(this)) {
            standardUser = true;
        }
        if (TextUtils.isEmpty(role) && SessionManager.isAdmin(this)) {
            role = ExtraKeys.ROLE_ADMIN;
        }
        setContentView(R.layout.activity_campaign_list);
        bindViews();
        configureFilters();
        configureSearch();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCampaigns();
    }

    private void loadCampaigns() {
        setLoading(true);
        CampaignApiRepository.getCampaigns(new CampaignApiRepository.Callback<List<Campaign>>() {
            @Override
            public void onSuccess(List<Campaign> value) {
                campaigns = value;
                showCampaigns(currentFilter);
                setLoading(false);
            }

            @Override
            public void onError(Exception exception) {
                setLoading(false);
                String detail = exception.getMessage();
                if (detail == null || detail.isEmpty()) {
                    detail = exception.getClass().getSimpleName();
                }
                Toast.makeText(
                        CampaignListActivity.this,
                        "No se pudieron cargar las campañas: " + detail,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        loadingIndicator.setVisibility(loading ? View.VISIBLE : View.GONE);
        campaignScroll.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
    }

    private void bindViews() {
        campaignContainer = findViewById(R.id.campaign_container);
        loadingIndicator = findViewById(R.id.campaign_loading);
        campaignScroll = findViewById(R.id.campaign_scroll);
        allFilter = findViewById(R.id.filter_all);
        activeFilter = findViewById(R.id.filter_active);
        upcomingFilter = findViewById(R.id.filter_upcoming);
        searchInput = findViewById(R.id.campaign_search);
        DrawerLayout drawerLayout = findViewById(R.id.campaign_drawer);
        NavigationView navigationView = findViewById(R.id.campaign_navigation_view);
        NavigationDrawerHelper.configure(this, drawerLayout, navigationView, standardUser, user, role);
        if (!standardUser) {
            findViewById(R.id.bottom_navigation).setVisibility(View.GONE);
        }
    }

    private void configureFilters() {
        allFilter.setOnClickListener(view -> selectFilter("Todas"));
        activeFilter.setOnClickListener(view -> selectFilter("Activas"));
        upcomingFilter.setOnClickListener(view -> selectFilter("Próximas"));
    }

    private void configureSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                showCampaigns(currentFilter);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void selectFilter(String filter) {
        currentFilter = filter;
        showCampaigns(currentFilter);
    }

    private void showCampaigns(String filter) {
        campaignContainer.removeAllViews();
        String query = searchInput.getText().toString().trim().toLowerCase(Locale.getDefault());
        List<Campaign> orderedCampaigns = new ArrayList<>(campaigns);
        orderedCampaigns.sort(Comparator
                .comparingInt((Campaign campaign) -> campaignStatusOrder(campaign.calculatedStatus))
                .thenComparing(campaign -> campaign.startDate));

        for (Campaign campaign : orderedCampaigns) {
            if ("Finalizada".equals(campaign.calculatedStatus)) {
                continue;
            }
            if (filter.equals("Activas") && !campaign.calculatedStatus.equals("Activa")) {
                continue;
            }
            if (filter.equals("Próximas") && !campaign.calculatedStatus.equals("Proximamente")) {
                continue;
            }
            if (!query.isEmpty()
                    && !campaign.title.toLowerCase(Locale.getDefault()).contains(query)) {
                continue;
            }
            View card = createCampaignCard(campaign);
            LinearLayout.LayoutParams cardParams = (LinearLayout.LayoutParams) card.getLayoutParams();
            cardParams.setMargins(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.card_spacing));
            campaignContainer.addView(card, cardParams);
        }
        updateFilterStyles(filter);
    }

    private int campaignStatusOrder(String status) {
        if ("Activa".equals(status)) {
            return 0;
        }
        if ("Proximamente".equals(status)) {
            return 1;
        }
        return 2;
    }

    private void updateFilterStyles(String selectedFilter) {
        styleFilter(allFilter, selectedFilter.equals("Todas"));
        styleFilter(activeFilter, selectedFilter.equals("Activas"));
        styleFilter(upcomingFilter, selectedFilter.equals("Próximas"));
    }

    private void styleFilter(MaterialButton filter, boolean selected) {
        filter.setTextColor(ContextCompat.getColor(this, selected ? R.color.white : R.color.secondary_text));
        filter.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(this, selected ? R.color.primary_red : R.color.surface)));
    }

    private View createCampaignCard(Campaign campaign) {
        View card = getLayoutInflater().inflate(R.layout.item_campaign, campaignContainer, false);
        card.setOnClickListener(view -> openDetails(campaign));
        ((TextView) card.findViewById(R.id.campaign_title)).setText(campaign.title);
        TextView status = card.findViewById(R.id.campaign_status);
        status.setText(CampaignHelper.statusText(campaign.calculatedStatus));
        status.setTextColor(ContextCompat.getColor(this, CampaignHelper.statusColor(campaign.calculatedStatus)));
        status.setBackgroundResource(CampaignHelper.statusBackground(campaign.calculatedStatus));
        ((TextView) card.findViewById(R.id.campaign_location)).setText(campaign.location);
        ((TextView) card.findViewById(R.id.campaign_dates)).setText(CampaignHelper.formatShortDate(campaign.startDate, campaign.endDate));
        ((TextView) card.findViewById(R.id.campaign_registered)).setText(getString(R.string.registered_count, campaign.totalRegistered));
        TextView capacity = card.findViewById(R.id.campaign_capacity);
        if (campaign.maximumCapacity == null) {
            capacity.setText(R.string.unlimited_capacity);
        } else {
            capacity.setText(getString(R.string.maximum_capacity, campaign.maximumCapacity));
        }
        return card;
    }

    private void openDetails(Campaign campaign) {
        Intent intent = new Intent(this, CampaignDetailActivity.class);
        intent.putExtra(ExtraKeys.EXTRA_CAMPAIGN, campaign);
        intent.putExtra(ExtraKeys.EXTRA_REMOTE_CAMPAIGN_DETAIL, true);
        intent.putExtra(ExtraKeys.EXTRA_STANDARD_USER, standardUser);
        intent.putExtra(ExtraKeys.EXTRA_USER, user);
        intent.putExtra(ExtraKeys.EXTRA_USER_ROLE, role);
        startActivity(intent);
    }
}

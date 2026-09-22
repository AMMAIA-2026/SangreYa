package com.ammaia_ispc.sangreyamobile.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.ammaia_ispc.sangreyamobile.R;
import com.ammaia_ispc.sangreyamobile.data.CampaignApiRepository;
import com.ammaia_ispc.sangreyamobile.helpers.CampaignHelper;
import com.ammaia_ispc.sangreyamobile.helpers.ExtraKeys;
import com.ammaia_ispc.sangreyamobile.helpers.NavigationHelper;
import com.ammaia_ispc.sangreyamobile.helpers.NavigationDrawerHelper;
import com.ammaia_ispc.sangreyamobile.helpers.SessionManager;
import com.ammaia_ispc.sangreyamobile.model.Campaign;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminCampaignListActivity extends AppCompatActivity {

    private List<Campaign> campaigns = new ArrayList<>();
    private LinearLayout campaignContainer;
    private EditText searchInput;
    private MaterialButton allFilter;
    private MaterialButton activeFilter;
    private MaterialButton upcomingFilter;
    private MaterialButton finishedFilter;
    private String currentFilter = "Todas";
    private String user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!SessionManager.requireAdmin(this)) {
            return;
        }

        CampaignHelper.configureSystemBars(this);

        user = getIntent().getStringExtra(ExtraKeys.EXTRA_USER);

        if (TextUtils.isEmpty(user)) {
            user = SessionManager.getUserName(this);
        }

        setContentView(R.layout.activity_admin_campaign_list);

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
        String accessToken = SessionManager.getAccessToken(this);
        if (accessToken == null || accessToken.trim().isEmpty()) {
            showCampaignError(new CampaignApiRepository.HttpException(
                    401,
                    "No hay un token de administrador"));
            return;
        }

        CampaignApiRepository.getCampaigns(
                accessToken,
                new CampaignApiRepository.Callback<List<Campaign>>() {
                    @Override
                    public void onSuccess(List<Campaign> value) {
                        campaigns = value;
                        showCampaigns();
                    }

                    @Override
                    public void onError(Exception exception) {
                        showCampaignError(exception);
                    }
                });
    }

    private void showCampaignError(Exception exception) {
        String message = CampaignApiRepository.isUnauthorized(exception)
                ? "No tenés permisos para consultar las campañas (401/403)."
                : "No se pudieron cargar las campañas. Revisá tu conexión e intentá nuevamente.";
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void bindViews() {

        campaignContainer = findViewById(R.id.admin_campaign_container);
        searchInput = findViewById(R.id.admin_campaign_search);
        allFilter = findViewById(R.id.admin_filter_all);
        activeFilter = findViewById(R.id.admin_filter_active);
        upcomingFilter = findViewById(R.id.admin_filter_upcoming);
        finishedFilter = findViewById(R.id.admin_filter_finished);

        DrawerLayout drawerLayout = findViewById(R.id.admin_campaign_drawer);
        NavigationView navigationView = findViewById(R.id.admin_campaign_navigation_view);
        NavigationDrawerHelper.configure(
                this,
                drawerLayout,
                navigationView,
                false,
                user,
                ExtraKeys.ROLE_ADMIN);

        findViewById(R.id.admin_campaign_add)
                .setOnClickListener(view -> openCreateCampaign());

        findViewById(R.id.admin_nav_dashboard)
                .setOnClickListener(view -> openDashboard());

        findViewById(R.id.admin_nav_users)
                .setOnClickListener(view -> {
                    Intent intent = new Intent(this, UsersActivity.class);
                    intent.putExtra(ExtraKeys.EXTRA_USER, user);
                    startActivity(intent);
                });

        findViewById(R.id.admin_messages_navigation).setOnClickListener(view -> {
            Intent intent = new Intent(this, AdminContactListActivity.class);
            intent.putExtra(ExtraKeys.EXTRA_USER, user);
            intent.putExtra(ExtraKeys.EXTRA_USER_ROLE, ExtraKeys.ROLE_ADMIN);
            startActivity(intent);
        });

        // Flecha reutilizable
        NavigationHelper.configureBackButton(this, R.id.btnBack);
    }

    private void configureFilters() {
        allFilter.setOnClickListener(view -> selectFilter("Todas"));
        activeFilter.setOnClickListener(view -> selectFilter("Activa"));
        upcomingFilter.setOnClickListener(view -> selectFilter("Proximamente"));
        finishedFilter.setOnClickListener(view -> selectFilter("Finalizada"));
    }

    private void configureSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count) {
                showCampaigns();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void selectFilter(String filter) {
        currentFilter = filter;
        showCampaigns();
    }

    private void showCampaigns() {

        String query = searchInput
                .getText()
                .toString()
                .trim()
                .toLowerCase(Locale.getDefault());

        campaignContainer.removeAllViews();

        for (Campaign campaign : campaigns) {

            if (!currentFilter.equals("Todas")
                    && !campaign.calculatedStatus.equals(currentFilter)) {
                continue;
            }

            if (!query.isEmpty()
                    && !campaign.title
                    .toLowerCase(Locale.getDefault())
                    .contains(query)) {
                continue;
            }

            View card = createCampaignCard(campaign);

            LinearLayout.LayoutParams cardParams =
                    (LinearLayout.LayoutParams) card.getLayoutParams();

            cardParams.setMargins(
                    0,
                    0,
                    0,
                    getResources().getDimensionPixelSize(R.dimen.card_spacing)
            );

            campaignContainer.addView(card, cardParams);
        }

        updateFilterStyles();
    }

    private void updateFilterStyles() {
        styleFilter(allFilter, currentFilter.equals("Todas"));
        styleFilter(activeFilter, currentFilter.equals("Activa"));
        styleFilter(upcomingFilter, currentFilter.equals("Proximamente"));
        styleFilter(finishedFilter, currentFilter.equals("Finalizada"));
    }

    private void styleFilter(MaterialButton filter, boolean selected) {

        filter.setTextColor(
                ContextCompat.getColor(
                        this,
                        selected
                                ? R.color.white
                                : R.color.secondary_text
                )
        );

        filter.setBackgroundTintList(
                ColorStateList.valueOf(
                        ContextCompat.getColor(
                                this,
                                selected
                                        ? R.color.primary_red
                                        : R.color.surface
                        )
                )
        );
    }

    private View createCampaignCard(Campaign campaign) {

        View card = getLayoutInflater().inflate(
                R.layout.item_admin_campaign,
                campaignContainer,
                false
        );

        card.setOnClickListener(
                view -> openCampaignDetail(campaign)
        );

        ((TextView) card.findViewById(R.id.admin_campaign_title))
                .setText(campaign.title);

        TextView status =
                card.findViewById(R.id.admin_campaign_status);

        status.setText(
                CampaignHelper.statusText(
                        campaign.calculatedStatus
                )
        );

        status.setTextColor(
                ContextCompat.getColor(
                        this,
                        CampaignHelper.statusColor(
                                campaign.calculatedStatus
                        )
                )
        );

        status.setBackgroundResource(
                CampaignHelper.statusBackground(
                        campaign.calculatedStatus
                )
        );

        String locationAndDates =
                campaign.location
                        + " · "
                        + CampaignHelper.formatShortDate(
                        campaign.startDate,
                        campaign.endDate
                );

        ((TextView) card.findViewById(R.id.admin_campaign_location))
                .setText(locationAndDates);

        TextView capacity =
                card.findViewById(R.id.admin_campaign_capacity);

        if (campaign.maximumCapacity != null) {

            capacity.setText(
                    getString(
                            R.string.capacity_occupied,
                            campaign.totalRegistered,
                            campaign.maximumCapacity
                    )
            );

        } else {

            capacity.setText(
                    getString(
                            R.string.registered_count,
                            campaign.totalRegistered
                    )
            );
        }

        ImageView editButton =
                card.findViewById(R.id.admin_campaign_edit);

        editButton.setOnClickListener(
                view -> openCampaignEdit(campaign)
        );

        return card;
    }

    private void openCreateCampaign() {

        Intent intent =
                new Intent(this, CreateCampaignActivity.class);

        intent.putExtra(
                ExtraKeys.EXTRA_STANDARD_USER,
                false
        );

        intent.putExtra(
                ExtraKeys.EXTRA_USER,
                user
        );

        intent.putExtra(
                ExtraKeys.EXTRA_USER_ROLE,
                ExtraKeys.ROLE_ADMIN
        );

        startActivity(intent);
    }

    private void openCampaignDetail(Campaign campaign) {

        Intent intent =
                new Intent(this, CampaignDetailActivity.class);

        intent.putExtra(
                ExtraKeys.EXTRA_CAMPAIGN,
                campaign
        );

        intent.putExtra(
                ExtraKeys.EXTRA_STANDARD_USER,
                false
        );

        intent.putExtra(
                ExtraKeys.EXTRA_REMOTE_CAMPAIGN_DETAIL,
                true
        );

        intent.putExtra(
                ExtraKeys.EXTRA_USER,
                user
        );

        intent.putExtra(
                ExtraKeys.EXTRA_USER_ROLE,
                ExtraKeys.ROLE_ADMIN
        );

        startActivity(intent);
    }

    private void openCampaignEdit(Campaign campaign) {

        Intent intent =
                new Intent(this, CreateCampaignActivity.class);

        intent.putExtra(
                ExtraKeys.EXTRA_CAMPAIGN,
                campaign
        );

        intent.putExtra(
                ExtraKeys.EXTRA_STANDARD_USER,
                false
        );

        intent.putExtra(
                ExtraKeys.EXTRA_USER,
                user
        );

        intent.putExtra(
                ExtraKeys.EXTRA_USER_ROLE,
                ExtraKeys.ROLE_ADMIN
        );

        startActivity(intent);
    }

    private void openDashboard() {

        Intent intent =
                new Intent(this, AdminDashboardActivity.class);

        intent.putExtra(
                ExtraKeys.EXTRA_USER,
                user
        );

        intent.putExtra(
                ExtraKeys.EXTRA_ACCESS_TOKEN,
                SessionManager.getAccessToken(this)
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP
        );

        startActivity(intent);
        finish();
    }
}

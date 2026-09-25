package com.ammaia_ispc.sangreyamobile.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
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
import com.ammaia_ispc.sangreyamobile.helpers.UiHelper;
import com.ammaia_ispc.sangreyamobile.model.Campaign;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.appcompat.app.AlertDialog;

public class AdminCampaignListActivity extends AppCompatActivity {

    private List<Campaign> campaigns = new ArrayList<>();
    private LinearLayout campaignContainer;
    private EditText searchInput;
    private MaterialButton allFilter;
    private MaterialButton activeFilter;
    private MaterialButton upcomingFilter;
    private MaterialButton finishedFilter;
    private String currentFilter = "Todas";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!SessionManager.requireAdmin(this)) {
            return;
        }

        UiHelper.configureSystemBars(this);

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
        CampaignApiRepository.getCampaigns(
                this,
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
                ? getString(R.string.admin_campaigns_unauthorized)
                : getString(R.string.campaigns_load_error);
        UiHelper.showToast(this, message, Toast.LENGTH_LONG);
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
                navigationView);

        findViewById(R.id.admin_campaign_add)
                .setOnClickListener(view -> openCreateCampaign());

        findViewById(R.id.admin_nav_dashboard)
                .setOnClickListener(view -> openDashboard());

        findViewById(R.id.admin_nav_users)
                .setOnClickListener(view -> {
                    Intent intent = new Intent(this, UsersActivity.class);
                    startActivity(intent);
                });

        findViewById(R.id.admin_messages_navigation).setOnClickListener(view -> {
            Intent intent = new Intent(this, AdminContactListActivity.class);
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
                .toLowerCase(Locale.getDefault()); //TODO. Aplicar el UiHelper.normalized

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

        ImageView deleteButton =
                card.findViewById(R.id.admin_campaign_delete);

        deleteButton.setOnClickListener(
                view -> confirmDeleteCampaign(campaign)
        );

        return card;
    }

    private void confirmDeleteCampaign(Campaign campaign) {

        new AlertDialog.Builder(this)
                .setTitle("Eliminar campaña")
                .setMessage(
                        "¿Estás seguro de que querés eliminar la campaña \""
                                + campaign.title
                                + "\"?"
                )
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", (dialog, which) ->
                        deleteCampaign(campaign)
                )
                .show();
    }

    private void deleteCampaign(Campaign campaign) {
        String accessToken = SessionManager.getAccessToken(this);

        if (accessToken == null || accessToken.trim().isEmpty()) {
            //TODO. Este toast se puede pasar a helper, Toast.makeText
            Toast.makeText(
                    this,
                    "No hay una sesión de administrador activa.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        // TODO (refactor pendiente): este flujo queda fuera del alcance actual.
        // Motivo: hay que centralizar el manejo de jwt y auth.
        // Aquí todavía usa HttpURLConnection y transporta el accessToken manualmente.
        // Pasos para aplicarlo:
        // 1. Agregar el DELETE en ApiService.
        // 2. Cambiar el repository para recibir Context y usar ApiClient.
        // 3. Dejar que AuthInterceptor y TokenAuthenticator administren el JWT.
        // 4. Reemplazar la llamada siguiente por este ejemplo:
        // CampaignApiRepository.deleteCampaign(this, campaign.id, callback);
        CampaignApiRepository.deleteCampaign(
                campaign.id,
                accessToken,
                new CampaignApiRepository.Callback<Void>() {

                    @Override
                    public void onSuccess(Void value) {
                        //TODO. Este toast se puede pasar a helper, Toast.makeText
                        Toast.makeText(
                                AdminCampaignListActivity.this,
                                "Campaña eliminada correctamente.",
                                Toast.LENGTH_SHORT
                        ).show();

                        loadCampaigns();
                    }

                    @Override
                    public void onError(Exception exception) {

                        String message;

                        if (CampaignApiRepository.isUnauthorized(exception)) {
                            message = "No tenés permisos para eliminar esta campaña.";
                        } else if (CampaignApiRepository.isNotFound(exception)) {
                            message = "La campaña no existe o ya fue eliminada.";
                        } else if (CampaignApiRepository.isNetworkError(exception)) {
                            message = "No se pudo conectar con el servidor. Revisá tu conexión.";
                        } else {
                            message = "No se pudo eliminar la campaña. Intentá nuevamente.";
                        }

                        //TODO. Este toast se puede pasar a helper, Toast.makeText
                        Toast.makeText(
                                AdminCampaignListActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void openCreateCampaign() {

        Intent intent =
                new Intent(this, CreateCampaignActivity.class);

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
                ExtraKeys.EXTRA_REMOTE_CAMPAIGN_DETAIL,
                true
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

        startActivity(intent);
    }

    private void openDashboard() {

        Intent intent =
                new Intent(this, AdminDashboardActivity.class);

        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP
        );

        startActivity(intent);
        finish();
    }
}

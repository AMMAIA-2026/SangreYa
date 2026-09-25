package com.ammaia_ispc.sangreyamobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.ammaia_ispc.sangreyamobile.R;
import com.ammaia_ispc.sangreyamobile.data.CampaignApiRepository;
import com.ammaia_ispc.sangreyamobile.data.DashboardApiRepository;
import com.ammaia_ispc.sangreyamobile.model.AdminDashboardData;
import com.ammaia_ispc.sangreyamobile.model.Campaign;
import com.ammaia_ispc.sangreyamobile.model.DashboardCampaignStatus;
import com.ammaia_ispc.sangreyamobile.helpers.AdminDashboardHelper;
import com.ammaia_ispc.sangreyamobile.helpers.CampaignHelper;
import com.ammaia_ispc.sangreyamobile.helpers.DashboardChartView;
import com.ammaia_ispc.sangreyamobile.helpers.ExtraKeys;
import com.ammaia_ispc.sangreyamobile.helpers.NavigationDrawerHelper;
import com.ammaia_ispc.sangreyamobile.helpers.SessionManager;
import com.google.android.material.navigation.NavigationView;

import android.widget.Toast;

public class AdminDashboardActivity extends AppCompatActivity {
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
        setContentView(R.layout.activity_admin_dashboard);
        bindViews();
    }

    private void bindViews() {
        DrawerLayout drawerLayout = findViewById(R.id.admin_dashboard_drawer);
        NavigationView navigationView = findViewById(R.id.admin_dashboard_navigation_view);
        NavigationDrawerHelper.configure(
                this,
                drawerLayout,
                navigationView,
                false,
                user,
                ExtraKeys.ROLE_ADMIN);
        findViewById(R.id.admin_campaigns_navigation).setOnClickListener(view -> openAllCampaigns());

        findViewById(R.id.admin_users_navigation).setOnClickListener(view -> {
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

        String accessToken = SessionManager.getAccessToken(this);
        if (accessToken == null || accessToken.trim().isEmpty()) {
            Toast.makeText(
                    this,
                    R.string.dashboard_token_required,
                    Toast.LENGTH_LONG).show();
            return;
        }

        DashboardApiRepository.getDashboard(
                accessToken,
                new CampaignApiRepository.Callback<AdminDashboardData>() {
                    @Override
                    public void onSuccess(AdminDashboardData dashboard) {
                        bindDashboardMetrics(dashboard);
                        bindStatusChart(dashboard);
                        bindRecentCampaigns(dashboard);
                    }

                    @Override
                    public void onError(Exception exception) {
                        String message = CampaignApiRepository.isUnauthorized(exception)
                                ? getString(R.string.dashboard_unauthorized_error)
                                : getString(R.string.dashboard_load_error);
                        Toast.makeText(
                                AdminDashboardActivity.this,
                                message,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void bindDashboardMetrics(AdminDashboardData dashboard) {
        ((TextView) findViewById(R.id.dashboard_campaigns_total))
                .setText(getString(R.string.dashboard_campaigns_total, dashboard.totalCampanias));
        ((TextView) findViewById(R.id.dashboard_enrollments_total))
                .setText(getString(R.string.dashboard_enrollments_total, dashboard.totalInscripciones));
        ((TextView) findViewById(R.id.dashboard_donors_total))
                .setText(getString(R.string.dashboard_donors_total, dashboard.totalDonantes));

        DashboardChartView donorsChart = findViewById(R.id.dashboard_donors_chart);
        donorsChart.setMonthlyDonors(dashboard.donantesPorMes);
        donorsChart.setEmptyMessage(dashboard.donantesPorMes.isEmpty()
                ? getString(R.string.dashboard_no_donors)
                : "");

        DashboardChartView enrollmentsChart = findViewById(R.id.dashboard_enrollments_chart);
        enrollmentsChart.setMonthlyDonors(dashboard.inscripcionesPorMes);
        enrollmentsChart.setEmptyMessage(dashboard.inscripcionesPorMes.isEmpty()
                ? getString(R.string.dashboard_no_enrollments)
                : "");
    }

    private void bindStatusChart(AdminDashboardData dashboard) {
        DashboardChartView chart = findViewById(R.id.dashboard_status_chart);
        chart.setCampaignStatuses(dashboard.campaniasPorEstado);

        LinearLayout legend = findViewById(R.id.dashboard_status_legend);
        legend.removeAllViews();
        int total = AdminDashboardHelper.totalCampaigns(dashboard.campaniasPorEstado);
        for (DashboardCampaignStatus status : dashboard.campaniasPorEstado) {
            TextView item = new TextView(this);
            item.setText(getString(
                    R.string.dashboard_status_legend,
                    status.estado,
                    AdminDashboardHelper.percentage(status.cantidad, total)));
            item.setTextColor(ContextCompat.getColor(
                    this,
                    CampaignHelper.statusColor(status.estado)));
            item.setTextSize(10);
            item.setTypeface(item.getTypeface(), android.graphics.Typeface.BOLD);
            item.setPadding(0, 8, 0, 8);
            legend.addView(item);
        }
    }

    private void bindRecentCampaigns(AdminDashboardData dashboard) {
        LinearLayout container = findViewById(R.id.dashboard_recent_container);
        container.removeAllViews();
        for (Campaign campaign : AdminDashboardHelper.orderRecentCampaigns(dashboard.campaniasRecientes)) {
            View item = getLayoutInflater().inflate(
                    R.layout.item_admin_recent_campaign,
                    container,
                    false);
            ((TextView) item.findViewById(R.id.admin_campaign_title)).setText(campaign.title);
            ((TextView) item.findViewById(R.id.admin_campaign_location)).setText(campaign.location);
            ((TextView) item.findViewById(R.id.admin_campaign_dates)).setText(
                    CampaignHelper.formatShortDate(campaign.startDate, campaign.endDate));
            ((TextView) item.findViewById(R.id.admin_campaign_registered)).setText(
                    getString(R.string.dashboard_campaign_registered, campaign.totalRegistered));

            TextView status = item.findViewById(R.id.admin_campaign_status);
            status.setText(CampaignHelper.statusText(campaign.calculatedStatus));
            status.setTextColor(ContextCompat.getColor(
                    this,
                    CampaignHelper.statusColor(campaign.calculatedStatus)));
            status.setBackgroundResource(CampaignHelper.statusBackground(campaign.calculatedStatus));

            item.setOnClickListener(view -> openCampaign(campaign));
            container.addView(item);
        }
    }

    private void openCampaign(Campaign campaign) {
        Intent intent = new Intent(this, CampaignDetailActivity.class);
        intent.putExtra(ExtraKeys.EXTRA_CAMPAIGN, campaign);
        intent.putExtra(ExtraKeys.EXTRA_REMOTE_CAMPAIGN_DETAIL, true);
        intent.putExtra(ExtraKeys.EXTRA_STANDARD_USER, false);
        intent.putExtra(ExtraKeys.EXTRA_USER, user);
        intent.putExtra(ExtraKeys.EXTRA_USER_ROLE, ExtraKeys.ROLE_ADMIN);
        startActivity(intent);
    }

    private void openAllCampaigns() {
        Intent intent = new Intent(this, AdminCampaignListActivity.class);
        intent.putExtra(ExtraKeys.EXTRA_USER, user);
        startActivity(intent);
    }


}

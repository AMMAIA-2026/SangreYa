package com.ammaia_ispc.sangreyamobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.ammaia_ispc.sangreyamobile.R;
import com.ammaia_ispc.sangreyamobile.data.MockAdminDashboardRepository;
import com.ammaia_ispc.sangreyamobile.model.AdminDashboardData;
import com.ammaia_ispc.sangreyamobile.model.Campaign;
import com.ammaia_ispc.sangreyamobile.model.DashboardCampaignStatus;
import com.ammaia_ispc.sangreyamobile.helpers.AdminDashboardHelper;
import com.ammaia_ispc.sangreyamobile.helpers.CampaignHelper;
import com.ammaia_ispc.sangreyamobile.helpers.DashboardChartView;
import com.ammaia_ispc.sangreyamobile.helpers.ExtraKeys;
import com.ammaia_ispc.sangreyamobile.helpers.NavigationDrawerHelper;
import com.google.android.material.navigation.NavigationView;

public class AdminDashboardActivity extends AppCompatActivity {
    private String user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CampaignHelper.configureSystemBars(this);
        user = getIntent().getStringExtra(ExtraKeys.EXTRA_USER);
        if (user == null) {
            user = AdminDashboardHelper.MOCK_ADMIN_EMAIL;
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
            startActivity(intent);
        });

        AdminDashboardData dashboard = MockAdminDashboardRepository.getDashboard();
        bindDonorsChart(dashboard);
        bindStatusChart(dashboard);
        bindRecentCampaigns(dashboard);
    }

    private void bindDonorsChart(AdminDashboardData dashboard) {
        int totalDonors = 0;
        for (int index = 0; index < dashboard.donantesPorMes.size(); index++) {
            totalDonors += dashboard.donantesPorMes.get(index).cantidad;
        }
        ((TextView) findViewById(R.id.dashboard_donors_total))
                .setText(getString(R.string.dashboard_donors_total, totalDonors));

        DashboardChartView chart = findViewById(R.id.dashboard_donors_chart);
        chart.setMonthlyDonors(dashboard.donantesPorMes);
    }

    private void bindStatusChart(AdminDashboardData dashboard) {
        DashboardChartView chart = findViewById(R.id.dashboard_status_chart);
        chart.setCampaignStatuses(dashboard.campaniasPorEstado);

        LinearLayout legend = findViewById(R.id.dashboard_status_legend);
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

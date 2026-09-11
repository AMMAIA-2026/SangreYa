package com.ammaia_ispc.sangreyamobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.ammaia_ispc.sangreyamobile.data.MockCampaignRepository;
import com.ammaia_ispc.sangreyamobile.model.Campaign;

import java.util.List;

public class CampaignListActivity extends AppCompatActivity {
    private List<Campaign> campaigns;
    private LinearLayout campaignContainer;
    private Button allFilter;
    private Button activeFilter;
    private Button upcomingFilter;
    private boolean standardUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CampaignHelper.configureSystemBars(this);
        campaigns = MockCampaignRepository.getCampaigns();
        standardUser = getIntent().getBooleanExtra(ExtraKeys.EXTRA_STANDARD_USER, true);
        setContentView(R.layout.activity_campaign_list);
        bindViews();
        configureFilters();
        showCampaigns("Todas");
    }

    private void bindViews() {
        campaignContainer = findViewById(R.id.campaign_container);
        allFilter = findViewById(R.id.filter_all);
        activeFilter = findViewById(R.id.filter_active);
        upcomingFilter = findViewById(R.id.filter_upcoming);
        Button aboutUsButton = findViewById(R.id.about_us_button);

        aboutUsButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, AboutUsActivity.class);
            startActivity(intent);
        });
        if (!standardUser) {
            findViewById(R.id.bottom_navigation).setVisibility(View.GONE);
        }
    }

    private void configureFilters() {
        allFilter.setOnClickListener(view -> showCampaigns("Todas"));
        activeFilter.setOnClickListener(view -> showCampaigns("Activas"));
        upcomingFilter.setOnClickListener(view -> showCampaigns("Próximas"));
    }

    private void showCampaigns(String filter) {
        campaignContainer.removeAllViews();
        for (Campaign campaign : campaigns) {
            if (filter.equals("Activas") && !campaign.calculatedStatus.equals("Activa")) {
                continue;
            }
            if (filter.equals("Próximas") && !campaign.calculatedStatus.equals("Proximamente")) {
                continue;
            }
            View card = createCampaignCard(campaign);
            LinearLayout.LayoutParams cardParams = (LinearLayout.LayoutParams) card.getLayoutParams();
            cardParams.setMargins(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.card_spacing));
            campaignContainer.addView(card, cardParams);
        }
        updateFilterStyles(filter);
    }

    private void updateFilterStyles(String selectedFilter) {
        styleFilter(allFilter, selectedFilter.equals("Todas"));
        styleFilter(activeFilter, selectedFilter.equals("Activas"));
        styleFilter(upcomingFilter, selectedFilter.equals("Próximas"));
    }

    private void styleFilter(Button filter, boolean selected) {
        filter.setTextColor(ContextCompat.getColor(this, selected ? R.color.white : R.color.secondary_text));
        filter.setBackgroundResource(selected ? R.drawable.bg_filter_selected : R.drawable.bg_filter_unselected);
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
        return card;
    }

    private void openDetails(Campaign campaign) {
        Intent intent = new Intent(this, CampaignDetailActivity.class);
        intent.putExtra(ExtraKeys.EXTRA_CAMPAIGN, campaign);
        intent.putExtra(ExtraKeys.EXTRA_STANDARD_USER, standardUser);
        startActivity(intent);
    }
}

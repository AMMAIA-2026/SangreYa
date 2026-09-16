package com.ammaia_ispc.sangreyamobile.activities;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
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
import com.ammaia_ispc.sangreyamobile.model.Campaign;
import com.ammaia_ispc.sangreyamobile.model.HealthCenter;
import com.google.android.material.navigation.NavigationView;

public class CampaignDetailActivity extends AppCompatActivity {
    private Campaign campaign;
    private boolean standardUser;
    private String user;
    private String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CampaignHelper.configureSystemBars(this);
        campaign = (Campaign) getIntent().getSerializableExtra(ExtraKeys.EXTRA_CAMPAIGN);
        if (campaign == null) {
            finish();
            return;
        }
        standardUser = getIntent().getBooleanExtra(ExtraKeys.EXTRA_STANDARD_USER, false);
        user = getIntent().getStringExtra(ExtraKeys.EXTRA_USER);
        role = getIntent().getStringExtra(ExtraKeys.EXTRA_USER_ROLE);
        setContentView(R.layout.activity_campaign_detail);
        bindViews();

        if (getIntent().getBooleanExtra(ExtraKeys.EXTRA_REMOTE_CAMPAIGN_DETAIL, false)) {
            loadCampaignDetails();
        }
    }

    private void loadCampaignDetails() {
        CampaignApiRepository.getCampaign(
                campaign.id,
                new CampaignApiRepository.Callback<Campaign>() {
                    @Override
                    public void onSuccess(Campaign value) {
                        campaign = value;
                        bindViews();
                    }

                    @Override
                    public void onError(Exception exception) {
                        Toast.makeText(
                                CampaignDetailActivity.this,
                                "No se pudo cargar el detalle de la campaña",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void bindViews() {
        DrawerLayout drawerLayout = findViewById(R.id.detail_root);
        NavigationView navigationView = findViewById(R.id.detail_navigation_view);
        NavigationDrawerHelper.configure(this, drawerLayout, navigationView, standardUser, user, role);

        View backButton = findViewById(R.id.detail_back_button);
        backButton.setVisibility(View.VISIBLE);
        backButton.setOnClickListener(view -> finish());

        TextView status = findViewById(R.id.detail_status);
        status.setText(CampaignHelper.statusText(campaign.calculatedStatus));
        status.setTextColor(ContextCompat.getColor(this, CampaignHelper.statusColor(campaign.calculatedStatus)));
        status.setBackgroundResource(CampaignHelper.statusBackground(campaign.calculatedStatus));

        ((TextView) findViewById(R.id.detail_title)).setText(campaign.title);
        ((TextView) findViewById(R.id.detail_location)).setText(campaign.location);
        ((TextView) findViewById(R.id.detail_dates)).setText(CampaignHelper.formatLongDate(campaign.startDate, campaign.endDate));
        ((TextView) findViewById(R.id.detail_description)).setText(campaign.description);
        ((TextView) findViewById(R.id.detail_registered)).setText(getString(R.string.registered_detail, campaign.totalRegistered));

        TextView capacity = findViewById(R.id.detail_capacity);
        if (campaign.maximumCapacity == null) {
            capacity.setVisibility(View.GONE);
        } else {
            capacity.setText(getString(R.string.capacity, campaign.maximumCapacity));
        }

        bindHealthCenter();
        bindEnrollmentAction();
    }

    private void bindHealthCenter() {
        View centerSection = findViewById(R.id.center_section);
        HealthCenter center = campaign.healthCenter;
        if (center == null) {
            centerSection.setVisibility(View.GONE);
            return;
        }
        ((TextView) findViewById(R.id.center_name)).setText(center.name);
        ((TextView) findViewById(R.id.center_address)).setText(getString(R.string.address, center.address));
        ((TextView) findViewById(R.id.center_neighborhood)).setText(getString(R.string.neighborhood_city, center.neighborhood, center.city));
        ((TextView) findViewById(R.id.center_phone)).setText(getString(R.string.phone, center.phone));
        ((TextView) findViewById(R.id.center_website)).setText(getString(R.string.website, center.website));
        ((TextView) findViewById(R.id.center_coordinates)).setText(getString(R.string.coordinates, center.latitude, center.longitude));
    }

    private void bindEnrollmentAction() {
        if (campaign.calculatedStatus.equals("Finalizada") || ExtraKeys.ROLE_ADMIN.equals(role)) {
            return;
        }

        View actionArea = findViewById(R.id.action_area);
        Button enrollButton = findViewById(R.id.enroll_button);
        actionArea.setVisibility(View.VISIBLE);
        View.OnClickListener enrollmentListener = view -> showEnrollmentMessage();
        actionArea.setOnClickListener(enrollmentListener);
        enrollButton.setOnClickListener(enrollmentListener);
    }

    private void showEnrollmentMessage() {
        int messageRes = standardUser
                ? R.string.offline_enrollment_message
                : R.string.login_required_enrollment;
        View toastView = getLayoutInflater().inflate(R.layout.toast_enrollment, null);
        ((TextView) toastView.findViewById(R.id.toast_message))
                .setText(messageRes);

        Toast toast = Toast.makeText(
                this,
                messageRes,
                Toast.LENGTH_LONG);
        toast.setGravity(
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL,
                0,
                getResources().getDimensionPixelSize(R.dimen.toast_bottom_offset));
        toast.setView(toastView);
        toast.show();
    }
}

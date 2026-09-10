package com.ammaia_ispc.sangreyamobile;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.ammaia_ispc.sangreyamobile.model.Campaign;
import com.ammaia_ispc.sangreyamobile.model.HealthCenter;

public class CampaignDetailActivity extends AppCompatActivity {
    private Campaign campaign;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CampaignHelper.configureSystemBars(this);
        campaign = (Campaign) getIntent().getSerializableExtra(ExtraKeys.EXTRA_CAMPAIGN);
        if (campaign == null) {
            finish();
            return;
        }
        setContentView(R.layout.activity_campaign_detail);
        bindViews();
    }

    private void bindViews() {
        findViewById(R.id.back_button).setOnClickListener(view -> finish());

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
        boolean standardUser = getIntent().getBooleanExtra(ExtraKeys.EXTRA_STANDARD_USER, false);
        if (!standardUser || campaign.calculatedStatus.equals("Finalizada")) {
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
        View toastView = getLayoutInflater().inflate(R.layout.toast_enrollment, null);
        ((TextView) toastView.findViewById(R.id.toast_message))
                .setText(R.string.offline_enrollment_message);

        Toast toast = Toast.makeText(
                this,
                R.string.offline_enrollment_message,
                Toast.LENGTH_LONG);
        toast.setGravity(
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL,
                0,
                getResources().getDimensionPixelSize(R.dimen.toast_bottom_offset));
        toast.setView(toastView);
        toast.show();
    }
}

package com.ammaia_ispc.sangreyamobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.ammaia_ispc.sangreyamobile.helpers.SessionManager;
import com.ammaia_ispc.sangreyamobile.model.Campaign;
import com.ammaia_ispc.sangreyamobile.model.HealthCenter;
import com.google.android.material.navigation.NavigationView;

public class CampaignDetailActivity extends AppCompatActivity {
    private Campaign campaign;
    private boolean standardUser;
    private String user;
    private String role;
    private Button enrollButton;
    private Button adminEnrollmentsButton;
    private View actionArea;
    private boolean enrollmentInProgress;

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
        if (TextUtils.isEmpty(user)) {
            user = SessionManager.getUserName(this);
        }
        if (!standardUser && !TextUtils.isEmpty(user) && !SessionManager.isAdmin(this)) {
            standardUser = true;
        }
        setContentView(R.layout.activity_campaign_detail);
        bindViews();

        if (getIntent().getBooleanExtra(ExtraKeys.EXTRA_REMOTE_CAMPAIGN_DETAIL, false)) {
            loadCampaignDetails();
        }
    }

    private void loadCampaignDetails() {
        CampaignApiRepository.getCampaign(
                campaign.id,
                ExtraKeys.ROLE_ADMIN.equals(role)
                        ? SessionManager.getAccessToken(this)
                        : null,
                new CampaignApiRepository.Callback<Campaign>() {
                    @Override
                    public void onSuccess(Campaign value) {
                        campaign = value;
                        bindViews();
                    }

                    @Override
                    public void onError(Exception exception) {
                        int messageRes = CampaignApiRepository.isNotFound(exception)
                                ? R.string.campaign_not_found_error
                                : CampaignApiRepository.isUnauthorized(exception)
                                ? R.string.campaign_unauthorized_error
                                : CampaignApiRepository.isNetworkError(exception)
                                ? R.string.campaign_network_error
                                : R.string.campaign_detail_error;
                        Toast.makeText(
                                CampaignDetailActivity.this,
                                messageRes,
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
            capacity.setText(getString(R.string.maximum_capacity, campaign.maximumCapacity));
        }

        bindHealthCenter();
        bindEnrollmentAction();
        bindAdminEnrollmentsAction();
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
        actionArea = findViewById(R.id.action_area);
        enrollButton = findViewById(R.id.enroll_button);
        actionArea.setVisibility(View.GONE);
        boolean adminUser = ExtraKeys.ROLE_ADMIN.equals(role) || SessionManager.isAdmin(this);
        if ("Finalizada".equals(campaign.calculatedStatus) || adminUser) {
            return;
        }

        actionArea.setVisibility(View.VISIBLE);
        View.OnClickListener enrollmentListener = view -> {
            if (standardUser) {
                enroll();
            } else {
                showEnrollmentMessage();
            }
        };
        actionArea.setOnClickListener(enrollmentListener);
        enrollButton.setOnClickListener(enrollmentListener);
    }

    private void bindAdminEnrollmentsAction() {
        adminEnrollmentsButton = findViewById(R.id.admin_enrollments_button);
        boolean adminUser = ExtraKeys.ROLE_ADMIN.equals(role) || SessionManager.isAdmin(this);
        adminEnrollmentsButton.setVisibility(adminUser ? View.VISIBLE : View.GONE);
        if (adminUser) {
            adminEnrollmentsButton.setOnClickListener(view -> {
                Intent intent = new Intent(this, AdminCampaignEnrollmentsActivity.class);
                intent.putExtra(ExtraKeys.EXTRA_CAMPAIGN, campaign);
                startActivity(intent);
            });
        }
    }

    private void enroll() {
        if (enrollmentInProgress) {
            return;
        }

        String accessToken = SessionManager.getAccessToken(this);
        if (TextUtils.isEmpty(accessToken)) {
            showEnrollmentMessage();
            return;
        }

        setEnrollmentInProgress(true);
        CampaignApiRepository.enrollInCampaign(
                campaign.id,
                accessToken,
                new CampaignApiRepository.Callback<Integer>() {
                    @Override
                    public void onSuccess(Integer totalInscriptos) {
                        setEnrollmentInProgress(false);
                        ((TextView) findViewById(R.id.detail_registered)).setText(
                                getString(R.string.registered_detail, totalInscriptos));
                        showEnrollmentToast(
                                getString(R.string.enrollment_success, totalInscriptos));
                        loadCampaignDetails();
                    }

                    @Override
                    public void onError(Exception exception) {
                        setEnrollmentInProgress(false);
                        showEnrollmentToast(enrollmentErrorMessage(exception));
                    }
                });
    }

    private void setEnrollmentInProgress(boolean inProgress) {
        enrollmentInProgress = inProgress;
        if (actionArea != null) {
            actionArea.setEnabled(!inProgress);
        }
        if (enrollButton != null) {
            enrollButton.setEnabled(!inProgress);
            enrollButton.setText(inProgress ? R.string.enrollment_loading : R.string.enroll);
        }
    }

    private int enrollmentErrorMessage(Exception exception) {
        String code = CampaignApiRepository.getErrorCode(exception);
        if ("edad_no_permitida".equals(code)) {
            return R.string.enrollment_age_error;
        }
        if ("inscripcion_duplicada".equals(code)) {
            return R.string.enrollment_duplicate_error;
        }
        if ("cupo_completo".equals(code)) {
            return R.string.enrollment_capacity_error;
        }
        if ("campania_finalizada".equals(code)) {
            return R.string.enrollment_finished_error;
        }
        if (exception instanceof CampaignApiRepository.HttpException) {
            int statusCode = ((CampaignApiRepository.HttpException) exception).getStatusCode();
            if (statusCode == 403) {
                return R.string.enrollment_forbidden_error;
            }
            if (statusCode == 401) {
                return R.string.session_expired_message;
            }
            if (statusCode == 404) {
                return R.string.campaign_not_found_error;
            }
        }
        return R.string.enrollment_network_error;
    }

    private void showEnrollmentMessage() {
        showEnrollmentToast(R.string.login_required_enrollment);
    }

    private void showEnrollmentToast(int messageRes) {
        showEnrollmentToast(getString(messageRes));
    }

    private void showEnrollmentToast(String message) {
        View toastView = getLayoutInflater().inflate(R.layout.toast_enrollment, null);
        ((TextView) toastView.findViewById(R.id.toast_message))
                .setText(message);

        Toast toast = Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG);
        toast.setGravity(
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL,
                0,
                getResources().getDimensionPixelSize(R.dimen.toast_bottom_offset));
        toast.setView(toastView);
        toast.show();
    }
}

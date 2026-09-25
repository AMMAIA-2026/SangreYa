package com.ammaia_ispc.sangreyamobile.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
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
import com.ammaia_ispc.sangreyamobile.model.Enrollment;
import com.ammaia_ispc.sangreyamobile.model.MyEnrollments;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class EnrollmentsActivity extends AppCompatActivity {
    private final List<Enrollment> currentEnrollments = new ArrayList<>();
    private final List<Enrollment> historicalEnrollments = new ArrayList<>();

    private LinearLayout enrollmentsContainer;
    private ProgressBar loadingIndicator;
    private ScrollView enrollmentsScroll;
    private TextView emptyView;
    private TextView messageView;
    private MaterialButton currentFilter;
    private MaterialButton historyFilter;
    private boolean showingHistory;
    private boolean cancellationInProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CampaignHelper.configureSystemBars(this);

        if (SessionManager.isAdmin(this)
                || TextUtils.isEmpty(SessionManager.getAccessToken(this))) {
            finish();
            return;
        }

        setContentView(R.layout.activity_enrollments);
        bindViews();
        configureNavigation();
        configureFilters();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (enrollmentsContainer != null) {
            loadEnrollments();
        }
    }

    private void bindViews() {
        enrollmentsContainer = findViewById(R.id.enrollments_container);
        loadingIndicator = findViewById(R.id.enrollments_loading);
        enrollmentsScroll = findViewById(R.id.enrollments_scroll);
        emptyView = findViewById(R.id.enrollments_empty);
        messageView = findViewById(R.id.enrollments_message);
        currentFilter = findViewById(R.id.enrollments_current_filter);
        historyFilter = findViewById(R.id.enrollments_history_filter);
    }

    private void configureNavigation() {
        DrawerLayout drawerLayout = findViewById(R.id.enrollments_drawer);
        NavigationView navigationView = findViewById(R.id.enrollments_navigation_view);
        NavigationDrawerHelper.configure(
                this,
                drawerLayout,
                navigationView,
                true,
                SessionManager.getUserName(this),
                SessionManager.getUserRole(this));

        findViewById(R.id.enrollments_campaigns_navigation_item)
                .setOnClickListener(view -> {
                    Intent intent = new Intent(this, CampaignListActivity.class);
                    intent.putExtra(ExtraKeys.EXTRA_STANDARD_USER, true);
                    intent.putExtra(ExtraKeys.EXTRA_USER, SessionManager.getUserName(this));
                    intent.putExtra(ExtraKeys.EXTRA_USER_ROLE, SessionManager.getUserRole(this));
                    startActivity(intent);
                    finish();
                });

        findViewById(R.id.enrollments_profile_navigation_item)
                .setOnClickListener(view -> {
                    startActivity(new Intent(this, ProfileActivity.class));
                    finish();
                });
    }

    private void configureFilters() {
        currentFilter.setOnClickListener(view -> {
            showingHistory = false;
            showEnrollments();
        });
        historyFilter.setOnClickListener(view -> {
            showingHistory = true;
            showEnrollments();
        });
    }

    private void loadEnrollments() {
        clearMessage();
        setLoading(true);
        CampaignApiRepository.getMyEnrollments(
                SessionManager.getAccessToken(this),
                new CampaignApiRepository.Callback<MyEnrollments>() {
                    @Override
                    public void onSuccess(MyEnrollments value) {
                        currentEnrollments.clear();
                        historicalEnrollments.clear();
                        currentEnrollments.addAll(value.current);
                        historicalEnrollments.addAll(value.historical);
                        showEnrollments();
                        setLoading(false);
                    }

                    @Override
                    public void onError(Exception exception) {
                        setLoading(false);
                        if (isSessionError(exception)) {
                            SessionManager.expireSession(EnrollmentsActivity.this);
                            return;
                        }
                        showMessage(R.string.enrollments_load_error);
                    }
                });
    }

    private void showEnrollments() {
        List<Enrollment> enrollments = showingHistory
                ? historicalEnrollments
                : currentEnrollments;

        enrollmentsContainer.removeAllViews();
        emptyView.setVisibility(enrollments.isEmpty() ? View.VISIBLE : View.GONE);
        emptyView.setText(showingHistory
                ? R.string.enrollments_empty_history
                : R.string.enrollments_empty_current);

        for (Enrollment enrollment : enrollments) {
            if (enrollment.campaign != null) {
                enrollmentsContainer.addView(createEnrollmentCard(enrollment));
            }
        }

        updateFilterStyles();
    }

    private View createEnrollmentCard(Enrollment enrollment) {
        View card = getLayoutInflater().inflate(
                R.layout.item_enrollment,
                enrollmentsContainer,
                false);
        Campaign campaign = enrollment.campaign;

        TextView status = card.findViewById(R.id.enrollment_campaign_status);
        status.setText(CampaignHelper.statusText(campaign.calculatedStatus));
        status.setTextColor(ContextCompat.getColor(
                this,
                CampaignHelper.statusColor(campaign.calculatedStatus)));
        status.setBackgroundResource(CampaignHelper.statusBackground(campaign.calculatedStatus));

        ((TextView) card.findViewById(R.id.enrollment_campaign_title))
                .setText(campaign.title);
        ((TextView) card.findViewById(R.id.enrollment_campaign_location))
                .setText(campaign.location);
        ((TextView) card.findViewById(R.id.enrollment_campaign_dates))
                .setText(CampaignHelper.formatShortDate(campaign.startDate, campaign.endDate));

        Button cancelButton = card.findViewById(R.id.enrollment_cancel);
        cancelButton.setVisibility(showingHistory ? View.GONE : View.VISIBLE);
        cancelButton.setOnClickListener(view -> confirmCancellation(enrollment));
        card.setOnClickListener(view -> openCampaignDetails(campaign));

        LinearLayout.LayoutParams cardParams = (LinearLayout.LayoutParams) card.getLayoutParams();
        cardParams.setMargins(
                0,
                0,
                0,
                getResources().getDimensionPixelSize(R.dimen.card_spacing));
        card.setLayoutParams(cardParams);
        return card;
    }

    private void confirmCancellation(Enrollment enrollment) {
        if (cancellationInProgress || enrollment.campaign == null) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.cancel_enrollment_title)
                .setMessage(getString(
                        R.string.cancel_enrollment_message,
                        enrollment.campaign.title))
                .setNegativeButton(R.string.dialog_cancel, null)
                .setPositiveButton(
                        R.string.cancel_enrollment_confirm,
                        (dialog, which) -> cancelEnrollment(enrollment))
                .show();
    }

    private void cancelEnrollment(Enrollment enrollment) {
        if (cancellationInProgress) {
            return;
        }

        cancellationInProgress = true;
        CampaignApiRepository.cancelEnrollment(
                enrollment.id,
                SessionManager.getAccessToken(this),
                new CampaignApiRepository.Callback<Void>() {
                    @Override
                    public void onSuccess(Void value) {
                        cancellationInProgress = false;
                        currentEnrollments.remove(enrollment);
                        showEnrollments();
                        Toast.makeText(
                                EnrollmentsActivity.this,
                                R.string.cancel_enrollment_success,
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(Exception exception) {
                        cancellationInProgress = false;
                        if (isSessionError(exception)) {
                            SessionManager.expireSession(EnrollmentsActivity.this);
                            return;
                        }
                        String code = CampaignApiRepository.getErrorCode(exception);
                        Toast.makeText(
                                EnrollmentsActivity.this,
                                "campania_finalizada".equals(code)
                                        ? R.string.cancel_enrollment_finished
                                        : R.string.cancel_enrollment_error,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void openCampaignDetails(Campaign campaign) {
        Intent intent = new Intent(this, CampaignDetailActivity.class);
        intent.putExtra(ExtraKeys.EXTRA_CAMPAIGN, campaign);
        intent.putExtra(ExtraKeys.EXTRA_REMOTE_CAMPAIGN_DETAIL, true);
        intent.putExtra(ExtraKeys.EXTRA_STANDARD_USER, true);
        intent.putExtra(ExtraKeys.EXTRA_USER, SessionManager.getUserName(this));
        intent.putExtra(ExtraKeys.EXTRA_USER_ROLE, SessionManager.getUserRole(this));
        startActivity(intent);
    }

    private void updateFilterStyles() {
        styleFilter(currentFilter, !showingHistory);
        styleFilter(historyFilter, showingHistory);
    }

    private void styleFilter(MaterialButton filter, boolean selected) {
        filter.setTextColor(ContextCompat.getColor(
                this,
                selected ? R.color.white : R.color.secondary_text));
        filter.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(
                this,
                selected ? R.color.primary_red : R.color.surface)));
    }

    private void setLoading(boolean loading) {
        loadingIndicator.setVisibility(loading ? View.VISIBLE : View.GONE);
        enrollmentsScroll.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
    }

    private void showMessage(int messageResId) {
        messageView.setText(messageResId);
        messageView.setVisibility(View.VISIBLE);
    }

    private void clearMessage() {
        messageView.setText("");
        messageView.setVisibility(View.GONE);
    }

    private boolean isSessionError(Exception exception) {
        return exception instanceof CampaignApiRepository.HttpException
                && ((CampaignApiRepository.HttpException) exception).getStatusCode() == 401;
    }
}

package com.ammaia_ispc.sangreyamobile.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.ammaia_ispc.sangreyamobile.R;
import com.ammaia_ispc.sangreyamobile.data.CampaignApiRepository;
import com.ammaia_ispc.sangreyamobile.helpers.CampaignHelper;
import com.ammaia_ispc.sangreyamobile.helpers.ExtraKeys;
import com.ammaia_ispc.sangreyamobile.helpers.NavigationDrawerHelper;
import com.ammaia_ispc.sangreyamobile.helpers.SessionManager;
import com.ammaia_ispc.sangreyamobile.helpers.UiHelper;
import com.ammaia_ispc.sangreyamobile.model.Campaign;
import com.ammaia_ispc.sangreyamobile.model.CampaignEnrollments;
import com.ammaia_ispc.sangreyamobile.model.EnrollmentUser;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class AdminCampaignEnrollmentsActivity extends AppCompatActivity {
    private Campaign campaign;
    private LinearLayout usersContainer;
    private ProgressBar loadingIndicator;
    private ScrollView usersScroll;
    private TextView messageView;
    private TextView emptyView;
    private TextView campaignView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!SessionManager.requireAdmin(this)) {
            return;
        }

        campaign = (Campaign) getIntent().getSerializableExtra(ExtraKeys.EXTRA_CAMPAIGN);
        if (campaign == null) {
            finish();
            return;
        }

        UiHelper.configureSystemBars(this);
        setContentView(R.layout.activity_admin_campaign_enrollments);
        bindViews();
        configureNavigation();
        showCampaign();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (usersContainer != null) {
            loadUsers();
        }
    }

    private void bindViews() {
        usersContainer = findViewById(R.id.admin_enrollments_container);
        loadingIndicator = findViewById(R.id.admin_enrollments_loading);
        usersScroll = findViewById(R.id.admin_enrollments_scroll);
        messageView = findViewById(R.id.admin_enrollments_message);
        emptyView = findViewById(R.id.admin_enrollments_empty);
        campaignView = findViewById(R.id.admin_enrollments_campaign);
    }

    private void configureNavigation() {
        DrawerLayout drawerLayout = findViewById(R.id.admin_enrollments_drawer);
        NavigationView navigationView = findViewById(R.id.admin_enrollments_navigation_view);
        NavigationDrawerHelper.configure(
                this,
                drawerLayout,
                navigationView);
    }

    private void showCampaign() {
        campaignView.setText(getString(
                R.string.admin_enrollments_campaign_name,
                campaign.title));
    }

    private void loadUsers() {
        UiHelper.clearMessage(messageView);
        UiHelper.setLoading(loadingIndicator, usersScroll, true);
        CampaignApiRepository.getCampaignEnrollments(
                this,
                campaign.id,
                new CampaignApiRepository.Callback<CampaignEnrollments>() {
                    @Override
                    public void onSuccess(CampaignEnrollments value) {
                        if (value.campaign != null) {
                            campaign = value.campaign;
                            showCampaign();
                        }
                        showUsers(value.users);
                        UiHelper.setLoading(loadingIndicator, usersScroll, false);
                    }

                    @Override
                    public void onError(Exception exception) {
                        UiHelper.setLoading(loadingIndicator, usersScroll, false);
                        if (exception instanceof CampaignApiRepository.HttpException
                                && ((CampaignApiRepository.HttpException) exception)
                                .getStatusCode() == 401) {
                            SessionManager.expireSession(AdminCampaignEnrollmentsActivity.this);
                            return;
                        }
                        if (exception instanceof CampaignApiRepository.HttpException
                                && ((CampaignApiRepository.HttpException) exception)
                                .getStatusCode() == 403) {
                            UiHelper.showMessage(
                                    messageView,
                                    R.string.admin_enrollments_unauthorized);
                            return;
                        }
                        UiHelper.showMessage(messageView, R.string.admin_enrollments_error);
                    }
                });
    }

    private void showUsers(List<EnrollmentUser> users) {
        usersContainer.removeAllViews();
        boolean empty = users == null || users.isEmpty();
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        if (empty) {
            return;
        }

        List<EnrollmentUser> orderedUsers = new ArrayList<>(users);
        orderedUsers.sort(Comparator
                .comparing((EnrollmentUser user) -> UiHelper.normalized(user.lastName, Locale.getDefault()))
                .thenComparing(user -> UiHelper.normalized(user.firstName, Locale.getDefault())));

        for (EnrollmentUser user : orderedUsers) {
            View row = getLayoutInflater().inflate(
                    R.layout.item_admin_enrollment_user,
                    usersContainer,
                    false);
            ((TextView) row.findViewById(R.id.admin_enrollment_user_name))
                    .setText(getString(
                            R.string.admin_enrollments_user_name,
                            user.lastName,
                            user.firstName));

            LinearLayout.LayoutParams rowParams = (LinearLayout.LayoutParams) row.getLayoutParams();
            rowParams.setMargins(
                    0,
                    0,
                    0,
                    getResources().getDimensionPixelSize(R.dimen.card_spacing));
            usersContainer.addView(row, rowParams);
        }
    }

}

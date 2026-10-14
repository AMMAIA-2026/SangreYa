package com.ammaia_ispc.sangreyamobile.screens;

import com.ammaia_ispc.sangreyamobile.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public final class AdminDashboardScreen {
    public boolean isOpen() {
        return LoginScreen.isShown(R.id.admin_dashboard_drawer);
    }

    public AdminCampaignListScreen openCampaigns() {
        onView(withId(R.id.admin_campaigns_navigation)).perform(click());
        return new AdminCampaignListScreen();
    }
}

package com.ammaia_ispc.sangreyamobile.screens;

import com.ammaia_ispc.sangreyamobile.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

public final class CampaignListScreen {
    public boolean isStandardAreaOpen() {
        return LoginScreen.isShown(R.id.campaign_drawer)
                && LoginScreen.isShown(R.id.enrollments_navigation_item);
    }

    public CampaignDetailScreen openCampaign(String title) {
        onView(withId(R.id.campaign_search)).perform(replaceText(title), closeSoftKeyboard());
        onView(allOf(withId(R.id.campaign_title), withText(title))).perform(scrollTo(), click());
        return new CampaignDetailScreen();
    }

    public MyEnrollmentsScreen openMyEnrollments() {
        onView(withId(R.id.enrollments_navigation_item)).perform(click());
        return new MyEnrollmentsScreen();
    }
}

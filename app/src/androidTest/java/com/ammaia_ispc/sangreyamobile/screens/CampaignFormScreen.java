package com.ammaia_ispc.sangreyamobile.screens;

import com.ammaia_ispc.sangreyamobile.R;
import com.ammaia_ispc.sangreyamobile.helpers.DateHelper;
import com.ammaia_ispc.sangreyamobile.support.TestFixtures.CampaignData;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public final class CampaignFormScreen {
    public void complete(CampaignData data) {
        fill(R.id.create_campaign_name, data.title);
        fill(R.id.create_campaign_address, data.location);
        fill(R.id.create_campaign_start_date, DateHelper.toDisplayDate(data.startDate));
        fill(R.id.create_campaign_end_date, DateHelper.toDisplayDate(data.endDate));
        fill(R.id.create_campaign_capacity, String.valueOf(data.capacity));
        fill(R.id.create_campaign_description, data.description);
    }

    public AdminCampaignListScreen save() {
        onView(withId(R.id.create_campaign_publish)).perform(scrollTo(), click());
        return new AdminCampaignListScreen();
    }

    public void requestDeletion() {
        onView(withId(R.id.create_campaign_delete)).perform(scrollTo(), click());
    }

    public void cancelDeletion() {
        onView(withId(android.R.id.button2)).perform(click());
    }

    public AdminCampaignListScreen returnToList() {
        onView(withId(R.id.create_campaign_back)).perform(click());
        return new AdminCampaignListScreen();
    }

    private void fill(int id, String value) {
        onView(withId(id)).perform(scrollTo(), replaceText(value), closeSoftKeyboard());
    }
}

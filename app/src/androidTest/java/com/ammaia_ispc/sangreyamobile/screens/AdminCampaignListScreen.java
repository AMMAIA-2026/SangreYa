package com.ammaia_ispc.sangreyamobile.screens;

import com.ammaia_ispc.sangreyamobile.R;

import java.util.concurrent.atomic.AtomicBoolean;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

public final class AdminCampaignListScreen {
    public void search(String title) {
        onView(withId(R.id.admin_campaign_search)).perform(replaceText(title), closeSoftKeyboard());
    }

    public boolean contains(String title) {
        AtomicBoolean result = new AtomicBoolean();
        onView(allOf(withId(R.id.admin_campaign_title), withText(title))).check((view, exception) -> {
            result.set(exception == null);
        });
        return result.get();
    }

    public CampaignFormScreen openCreation() {
        onView(withId(R.id.admin_campaign_add)).perform(click());
        return new CampaignFormScreen();
    }

    public CampaignDetailScreen openDetail(String title) {
        onView(allOf(withId(R.id.admin_campaign_title), withText(title))).perform(scrollTo(), click());
        return new CampaignDetailScreen();
    }

    public CampaignFormScreen openEdit(String title) {
        onView(allOf(withId(R.id.admin_campaign_edit), isDescendantOfA(
                allOf(withId(R.id.admin_campaign_card), hasDescendant(withText(title))))))
                .perform(scrollTo(), click());
        return new CampaignFormScreen();
    }

    public void requestDeletion(String title) {
        onView(allOf(withId(R.id.admin_campaign_delete), isDescendantOfA(
                allOf(withId(R.id.admin_campaign_card), hasDescendant(withText(title))))))
                .perform(scrollTo(), click());
    }

    public void cancelDeletion() {
        onView(withId(android.R.id.button2)).perform(click());
    }

    public void confirmDeletion() {
        onView(withId(android.R.id.button1)).perform(click());
    }
}

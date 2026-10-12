package com.ammaia_ispc.sangreyamobile.screens;

import android.widget.LinearLayout;
import android.widget.TextView;

import com.ammaia_ispc.sangreyamobile.R;

import java.util.concurrent.atomic.AtomicInteger;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

public final class MyEnrollmentsScreen {
    public int entriesFor(String title) {
        AtomicInteger count = new AtomicInteger();
        onView(withId(R.id.enrollments_container)).check((view, exception) -> {
            if (exception != null) {
                throw exception;
            }
            LinearLayout container = (LinearLayout) view;
            for (int index = 0; index < container.getChildCount(); index++) {
                TextView name = container.getChildAt(index).findViewById(R.id.enrollment_campaign_title);
                if (name != null && title.contentEquals(name.getText())) {
                    count.incrementAndGet();
                }
            }
        });
        return count.get();
    }

    public CampaignDetailScreen openCampaign(String title) {
        onView(allOf(withId(R.id.enrollment_campaign_title), withText(title)))
                .perform(scrollTo(), click());
        return new CampaignDetailScreen();
    }

    public CampaignListScreen returnToCampaigns() {
        onView(withId(R.id.enrollments_campaigns_navigation_item)).perform(click());
        return new CampaignListScreen();
    }
}

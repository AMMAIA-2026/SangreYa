package com.ammaia_ispc.sangreyamobile;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ammaia_ispc.sangreyamobile.activities.AdminContactListActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * TC-ACC-01 (parte 2) - Bandeja administrativa de mensajes de contacto.
 */
@RunWith(AndroidJUnit4.class)
public class AdminContactListAccessibilityTest extends BaseAccessibilityTest {

    @Before
    public void setUpAdminSession() {
        TestSession.signInAsAdmin();
    }

    @After
    public void tearDownSession() {
        TestSession.clearSession();
    }

    @Test
    public void adminInbox_allFilter() {
        try (ActivityScenario<AdminContactListActivity> scenario =
                     ActivityScenario.launch(AdminContactListActivity.class)) {
            onView(withId(R.id.admin_contact_filter_all)).perform(click());
        }
    }

    @Test
    public void adminInbox_unansweredFilter() {
        try (ActivityScenario<AdminContactListActivity> scenario =
                     ActivityScenario.launch(AdminContactListActivity.class)) {
            onView(withId(R.id.admin_contact_filter_unanswered)).perform(click());
        }
    }
}

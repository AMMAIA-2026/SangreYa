package com.ammaia_ispc.sangreyamobile;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ammaia_ispc.sangreyamobile.activities.AdminDashboardActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * TC-ACC-07 - Dashboard administrativo.
 * Requiere sesion de admin, que se simula con TestSession (no hay backend en el test).
 */
@RunWith(AndroidJUnit4.class)
public class DashboardAccessibilityTest extends BaseAccessibilityTest {

    @Before
    public void setUpAdminSession() {
        TestSession.signInAsAdmin();
    }

    @After
    public void tearDownSession() {
        TestSession.clearSession();
    }

    @Test
    public void dashboard_screen() {
        try (ActivityScenario<AdminDashboardActivity> scenario =
                     ActivityScenario.launch(AdminDashboardActivity.class)) {
            onView(withId(R.id.admin_role_badge)).perform(click());
        }
    }
}

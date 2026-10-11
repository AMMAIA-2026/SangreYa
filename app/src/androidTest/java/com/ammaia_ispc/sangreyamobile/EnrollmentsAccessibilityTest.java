package com.ammaia_ispc.sangreyamobile;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ammaia_ispc.sangreyamobile.activities.EnrollmentsActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * TC-ACC-07 - Inscripciones del usuario.
 * Requiere sesion de usuario comun, que se simula con TestSession (no hay backend en el test).
 */
@RunWith(AndroidJUnit4.class)
public class EnrollmentsAccessibilityTest extends BaseAccessibilityTest {

    @Before
    public void setUpUserSession() {
        TestSession.signInAsUser();
    }

    @After
    public void tearDownSession() {
        TestSession.clearSession();
    }

    @Test
    public void enrollments_currentFilter() {
        try (ActivityScenario<EnrollmentsActivity> scenario =
                     ActivityScenario.launch(EnrollmentsActivity.class)) {
            onView(withId(R.id.enrollments_current_filter)).perform(click());
        }
    }
}

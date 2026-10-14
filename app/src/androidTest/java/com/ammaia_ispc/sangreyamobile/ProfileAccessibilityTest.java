package com.ammaia_ispc.sangreyamobile;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ammaia_ispc.sangreyamobile.activities.ProfileActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * TC-ACC-07 - Perfil propio del usuario.
 * Requiere sesion de usuario comun, que se simula con TestSession (no hay backend en el test).
 */
@RunWith(AndroidJUnit4.class)
public class ProfileAccessibilityTest extends BaseAccessibilityTest {

    @Before
    public void setUpUserSession() {
        TestSession.signInAsUser();
    }

    @After
    public void tearDownSession() {
        TestSession.clearSession();
    }

    @Test
    public void profile_ownProfile() {
        try (ActivityScenario<ProfileActivity> scenario =
                     ActivityScenario.launch(ProfileActivity.class)) {
            onView(withId(R.id.profile_username)).perform(click());
        }
    }
}

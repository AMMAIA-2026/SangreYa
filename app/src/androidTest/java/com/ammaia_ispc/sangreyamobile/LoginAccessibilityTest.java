package com.ammaia_ispc.sangreyamobile;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ammaia_ispc.sangreyamobile.activities.LoginActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * TC-ACC-07 - Autenticacion: pantalla de Login (publica, no requiere sesion).
 */
@RunWith(AndroidJUnit4.class)
public class LoginAccessibilityTest extends BaseAccessibilityTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> rule =
            new ActivityScenarioRule<>(LoginActivity.class);

    @Test
    public void login_initialState() {
        onView(withId(R.id.etEmail)).perform(click());
    }
}

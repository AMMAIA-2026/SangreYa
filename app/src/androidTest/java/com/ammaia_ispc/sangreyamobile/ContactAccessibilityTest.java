package com.ammaia_ispc.sangreyamobile;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ammaia_ispc.sangreyamobile.activities.ContactActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * TC-ACC-01 (parte 1) - Formulario publico de Contacto.
 */
@RunWith(AndroidJUnit4.class)
public class ContactAccessibilityTest extends BaseAccessibilityTest {

    @Rule
    public ActivityScenarioRule<ContactActivity> rule =
            new ActivityScenarioRule<>(ContactActivity.class);

    @Test
    public void contactForm_initialState() {
        onView(withId(R.id.etNombre)).perform(click());
    }

    @Test
    public void contactForm_withVisibleErrors() {
        onView(withId(R.id.btnEnviar)).perform(closeSoftKeyboard(), click());
        onView(withId(R.id.etNombre)).perform(click());
    }
}

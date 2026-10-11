package com.ammaia_ispc.sangreyamobile;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ammaia_ispc.sangreyamobile.activities.AboutUsActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * TC-ACC-07 - Contenido institucional: pantalla "Nosotros" (publica, no requiere sesion).
 * El chequeo de accesibilidad corre antes de la accion, por eso el clic en
 * "Contactanos" (que navega a Contacto) no afecta al resultado.
 */
@RunWith(AndroidJUnit4.class)
public class AboutUsAccessibilityTest extends BaseAccessibilityTest {

    @Rule
    public ActivityScenarioRule<AboutUsActivity> rule =
            new ActivityScenarioRule<>(AboutUsActivity.class);

    @Test
    public void aboutUs_screen() {
        onView(withId(R.id.tvContactanos)).perform(click());
    }
}

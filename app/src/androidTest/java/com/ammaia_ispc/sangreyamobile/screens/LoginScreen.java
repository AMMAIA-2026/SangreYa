package com.ammaia_ispc.sangreyamobile.screens;

import android.os.SystemClock;
import android.view.View;
import android.widget.TextView;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;

import org.hamcrest.Matcher;

import com.ammaia_ispc.sangreyamobile.R;
import com.ammaia_ispc.sangreyamobile.support.TestFixtures.Account;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.onIdle;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;

public final class LoginScreen {
    public boolean isOpen() {
        return isShown(R.id.etEmail);
    }

    public RegisterScreen openRegistration() {
        onView(withId(R.id.tvRegistrate)).perform(click());
        return new RegisterScreen();
    }

    public CampaignListScreen signIn(Account account) {
        enterCredentials(account.email, account.password);
        awaitScreen(R.id.campaign_drawer);
        return new CampaignListScreen();
    }

    public AdminDashboardScreen signInAsAdmin(String email, String password) {
        enterCredentials(email, password);
        awaitScreen(R.id.admin_dashboard_drawer);
        return new AdminDashboardScreen();
    }

    private void enterCredentials(String email, String password) {
        onView(withId(R.id.etEmail)).perform(replaceText(email), closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(replaceText(password), closeSoftKeyboard());
        onView(withId(R.id.btnIngresar)).perform(click());
    }

    // Shared UI queries; business assertions stay in CriticalFlowsTest.
    private static void awaitScreen(int id) {
        onView(isRoot()).perform(new ViewAction() {
            @Override public Matcher<View> getConstraints() { return isRoot(); }
            @Override public String getDescription() { return "esperar la pantalla destino del login"; }

            @Override
            public void perform(UiController uiController, View view) {
                long deadline = SystemClock.uptimeMillis() + 10000;
                do {
                    for (android.app.Activity activity : ActivityLifecycleMonitorRegistry.getInstance()
                            .getActivitiesInStage(Stage.RESUMED)) {
                        View destination = activity.findViewById(id);
                        if (destination != null && destination.isShown()) {
                            return;
                        }
                    }
                    uiController.loopMainThreadForAtLeast(50);
                } while (SystemClock.uptimeMillis() < deadline);
                throw new IllegalStateException("Login no abrió la pantalla destino dentro de 10 segundos.");
            }
        });
        onIdle();
    }

    static boolean isShown(int id) {
        AtomicBoolean result = new AtomicBoolean();
        onView(withId(id)).check((view, exception) -> {
            if (exception == null) {
                result.set(view.isShown());
            }
        });
        return result.get();
    }

    static String text(int id) {
        AtomicReference<String> result = new AtomicReference<>();
        onView(withId(id)).check((view, exception) -> {
            if (exception != null) {
                throw exception;
            }
            result.set(((TextView) view).getText().toString());
        });
        return result.get();
    }

    static String error(int id) {
        AtomicReference<String> result = new AtomicReference<>();
        onView(withId(id)).check((view, exception) -> {
            if (exception != null) {
                throw exception;
            }
            CharSequence error = ((TextView) view).getError();
            result.set(error == null ? null : error.toString());
        });
        return result.get();
    }
}

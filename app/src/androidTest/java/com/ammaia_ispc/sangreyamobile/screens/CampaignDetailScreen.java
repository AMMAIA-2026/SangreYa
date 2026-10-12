package com.ammaia_ispc.sangreyamobile.screens;

import android.view.accessibility.AccessibilityEvent;

import androidx.test.platform.app.InstrumentationRegistry;

import com.ammaia_ispc.sangreyamobile.R;

import java.util.concurrent.CopyOnWriteArrayList;

import static androidx.test.espresso.Espresso.onIdle;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public final class CampaignDetailScreen {
    private final CopyOnWriteArrayList<String> messages = new CopyOnWriteArrayList<>();

    public String title() { return LoginScreen.text(R.id.detail_title); }
    public String location() { return LoginScreen.text(R.id.detail_location); }
    public String description() { return LoginScreen.text(R.id.detail_description); }
    public String capacity() { return LoginScreen.text(R.id.detail_capacity); }
    public String dates() { return LoginScreen.text(R.id.detail_dates); }

    public int registeredCount() {
        return Integer.parseInt(LoginScreen.text(R.id.detail_registered).replaceAll("[^0-9]", ""));
    }

    public void enroll() {
        messages.clear();
        // Observe Toast text only; this is not an accessibility assertion or scan.
        InstrumentationRegistry.getInstrumentation().getUiAutomation()
                .setOnAccessibilityEventListener(event -> {
                    if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
                            && "android.widget.Toast".contentEquals(event.getClassName() == null
                            ? "" : event.getClassName())) {
                        for (CharSequence text : event.getText()) {
                            messages.add(text.toString());
                        }
                    }
                });
        onView(withId(R.id.enroll_button)).perform(click());
        onIdle();
    }

    public boolean receivedMessage(String expected) {
        // Also check a still-visible custom Toast, whose view belongs to this process.
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            if (android.os.Build.VERSION.SDK_INT >= 29) {
                for (android.view.View root : android.view.inspector.WindowInspector.getGlobalWindowViews()) {
                    android.widget.TextView toast = root.findViewById(R.id.toast_message);
                    if (toast != null) {
                        messages.add(toast.getText().toString());
                    }
                }
            }
        });
        return messages.contains(expected);
    }

    public void returnToList() {
        InstrumentationRegistry.getInstrumentation().getUiAutomation()
                .setOnAccessibilityEventListener(null);
        onView(withId(R.id.menu_button)).perform(click());
    }
}

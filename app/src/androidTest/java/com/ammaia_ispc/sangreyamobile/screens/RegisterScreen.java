package com.ammaia_ispc.sangreyamobile.screens;

import android.widget.CheckBox;

import com.ammaia_ispc.sangreyamobile.R;
import com.ammaia_ispc.sangreyamobile.helpers.DateHelper;
import com.ammaia_ispc.sangreyamobile.support.TestFixtures.Account;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public final class RegisterScreen {
    public boolean isOpen() {
        return LoginScreen.isShown(R.id.btnCreateAccount);
    }

    public void submit() {
        onView(withId(R.id.btnCreateAccount)).perform(scrollTo(), click());
    }

    public String nameError() {
        return LoginScreen.error(R.id.etName);
    }

    public boolean isFormCleared() {
        int[] fields = {R.id.etName, R.id.etApellido, R.id.etUsername, R.id.etDni,
                R.id.etRegisterEmail, R.id.etFechaNacimiento, R.id.etRegisterPassword,
                R.id.etConfirmPassword};
        for (int id : fields) {
            if (!LoginScreen.text(id).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public void complete(Account account) {
        fill(R.id.etName, account.name);
        fill(R.id.etApellido, account.lastName);
        fill(R.id.etUsername, account.username);
        fill(R.id.etDni, account.dni);
        fill(R.id.etRegisterEmail, account.email);
        fill(R.id.etFechaNacimiento, DateHelper.toDisplayDate(account.birthDate));
        fill(R.id.etRegisterPassword, account.password);
        fill(R.id.etConfirmPassword, account.password);
        onView(withId(R.id.cbAceptarTerminos)).perform(scrollTo(), click());
        java.util.concurrent.atomic.AtomicBoolean checked = new java.util.concurrent.atomic.AtomicBoolean();
        onView(withId(R.id.cbAceptarTerminos)).check((view, exception) -> {
            if (exception != null) {
                throw exception;
            }
            checked.set(((CheckBox) view).isChecked());
        });
        // This focusable-in-touch-mode checkbox consumes the first tap to acquire focus.
        if (!checked.get()) {
            onView(withId(R.id.cbAceptarTerminos)).perform(click());
        }
        onView(withId(android.R.id.button1)).perform(click());
    }

    public LoginScreen returnToLogin() {
        onView(withId(R.id.tvIniciarSesion)).perform(scrollTo(), click());
        return new LoginScreen();
    }

    private void fill(int id, String value) {
        onView(withId(id)).perform(scrollTo(), replaceText(value), closeSoftKeyboard());
    }
}

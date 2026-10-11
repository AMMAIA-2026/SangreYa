package com.ammaia_ispc.sangreyamobile.helpers;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.res.ColorStateList;

import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.ammaia_ispc.sangreyamobile.R;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;


public final class UiHelper {
    private UiHelper() {
    }
    public static void setLoading(
            ProgressBar loadingIndicator,
            View contentView,
            boolean loading,
            View... disabledViews) {
        loadingIndicator.setVisibility(loading ? View.VISIBLE : View.GONE);
        contentView.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
        for (View disabledView : disabledViews) {
            disabledView.setEnabled(!loading);
        }
    }

    public static void configureSystemBars(Activity activity) {
        Window window = activity.getWindow();
        window.setStatusBarColor(ContextCompat.getColor(activity, R.color.dark_red));
        window.setNavigationBarColor(ContextCompat.getColor(activity, R.color.white));
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
    }

    public static void showMessage(TextView messageView, int messageResId) {
        messageView.setText(messageResId);
        messageView.setVisibility(View.VISIBLE);
    }
    public static void clearMessage(TextView messageView) {
        messageView.setText("");
        messageView.setVisibility(View.GONE);
    }

    public static boolean focusFirstError(TextView... fields) {
        for (TextView field : fields) {
            if (field.getError() != null) {
                field.requestFocus();
                return true;
            }
        }
        return false;
    }

    public static String normalized(String value, Locale locale) {
        return value == null ? "" : value.trim().toLowerCase(locale);
    }

    public static void showToast(Context context, CharSequence message, int duration) {
        Toast.makeText(context, message, duration).show();
    }

    public static void showToast(Context context, @StringRes int messageResId, int duration) {
        showToast(context, context.getText(messageResId), duration);
    }

    public static void showCustomToast(Activity activity, CharSequence message, int duration) {
        View toastView = activity.getLayoutInflater().inflate(R.layout.toast_enrollment, null);
        ((TextView) toastView.findViewById(R.id.toast_message)).setText(message);

        Toast toast = Toast.makeText(activity, message, duration);
        toast.setGravity(
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL,
                0,
                activity.getResources().getDimensionPixelSize(R.dimen.toast_bottom_offset));
        toast.setView(toastView);
        toast.show();
    }

    public static void showCustomToast(Activity activity, @StringRes int messageResId, int duration) {
        showCustomToast(activity, activity.getText(messageResId), duration);
    }

    public static void configurePasswordToggle(
            EditText passwordInput,
            ImageButton toggleButton) {
        configurePasswordToggle(passwordInput, toggleButton,
                R.string.show_password, R.string.hide_password);
    }

    public static void configurePasswordToggle(
            EditText passwordInput,
            ImageButton toggleButton,
            @StringRes int showDescriptionResId,
            @StringRes int hideDescriptionResId) {
        toggleButton.setContentDescription(
                toggleButton.getContext().getString(showDescriptionResId));
        toggleButton.setOnClickListener(view -> {
            boolean passwordVisible = !toggleButton.isSelected();
            toggleButton.setSelected(passwordVisible);
            passwordInput.setTransformationMethod(passwordVisible
                    ? HideReturnsTransformationMethod.getInstance()
                    : PasswordTransformationMethod.getInstance());
            passwordInput.setSelection(passwordInput.length());
            toggleButton.setContentDescription(toggleButton.getContext().getString(
                    passwordVisible ? hideDescriptionResId : showDescriptionResId));
        });
    }

    public static void styleFilter(
            Context context,
            MaterialButton filter,
            boolean selected) {

        filter.setSelected(selected);
        ViewCompat.setStateDescription(filter, context.getString(
                selected ? R.string.filter_selected : R.string.filter_not_selected));
        filter.setTypeface(filter.getTypeface(), selected ? Typeface.BOLD : Typeface.NORMAL);

        filter.setTextColor(
                ContextCompat.getColor(
                        context,
                        selected ? R.color.white : R.color.secondary_text
                )
        );

        filter.setBackgroundTintList(
                ColorStateList.valueOf(
                        ContextCompat.getColor(
                                context,
                                selected ? R.color.primary_red : R.color.surface
                        )
                )
        );
    }

    public static void announceFilterChange(MaterialButton filter) {
        filter.announceForAccessibility(filter.getContext().getString(
                R.string.filter_applied, filter.getText()));
    }
}

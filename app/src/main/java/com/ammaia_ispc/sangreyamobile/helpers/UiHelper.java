package com.ammaia_ispc.sangreyamobile.helpers;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.res.ColorStateList;

import androidx.core.content.ContextCompat;

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
    public static String normalized(String value, Locale locale) {
        return value == null ? "" : value.trim().toLowerCase(locale);
    }

    public static void showToast(Context context, CharSequence message, int duration) {
        Toast.makeText(context, message, duration).show();
    }

    public static void styleFilter(
            Context context,
            MaterialButton filter,
            boolean selected) {

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
    /*
     * TODO: completar la centralizacion de Toast.makeText.
     * Pendiente:
     * 1. Utilizarlo en las llamadas marcadas.
     * 2. Detalle: puede haber métodos que estén usando esto con R.string, que es int y no caracter. Es
     * un identificador de recurso, y hay que castearlo con "getString(R.string.algun_mensaje)"
     * para poder pasarle correctamente como caracter.
     */
}

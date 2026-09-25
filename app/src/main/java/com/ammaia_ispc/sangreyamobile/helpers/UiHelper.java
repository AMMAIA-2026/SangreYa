package com.ammaia_ispc.sangreyamobile.helpers;

import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

    /*
     * TODO: completar la centralizacion de Toast.makeText.
     * Pendiente:
     * 1. Utilizarlo en las llamadas marcadas.
     * 2. Detalle: puede haber métodos que estén usando esto con R.string, que es int y no caracter. Es
     * un identificador de recurso, y hay que castearlo con "getString(R.string.algun_mensaje)"
     * para poder pasarle correctamente como caracter.
     */
}

package com.ammaia_ispc.sangreyamobile.helpers;

import android.app.Activity;
import android.view.View;
import android.view.Window;

import androidx.core.content.ContextCompat;

import com.ammaia_ispc.sangreyamobile.R;

public final class CampaignHelper {
    private CampaignHelper() {
    }

    public static void configureSystemBars(Activity activity) {
        Window window = activity.getWindow();
        window.setStatusBarColor(ContextCompat.getColor(activity, R.color.dark_red));
        window.setNavigationBarColor(ContextCompat.getColor(activity, R.color.white));
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
    }

    public static String statusText(String status) {
        return status.equals("Proximamente") ? "Próximamente" : status;
    }

    public static int statusColor(String status) {
        if (status.equals("Activa")) {
            return R.color.active_text;
        }
        if (status.equals("Proximamente")) {
            return R.color.upcoming_text;
        }
        return R.color.secondary_text;
    }

    public static int statusBackground(String status) {
        if (status.equals("Activa")) {
            return R.drawable.bg_status_active;
        }
        if (status.equals("Proximamente")) {
            return R.drawable.bg_status_upcoming;
        }
        return R.drawable.bg_status_finished;
    }

    public static String formatShortDate(String start, String end) {
        String startDay = start.substring(8, 10).replaceFirst("^0", "");
        String endDay = end.substring(8, 10).replaceFirst("^0", "");
        String month = monthName(Integer.parseInt(end.substring(5, 7)));
        return startDay + " al " + endDay + " de " + month;
    }

    public static String formatLongDate(String start, String end) {
        return formatShortDate(start, end) + ", " + end.substring(0, 4);
    }

    private static String monthName(int month) {
        String[] months = {"enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"};
        return months[month - 1];
    }
}

package com.ammaia_ispc.sangreyamobile.helpers;

import com.ammaia_ispc.sangreyamobile.R;
import com.ammaia_ispc.sangreyamobile.model.Campaign;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class CampaignHelper {
    private CampaignHelper() {
    }

    public static boolean isActiveOn(Campaign campaign, Date today) {
        if (!"Activa".equals(campaign.calculatedStatus)) {
            return false;
        }

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        formatter.setLenient(false);
        Date start = parseCampaignDate(campaign.startDate, formatter);
        Date end = parseCampaignDate(campaign.endDate, formatter);
        if (start == null || end == null) {
            return false;
        }

        // The API supplies calendar dates, so the entire start/end day is included.
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date currentDay = calendar.getTime();
        return !currentDay.before(start) && !currentDay.after(end);
    }

    private static Date parseCampaignDate(String value, SimpleDateFormat formatter) {
        if (value == null || !value.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
            return null;
        }
        ParsePosition position = new ParsePosition(0);
        Date date = formatter.parse(value, position);
        return position.getIndex() == value.length() ? date : null;
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
        return R.color.finished_text;
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
        String startMonth = monthName(Integer.parseInt(start.substring(5, 7)));
        String endMonth = monthName(Integer.parseInt(end.substring(5, 7)));
        String startYear = start.substring(0, 4);
        String endYear = end.substring(0, 4);
        if (!startYear.equals(endYear)) {
            return startDay + " de " + startMonth + " de " + startYear
                    + " al " + endDay + " de " + endMonth + " de " + endYear;
        }
        if (!start.substring(5, 7).equals(end.substring(5, 7))) {
            return startDay + " de " + startMonth + " al " + endDay + " de " + endMonth;
        }
        return startDay + " al " + endDay + " de " + endMonth;
    }

    public static String formatLongDate(String start, String end) {
        String range = formatShortDate(start, end);
        return start.substring(0, 4).equals(end.substring(0, 4))
                ? range + ", " + end.substring(0, 4)
                : range;
    }

    private static String monthName(int month) {
        String[] months = {"enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"};
        return months[month - 1];
    }
}

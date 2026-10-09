package com.ammaia_ispc.sangreyamobile.helpers;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class CampaignValidator {
    public static final int MAX_CAPACITY = 1000;
    private static final String ISO_PATTERN = "yyyy-MM-dd";

    private CampaignValidator() {}

    public static boolean isValidDateRange(String startIso, String endIso) {
        Date start = parseIso(startIso);
        Date end = parseIso(endIso);
        return start != null && end != null && !end.before(start);
    }

    public static boolean isValidCapacity(Integer capacity) {
        return capacity != null && capacity >= 1 && capacity <= MAX_CAPACITY;
    }

    private static Date parseIso(String value) {
        if (value == null || value.length() != ISO_PATTERN.length()) {
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat(ISO_PATTERN, Locale.US);
        format.setLenient(false);
        ParsePosition position = new ParsePosition(0);
        Date parsed = format.parse(value, position);
        return position.getIndex() == value.length() ? parsed : null;
    }
}

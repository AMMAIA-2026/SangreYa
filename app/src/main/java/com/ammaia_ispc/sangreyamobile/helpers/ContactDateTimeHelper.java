package com.ammaia_ispc.sangreyamobile.helpers;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ContactDateTimeHelper {
    private static final Locale ARGENTINA_LOCALE = new Locale("es", "AR");
    private static final TimeZone ARGENTINA_TIME_ZONE =
            TimeZone.getTimeZone("America/Argentina/Buenos_Aires");
    private static final Pattern FRACTIONAL_SECONDS =
            Pattern.compile("\\.(\\d+)(?=(?:Z|[+-]\\d{2}:?\\d{2})?$)", Pattern.CASE_INSENSITIVE);
    private static final Pattern COMPACT_TIME_ZONE = Pattern.compile("([+-]\\d{2})(\\d{2})$");
    private static final String[] INPUT_PATTERNS = {
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss"
    };

    private ContactDateTimeHelper() {
    }

    public static String formatForAdmin(String value) {
        if (value == null || value.trim().isEmpty()) {
            return value;
        }

        Date date = parse(value.trim().replace(' ', 'T'));
        if (date == null) {
            return value;
        }

        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm 'hs' - dd/MM/yyyy", ARGENTINA_LOCALE);
        formatter.setTimeZone(ARGENTINA_TIME_ZONE);
        return formatter.format(date);
    }

    private static Date parse(String value) {
        String normalized = normalizeFractionalSeconds(value);
        Matcher compactTimeZone = COMPACT_TIME_ZONE.matcher(normalized);
        if (compactTimeZone.find()) {
            normalized = compactTimeZone.replaceFirst("$1:$2");
        }

        for (String pattern : INPUT_PATTERNS) {
            SimpleDateFormat parser = new SimpleDateFormat(pattern, Locale.US);
            parser.setLenient(false);
            parser.setTimeZone(ARGENTINA_TIME_ZONE);

            ParsePosition position = new ParsePosition(0);
            Date parsed = parser.parse(normalized, position);
            if (parsed != null && position.getIndex() == normalized.length()) {
                return parsed;
            }
        }
        return null;
    }

    private static String normalizeFractionalSeconds(String value) {
        Matcher matcher = FRACTIONAL_SECONDS.matcher(value);
        if (!matcher.find()) {
            return value;
        }

        String fraction = matcher.group(1);
        if (fraction.length() > 3) {
            fraction = fraction.substring(0, 3);
        } else {
            while (fraction.length() < 3) {
                fraction += "0";
            }
        }
        return matcher.replaceFirst("." + fraction);
    }
}

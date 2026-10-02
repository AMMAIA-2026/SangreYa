package com.ammaia_ispc.sangreyamobile.helpers;

import android.app.DatePickerDialog;
import android.content.Context;
import android.text.InputType;
import android.view.MotionEvent;
import android.widget.EditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class DateHelper {
    private static final String DISPLAY_DATE_PATTERN = "dd/MM/yyyy";
    private static final String API_DATE_PATTERN = "yyyy-MM-dd";

    private DateHelper() {
    }

    public static void configureDateInput(Context context, EditText dateInput) {
        dateInput.setFocusable(true);
        dateInput.setFocusableInTouchMode(true);
        dateInput.setCursorVisible(true);
        dateInput.setInputType(
                InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
        dateInput.setOnTouchListener((view, event) -> {
            boolean onCalendarIcon = dateInput.getCompoundDrawables()[2] != null
                    && event.getX()
                    >= dateInput.getWidth() - dateInput.getCompoundPaddingRight();
            if (onCalendarIcon) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    showDatePicker(context, dateInput);
                }
                return true;
            }
            return false;
        });
    }

    public static void showDatePicker(Context context, EditText dateInput) {
        Calendar selectedDate = Calendar.getInstance();
        Date currentDate = parseDate(dateInput.getText().toString(), DISPLAY_DATE_PATTERN);
        if (currentDate != null) {
            selectedDate.setTime(currentDate);
        }

        new DatePickerDialog(
                context,
                (view, year, month, dayOfMonth) -> dateInput.setText(String.format(
                        Locale.US,
                        "%02d/%02d/%04d",
                        dayOfMonth,
                        month + 1,
                        year)),
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    public static String toIsoDate(String displayDate) {
        Date parsedDate = parseDate(displayDate, DISPLAY_DATE_PATTERN);
        return parsedDate == null ? null : formatDate(parsedDate, API_DATE_PATTERN);
    }

    public static String toDisplayDate(String isoDate) {
        Date parsedDate = parseDate(isoDate, API_DATE_PATTERN);
        return parsedDate == null ? null : formatDate(parsedDate, DISPLAY_DATE_PATTERN);
    }

    public static boolean isValidDisplayDate(String displayDate) {
        return parseDate(displayDate, DISPLAY_DATE_PATTERN) != null;
    }

    public static boolean isValidIsoDate(String isoDate) {
        return parseDate(isoDate, API_DATE_PATTERN) != null;
    }

    private static Date parseDate(String value, String pattern) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        SimpleDateFormat formatter = new SimpleDateFormat(pattern, Locale.US);
        formatter.setLenient(false);
        try {
            return formatter.parse(value);
        } catch (ParseException ignored) {
            return null;
        }
    }

    private static String formatDate(Date date, String pattern) {
        return new SimpleDateFormat(pattern, Locale.US).format(date);
    }
}

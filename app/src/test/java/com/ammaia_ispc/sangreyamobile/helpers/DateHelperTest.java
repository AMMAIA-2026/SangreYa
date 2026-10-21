package com.ammaia_ispc.sangreyamobile.helpers;

import com.ammaia_ispc.sangreyamobile.R;

import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.assertEquals;

public class DateHelperTest {
    @Test
    public void rejectsFutureBirthDatesWithSpecificMessage() {
        Calendar today = date(2026, 10, 21);

        assertEquals(R.string.error_birth_date_future,
                DateHelper.getBirthDateError("2026-10-22", today));
        assertEquals(R.string.error_birth_date_future,
                DateHelper.getBirthDateError("2026-12-31", today));
        assertEquals(R.string.error_birth_date_future,
                DateHelper.getBirthDateError("2027-01-01", today));
        assertEquals(R.string.error_birth_date_age_range,
                DateHelper.getBirthDateError("2026-10-21", today));
    }

    @Test
    public void acceptsOnlyAges18Through64Inclusive() {
        Calendar today = date(2026, 10, 21);

        assertEquals(R.string.error_birth_date_age_range,
                DateHelper.getBirthDateError("2009-10-21", today));
        assertEquals(0, DateHelper.getBirthDateError("2008-10-21", today));
        assertEquals(0, DateHelper.getBirthDateError("1962-10-21", today));
        assertEquals(R.string.error_birth_date_age_range,
                DateHelper.getBirthDateError("1961-10-21", today));
    }

    @Test
    public void checksWhetherBirthdayHasOccurredThisYear() {
        Calendar today = date(2026, 10, 21);

        // Turns 18 tomorrow: still 17.
        assertEquals(R.string.error_birth_date_age_range,
                DateHelper.getBirthDateError("2008-10-22", today));
        assertEquals(0, DateHelper.getBirthDateError("2008-10-20", today));
        // Turns 65 tomorrow: still 64.
        assertEquals(0, DateHelper.getBirthDateError("1961-10-22", today));
        assertEquals(R.string.error_birth_date_age_range,
                DateHelper.getBirthDateError("1961-10-20", today));
        assertEquals(0, DateHelper.getBirthDateError("2008-01-01", today));
        assertEquals(R.string.error_birth_date_age_range,
                DateHelper.getBirthDateError("2008-12-31", today));
    }

    @Test
    public void handlesYearRolloverAndLeapDayBirthdays() {
        assertEquals(0, DateHelper.getBirthDateError("2008-12-31", date(2026, 12, 31)));
        assertEquals(R.string.error_birth_date_future,
                DateHelper.getBirthDateError("2027-01-01", date(2026, 12, 31)));
        assertEquals(R.string.error_birth_date_age_range,
                DateHelper.getBirthDateError("2008-02-29", date(2026, 2, 28)));
        assertEquals(0, DateHelper.getBirthDateError("2008-02-29", date(2026, 3, 1)));
    }

    @Test
    public void rejectsMissingMalformedAndNonexistentDates() {
        String[] invalidDates = {null, "", "2008-02-30", "2007-02-29", "21/10/2000",
                "2000-1-01", "2000-01-01extra"};
        for (String value : invalidDates) {
            assertEquals(value, R.string.profile_birth_date_invalid,
                    DateHelper.getBirthDateError(value, date(2026, 10, 21)));
        }
    }

    @Test
    public void validatesDisplayDateAfterConversionUsedByBothForms() {
        assertEquals(0, DateHelper.getBirthDateError(
                DateHelper.toIsoDate("21/10/2008"), date(2026, 10, 21)));
        assertEquals(R.string.error_birth_date_future, DateHelper.getBirthDateError(
                DateHelper.toIsoDate("22/10/2026"), date(2026, 10, 21)));
        assertEquals(R.string.profile_birth_date_invalid, DateHelper.getBirthDateError(
                DateHelper.toIsoDate("31/02/2000"), date(2026, 10, 21)));
    }

    private static Calendar date(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month - 1, day);
        return calendar;
    }
}

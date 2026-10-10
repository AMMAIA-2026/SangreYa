package com.ammaia_ispc.sangreyamobile.helpers;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CampaignValidatorTest {
    // TC-UNIT-06
    @Test
    public void rejectsInvalidDates() {
        // Arrange
        String startDate = "2026-11-10";
        String endBeforeStart = "2026-11-09";
        String nonexistentDate = "2026-02-30";
        String wrongFormat = "10/11/2026";

        // Act
        boolean acceptsEndBeforeStart = CampaignValidator.isValidDateRange(startDate, endBeforeStart);
        boolean acceptsNonexistentDate = CampaignValidator.isValidDateRange(nonexistentDate, "2026-03-05");
        boolean acceptsWrongFormat = CampaignValidator.isValidDateRange(wrongFormat, "2026-11-12");
        boolean acceptsNullStart = CampaignValidator.isValidDateRange(null, "2026-11-12");
        boolean acceptsEmptyEnd = CampaignValidator.isValidDateRange(startDate, "");

        // Assert: rejections plus minimal positive controls.
        assertFalse(acceptsEndBeforeStart);
        assertFalse(acceptsNonexistentDate);
        assertFalse(acceptsWrongFormat);
        assertFalse(acceptsNullStart);
        assertFalse(acceptsEmptyEnd);
        assertTrue(CampaignValidator.isValidDateRange(startDate, startDate));
        assertTrue(CampaignValidator.isValidDateRange(startDate, "2026-11-11"));
    }

    // TC-UNIT-07
    @Test
    public void validatesCapacity() {
        // Arrange
        Integer zero = 0;
        Integer negative = -5;
        Integer overLimit = CampaignValidator.MAX_CAPACITY + 1;
        Integer nullValue = null;
        Integer minimum = 1;
        Integer middle = 50;
        Integer maximum = CampaignValidator.MAX_CAPACITY;

        // Act
        boolean acceptsZero = CampaignValidator.isValidCapacity(zero);
        boolean acceptsNegative = CampaignValidator.isValidCapacity(negative);
        boolean acceptsOverLimit = CampaignValidator.isValidCapacity(overLimit);
        boolean acceptsNull = CampaignValidator.isValidCapacity(nullValue);
        boolean acceptsMinimum = CampaignValidator.isValidCapacity(minimum);
        boolean acceptsMiddle = CampaignValidator.isValidCapacity(middle);
        boolean acceptsMaximum = CampaignValidator.isValidCapacity(maximum);

        // Assert: out-of-range values are rejected and inclusive limits are accepted.
        assertFalse(acceptsZero);
        assertFalse(acceptsNegative);
        assertFalse(acceptsOverLimit);
        assertTrue(acceptsNull);
        assertTrue(acceptsMinimum);
        assertTrue(acceptsMiddle);
        assertTrue(acceptsMaximum);
    }
}

package com.ammaia_ispc.sangreyamobile.helpers;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CampaignValidatorTest {
    @Test
    public void rechazaFechasInvalidas() {
        String inicio = "2026-11-10";
        String finAnterior = "2026-11-09";
        String fechaInexistente = "2026-02-30";
        String formatoIncorrecto = "10/11/2026";

        boolean acceptsEndBeforeStart = CampaignValidator.isValidDateRange(inicio, finAnterior);
        boolean acceptsNonexistentDate = CampaignValidator.isValidDateRange(fechaInexistente, "2026-03-05");
        boolean acceptsWrongFormat = CampaignValidator.isValidDateRange(formatoIncorrecto, "2026-11-12");
        boolean acceptsNullStart = CampaignValidator.isValidDateRange(null, "2026-11-12");
        boolean acceptsEmptyEnd = CampaignValidator.isValidDateRange(inicio, "");
        boolean acceptsZeroCapacity = CampaignValidator.isValidCapacity(0);
        boolean acceptsNegativeCapacity = CampaignValidator.isValidCapacity(-5);
        boolean acceptsOverLimitCapacity = CampaignValidator.isValidCapacity(CampaignValidator.MAX_CAPACITY + 1);
        boolean acceptsNullCapacity = CampaignValidator.isValidCapacity(null);

        // Assert: rechazos y controles positivos mínimos.
        assertFalse(acceptsEndBeforeStart);
        assertFalse(acceptsNonexistentDate);
        assertFalse(acceptsWrongFormat);
        assertFalse(acceptsNullStart);
        assertFalse(acceptsEmptyEnd);
        assertFalse(acceptsZeroCapacity);
        assertFalse(acceptsNegativeCapacity);
        assertFalse(acceptsOverLimitCapacity);
        assertFalse(acceptsNullCapacity);
        assertTrue(CampaignValidator.isValidDateRange(inicio, inicio));
        assertTrue(CampaignValidator.isValidDateRange(inicio, "2026-11-11"));
        assertTrue(CampaignValidator.isValidCapacity(1));
        assertTrue(CampaignValidator.isValidCapacity(CampaignValidator.MAX_CAPACITY));
    }
}

package com.ammaia_ispc.sangreyamobile.helpers;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CampaignValidatorTest {
    // TC-UNIT-06
    @Test
    public void rechazaFechasInvalidas() {
        // Arrange
        String inicio = "2026-11-10";
        String finAnterior = "2026-11-09";
        String fechaInexistente = "2026-02-30";
        String formatoIncorrecto = "10/11/2026";

        // Act
        boolean acceptsEndBeforeStart = CampaignValidator.isValidDateRange(inicio, finAnterior);
        boolean acceptsNonexistentDate = CampaignValidator.isValidDateRange(fechaInexistente, "2026-03-05");
        boolean acceptsWrongFormat = CampaignValidator.isValidDateRange(formatoIncorrecto, "2026-11-12");
        boolean acceptsNullStart = CampaignValidator.isValidDateRange(null, "2026-11-12");
        boolean acceptsEmptyEnd = CampaignValidator.isValidDateRange(inicio, "");

        // Assert: rechazos y controles positivos mínimos.
        assertFalse(acceptsEndBeforeStart);
        assertFalse(acceptsNonexistentDate);
        assertFalse(acceptsWrongFormat);
        assertFalse(acceptsNullStart);
        assertFalse(acceptsEmptyEnd);
        assertTrue(CampaignValidator.isValidDateRange(inicio, inicio));
        assertTrue(CampaignValidator.isValidDateRange(inicio, "2026-11-11"));
    }

    // TC-UNIT-07
    @Test
    public void validaCupo() {
        // Arrange
        Integer cero = 0;
        Integer negativo = -5;
        Integer sobreLimite = CampaignValidator.MAX_CAPACITY + 1;
        Integer nulo = null;
        Integer minimo = 1;
        Integer intermedio = 50;
        Integer maximo = CampaignValidator.MAX_CAPACITY;

        // Act
        boolean acceptsZero = CampaignValidator.isValidCapacity(cero);
        boolean acceptsNegative = CampaignValidator.isValidCapacity(negativo);
        boolean acceptsOverLimit = CampaignValidator.isValidCapacity(sobreLimite);
        boolean acceptsNull = CampaignValidator.isValidCapacity(nulo);
        boolean acceptsMinimum = CampaignValidator.isValidCapacity(minimo);
        boolean acceptsMiddle = CampaignValidator.isValidCapacity(intermedio);
        boolean acceptsMaximum = CampaignValidator.isValidCapacity(maximo);

        // Assert: rechazo fuera de rango y aceptación de los límites inclusivos.
        assertFalse(acceptsZero);
        assertFalse(acceptsNegative);
        assertFalse(acceptsOverLimit);
        assertFalse(acceptsNull);
        assertTrue(acceptsMinimum);
        assertTrue(acceptsMiddle);
        assertTrue(acceptsMaximum);
    }
}

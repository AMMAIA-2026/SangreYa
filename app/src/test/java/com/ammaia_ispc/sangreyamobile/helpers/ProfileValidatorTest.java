package com.ammaia_ispc.sangreyamobile.helpers;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ProfileValidatorTest {
    // TC-UNIT-04
    @Test
    public void rechazaEmailYDniInvalidos() {
        // Arrange
        String invalidEmail = "sin-arroba";
        String shortDni = "123456";
        String longDni = "123456789";
        String nonNumericDni = "1234A678";

        // Act
        boolean acceptsEmail = ProfileValidator.isValidEmail(invalidEmail);
        boolean acceptsShortDni = ProfileValidator.isValidDni(shortDni);
        boolean acceptsLongDni = ProfileValidator.isValidDni(longDni);
        boolean acceptsNonNumericDni = ProfileValidator.isValidDni(nonNumericDni);

        // Assert: rechazo y controles positivos mínimos.
        assertFalse(acceptsEmail);
        assertFalse(acceptsShortDni);
        assertFalse(acceptsLongDni);
        assertFalse(acceptsNonNumericDni);
        assertTrue(ProfileValidator.isValidEmail("ana@example.test"));
        assertTrue(ProfileValidator.isValidDni("1234567"));
        assertTrue(ProfileValidator.isValidDni("12345678"));
    }
}

package com.ammaia_ispc.sangreyamobile.helpers;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PasswordValidatorTest {

    // TC-UNIT-14: mínimo 10 caracteres, mayúscula, minúscula, número y símbolo.
    @Test
    public void aceptaPasswordEnElMinimoYPorEncima() {
        assertTrue(PasswordValidator.isValid("Aa1234567!"));
        assertTrue(PasswordValidator.isValid("Aa12345678!"));
    }

    @Test
    public void rechazaPasswordDeNueveCaracteresAunqueCumplaLaComplejidad() {
        assertFalse(PasswordValidator.isValid("Aa123456!"));
    }

    @Test
    public void rechazaPasswordNulaOVacia() {
        assertFalse(PasswordValidator.isValid(null));
        assertFalse(PasswordValidator.isValid(""));
    }

    @Test
    public void rechazaPasswordSinMayuscula() {
        assertFalse(PasswordValidator.isValid("aa1234567!"));
    }

    @Test
    public void rechazaPasswordSinMinuscula() {
        assertFalse(PasswordValidator.isValid("AA1234567!"));
    }

    @Test
    public void rechazaPasswordSinNumero() {
        assertFalse(PasswordValidator.isValid("Aaabcdefg!"));
    }

    @Test
    public void rechazaPasswordSinSimboloInclusoConEspaciosOLetrasAcentuadas() {
        assertFalse(PasswordValidator.isValid("Aa12345678"));
        assertFalse(PasswordValidator.isValid("Aa1234567 "));
        assertFalse(PasswordValidator.isValid("Aa1234567ñ"));
        assertFalse(PasswordValidator.isValid("Aa1234567á"));
    }

    @Test
    public void aceptaPasswordConLetrasAcentuadasYSimbolo() {
        assertTrue(PasswordValidator.isValid("Aa123456ñ!"));
    }

    // TC-UNIT-15: la confirmación debe coincidir exactamente con la contraseña.
    @Test
    public void aceptaConfirmacionIdentica() {
        assertTrue(PasswordValidator.matchesConfirmation("Aa1234567!", "Aa1234567!"));
    }

    @Test
    public void rechazaConfirmacionDistintaSinNormalizarMayusculasNiEspacios() {
        assertFalse(PasswordValidator.matchesConfirmation("Aa1234567!", "Aa1234568!"));
        assertFalse(PasswordValidator.matchesConfirmation("Aa1234567!", "aa1234567!"));
        assertFalse(PasswordValidator.matchesConfirmation("Aa1234567!", "Aa1234567! "));
        assertFalse(PasswordValidator.matchesConfirmation("Aa1234567!", ""));
    }

    @Test
    public void rechazaConfirmacionConValoresNulos() {
        assertFalse(PasswordValidator.matchesConfirmation("Aa1234567!", null));
        assertFalse(PasswordValidator.matchesConfirmation(null, "Aa1234567!"));
        assertFalse(PasswordValidator.matchesConfirmation(null, null));
    }
}

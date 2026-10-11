package com.ammaia_ispc.sangreyamobile;

import androidx.test.espresso.accessibility.AccessibilityChecks;

import org.junit.BeforeClass;

/**
 * Habilita el Accessibility Test Framework una sola vez para toda la app
 */
public abstract class BaseAccessibilityTest {

    @BeforeClass
    public static void enableAccessibilityChecks() {
        AccessibilityChecks.enable().setRunChecksFromRootView(true);
    }
}

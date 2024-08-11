package org.github.gestalt.config.cdi;

public final class InnerTestClassCondition {

    public static Boolean isDisabled = true;

    private InnerTestClassCondition() {

    }

    public static boolean isDisabled() {
        // Always disable unless running manually
        return isDisabled;
    }

    public static void reset() {
        isDisabled = true;
    }
}

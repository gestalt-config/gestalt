package org.github.gestalt.config.cdi;

public class InnerTestClassCondition {

    public static Boolean isDisabled = true;

    public static boolean isDisabled() {
        // Always disable unless running manually
        return isDisabled;
    }

    public static void reset() {
        isDisabled = true;
    }
}

package org.github.gestalt.config.utils;

import java.util.Map;

/**
 * Wrapper for getting the System Environments.
 * Used so we can Mock the results
 */
public final class SystemWrapper {

    private SystemWrapper() {

    }

    /**
     * get the System.getenv()
     *
     * @return the System.getenv()
     */
    public static Map<String, String> getEnvVars() {
        return System.getenv();
    }
}

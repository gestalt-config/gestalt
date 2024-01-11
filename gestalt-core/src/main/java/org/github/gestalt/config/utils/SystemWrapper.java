package org.github.gestalt.config.utils;

import java.util.Map;

/**
 * Wrapper for getting the System Environments.
 * Used so we can Mock the results.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
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

package org.github.gestalt.config.utils;

import java.util.Map;
import java.util.Properties;

/**
 * Wrapper for getting the System Environments.
 * Used so we can Mock the results.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class SystemWrapper {

    private SystemWrapper() {

    }

    /**
     * Returns an unmodifiable string map view of the current system environment.
     * The environment is a system-dependent mapping from names to values which is passed from parent to child processes.
     *
     * @return the System.getenv()
     */
    public static Map<String, String> getEnvVars() {
        return System.getenv();
    }

    /**
     * Gets the value of the specified environment variable. An environment variable is a system-dependent external named value.
     *
     * @param variable the environment variable to get.
     * @return the System.getenv()
     */
    public static String getEnvVars(String variable) {
        return System.getenv(variable);
    }

    /**
     * Get the system Properties.
     *
     * @return the system Properties
     */
    public static Properties getProperties() {
        return System.getProperties();
    }
}

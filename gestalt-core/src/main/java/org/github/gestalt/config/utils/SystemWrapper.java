package org.github.gestalt.config.utils;

import java.util.Map;

public final class SystemWrapper {

    private SystemWrapper() {

    }

    public static Map<String, String> getEnvVars() {
        return System.getenv();
    }
}

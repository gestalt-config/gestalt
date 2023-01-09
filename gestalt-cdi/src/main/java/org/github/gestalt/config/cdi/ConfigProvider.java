package org.github.gestalt.config.cdi;

import org.github.gestalt.config.Gestalt;

public class ConfigProvider {
    private static Gestalt gestalt = null;

    public static void registerGestalt(Gestalt regGestalt) {
        if (gestalt == null) {
            synchronized (Gestalt.class) {
                if (gestalt == null) {
                    gestalt = regGestalt;
                    return;
                }
            }
        }
        throw new ConfigException("Gestalt has already been registered");
    }

    public static Gestalt getGestaltConfig() {
        return gestalt;
    }
}

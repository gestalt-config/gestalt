package org.github.gestalt.config.cdi;

import org.github.gestalt.config.Gestalt;

public final class ConfigProvider {
    private static volatile Gestalt gestalt = null;

    private ConfigProvider() { }

    public static void registerGestalt(Gestalt regGestalt) {
        if (gestalt == null) {  // NOPMD
            synchronized (ConfigProvider.class) {
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

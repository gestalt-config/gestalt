package org.github.gestalt.config.cdi;

import org.github.gestalt.config.Gestalt;

public final class GestaltConfigProvider {
    private static volatile Gestalt gestalt = null;

    private GestaltConfigProvider() { }

    public static void registerGestalt(Gestalt regGestalt) {
        if (gestalt == null) {  // NOPMD
            synchronized (GestaltConfigProvider.class) {
                if (gestalt == null) {
                    gestalt = regGestalt;
                    return;
                }
            }
        }
        throw new GestaltConfigException("Gestalt has already been registered");
    }

    public static Gestalt getGestaltConfig() {
        return gestalt;
    }

    public static void unRegisterGestalt() {
        if (gestalt != null) {  // NOPMD
            synchronized (GestaltConfigProvider.class) {
                if (gestalt != null) {
                    gestalt = null;
                }
            }
        }

    }
}

package org.github.gestalt.config.test.classes;

public interface DBPoolInterfaceDefault {
    int getMaxTotal();

    int getMaxPerRoute();

    int getValidateAfterInactivity();

    int getKeepAliveTimeoutMs();

    int getIdleTimeoutSec();

    default float getDefaultWait() {
        return 0.26f;
    }

    boolean isEnabled();
}

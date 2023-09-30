package org.github.gestalt.config.test.classes;

import java.util.Optional;

public interface DBPoolGenericInterface {
    int getMaxTotal();

    int getMaxPerRoute();

    int getValidateAfterInactivity();

    int getKeepAliveTimeoutMs();

    Optional<Integer> getIdleTimeoutSec();

    float getDefaultWait();

    boolean isEnabled();
}

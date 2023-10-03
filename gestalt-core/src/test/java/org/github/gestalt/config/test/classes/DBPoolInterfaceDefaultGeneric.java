package org.github.gestalt.config.test.classes;

import java.util.List;

public interface DBPoolInterfaceDefaultGeneric {
    int getMaxTotal();

    int getMaxPerRoute();

    int getValidateAfterInactivity();

    int getKeepAliveTimeoutMs();

    int getIdleTimeoutSec();

    default List<Integer> getDefaultWait() {
        return List.of(1, 2, 3, 4);
    }

    boolean isEnabled();
}

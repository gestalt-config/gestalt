package org.github.gestalt.config.test.classes;

import java.util.Optional;

public interface DBPoolInterfaceWrapper {
    Integer getMaxTotal();

    Integer getMaxPerRoute();

    Integer getValidateAfterInactivity();

    Integer getKeepAliveTimeoutMs();

    Optional<Integer> getIdleTimeoutSec();

    Float getDefaultWait();

    Boolean isEnabled();
}

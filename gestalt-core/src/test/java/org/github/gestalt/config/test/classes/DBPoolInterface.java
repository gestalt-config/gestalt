package org.github.gestalt.config.test.classes;

public interface DBPoolInterface {
    int getMaxTotal();
    int getMaxPerRoute();
    int getValidateAfterInactivity();
    int getKeepAliveTimeoutMs();
    int getIdleTimeoutSec();
    float getDefaultWait();
    boolean isEnabled();
}

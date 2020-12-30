package org.config.gestalt.test.classes;

public class DBPool {
    public int maxTotal;
    public int maxPerRoute;
    public int validateAfterInactivity;
    public int keepAliveTimeoutMs = 6000;
    public int idleTimeoutSec = 10;
    public float defaultWait = 33.0F;

    public DBPool() {
    }
}

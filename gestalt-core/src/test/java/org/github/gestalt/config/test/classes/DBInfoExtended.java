package org.github.gestalt.config.test.classes;

public class DBInfoExtended extends DBInfo {
    private int timeout;
    private String user;

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}

package org.github.gestalt.config.test.classes;

public class DBInfoExtended extends DBInfo {
    private int timeout = 10000;
    private String user = "admin";

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

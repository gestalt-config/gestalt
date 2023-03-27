package org.github.gestalt.config.test.classes;

public class DBInfoSetterChangeValue {
    private int port;
    private String uri;
    private String password;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port * 2;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri + "abc";
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = "****";
    }
}

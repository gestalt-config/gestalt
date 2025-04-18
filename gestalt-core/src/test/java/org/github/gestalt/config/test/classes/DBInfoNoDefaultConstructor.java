package org.github.gestalt.config.test.classes;

public class DBInfoNoDefaultConstructor {
    private int port;
    private String uri;
    private String password;

    // no default constructor
    public DBInfoNoDefaultConstructor(int port, String uri, String password, String passwordPrefix) {
        this.port = port;
        this.uri = uri;
        this.password = passwordPrefix + password;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

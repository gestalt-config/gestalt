package org.github.gestalt.config.test.classes;

public final class DBInfoPrivateConstructor {
    private int port;
    private String uri;
    private String password;

    // private constructor
    private DBInfoPrivateConstructor() {
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

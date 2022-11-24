package org.github.gestalt.config.test.classes;

public class DBInfoGeneric<T> {
    private T port;
    private String uri;
    private String password;

    public T getPort() {
        return port;
    }

    public void setPort(T port) {
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

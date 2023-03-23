package org.github.gestalt.config.test.classes;

public class DBInfoIntegerPortNonNullGetter {
    private Integer port;
    private String uri;
    private String password;

    public Integer getPort() {
        return port != null ? port : 1234;
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

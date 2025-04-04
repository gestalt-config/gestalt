package org.github.gestalt.config.test.classes;

public class DBInfoConstructor {
    private int port;
    private String uri;
    private String password;

    public DBInfoConstructor() {

    }

    public DBInfoConstructor(int port, String uri, String password) {
        this.port = port;
        this.uri = uri;
        this.password = password;
    }

    public DBInfoConstructor(int port, String uri) {
        this.port = port;
        this.uri = uri;
        this.password = "unknown";
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

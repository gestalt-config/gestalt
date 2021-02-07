package org.github.gestalt.config.test.classes;

public class DBInforNoConstructor {
    private int port = 100;
    private String uri = "test";
    private String password = "password";

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

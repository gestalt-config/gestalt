package org.github.gestalt.config.test.classes;

public class DBInfoStatic {
    private static int port;
    private String uri;
    private String password;

    public static int getPort() {
        return port;
    }

    public static void setPort(int port) {
        DBInfoStatic.port = port;
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

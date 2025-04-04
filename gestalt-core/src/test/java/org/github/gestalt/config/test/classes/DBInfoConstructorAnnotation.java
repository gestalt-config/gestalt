package org.github.gestalt.config.test.classes;

import org.github.gestalt.config.annotations.ConfigParameter;

public class DBInfoConstructorAnnotation {
    private int port;
    private String uri;
    private String password;

    public DBInfoConstructorAnnotation() {

    }

    public DBInfoConstructorAnnotation(@ConfigParameter(path = "address") int port,
                                              @ConfigParameter(path = "hostname") String uri,
                                              @ConfigParameter(path = "secret") String password) {
        this.port = port;
        this.uri = uri;
        this.password = password;
    }

    public DBInfoConstructorAnnotation(@ConfigParameter(path = "address") int port, @ConfigParameter(path = "hostname") String uri) {
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

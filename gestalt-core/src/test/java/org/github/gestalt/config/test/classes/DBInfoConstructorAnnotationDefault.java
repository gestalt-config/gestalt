package org.github.gestalt.config.test.classes;

import org.github.gestalt.config.annotations.ConfigParameter;

public class DBInfoConstructorAnnotationDefault {
    private int port;
    private String uri;
    private String password;

    public DBInfoConstructorAnnotationDefault() {

    }

    public DBInfoConstructorAnnotationDefault(@ConfigParameter(path = "address") int port,
                                              @ConfigParameter(path = "hostname") String uri,
                                              @ConfigParameter(path = "secret", defaultVal = "default") String password) {
        this.port = port;
        this.uri = uri;
        this.password = password;
    }

    public DBInfoConstructorAnnotationDefault(@ConfigParameter(path = "address") int port,
                                              @ConfigParameter(path = "secret") String secret) {
        this.port = port;
        this.uri = "unknown";
        this.password = secret;
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

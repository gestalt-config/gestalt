package org.github.gestalt.config.test.classes;

import org.github.gestalt.config.annotations.Config;

public class DBInfoConstructorAnnotationDefault {
    private int port;
    private String uri;
    private String password;

    public DBInfoConstructorAnnotationDefault() {

    }

    public DBInfoConstructorAnnotationDefault(@Config(path = "address") int port,
                                              @Config(path = "hostname") String uri,
                                              @Config(path = "secret", defaultVal = "default") String password) {
        this.port = port;
        this.uri = uri;
        this.password = password;
    }

    public DBInfoConstructorAnnotationDefault(@Config(path = "address") int port, @Config(path = "secret") String secret) {
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

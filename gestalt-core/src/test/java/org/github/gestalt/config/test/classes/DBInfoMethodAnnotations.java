package org.github.gestalt.config.test.classes;

import org.github.gestalt.config.annotations.Config;

public class DBInfoMethodAnnotations implements IDBInfoAnnotations {
    private int port;
    private String uri;
    private String password;

    @Override
    @Config(path = "channel", defaultVal = "1234")
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

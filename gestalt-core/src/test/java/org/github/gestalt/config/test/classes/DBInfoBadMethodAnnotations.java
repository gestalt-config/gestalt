package org.github.gestalt.config.test.classes;

import org.github.gestalt.config.annotations.Config;

public class DBInfoBadMethodAnnotations {

    private int port;
    private String uri;
    private String password;

    @Config(path = "channel", defaultVal = "abc")
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

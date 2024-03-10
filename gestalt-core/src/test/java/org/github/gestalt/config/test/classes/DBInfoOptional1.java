package org.github.gestalt.config.test.classes;

import java.util.Optional;

public class DBInfoOptional1 {
    private Integer port;
    private Optional<String> uri;
    private Optional<String> password;

    public Integer getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Optional<String> getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = Optional.ofNullable(uri);
    }

    public Optional<String> getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = Optional.ofNullable(password);
    }
}

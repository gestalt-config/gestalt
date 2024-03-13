package org.github.gestalt.config.model;

import org.github.gestalt.config.annotations.Config;

import java.util.Optional;

public class DBInfoOptional {
    private Optional<Integer> port;
    private Optional<String> uri;
    private Optional<String> password;

    @Config(defaultVal = "200")
    private Integer connections;

    public Optional<Integer> getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = Optional.of(port);
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

    public Integer getConnections() {
        return connections;
    }

    public void setConnections(Integer connections) {
        this.connections = connections;
    }
}

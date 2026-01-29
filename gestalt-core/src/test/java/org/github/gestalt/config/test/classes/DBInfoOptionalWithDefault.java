package org.github.gestalt.config.test.classes;

import java.util.Optional;

public class DBInfoOptionalWithDefault {
    private Optional<Integer> port;
    private Optional<String> uri;
    private Optional<String> password;
    private Optional<Integer> timeoutSeconds = Optional.of(30); // default value

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

    public Optional<Integer> getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(Optional<Integer> timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
}

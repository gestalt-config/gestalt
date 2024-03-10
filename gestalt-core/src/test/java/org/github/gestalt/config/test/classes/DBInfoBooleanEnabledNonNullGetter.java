package org.github.gestalt.config.test.classes;

public class DBInfoBooleanEnabledNonNullGetter {
    private Integer port;
    private String uri;
    private String password;

    private Boolean enabled;

    public Integer getPort() {
        return port != null ? port : 1234;
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

    public Boolean isEnabled() {
        return enabled == null || enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}

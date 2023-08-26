package org.github.gestalt.config.cdi;

public final class GestaltConfigException extends RuntimeException {
    private final String configPropertyName;

    public GestaltConfigException(String message) {
        super(message);
        this.configPropertyName = null;
    }

    public GestaltConfigException(String message, String configPropertyName) {
        super(message);
        this.configPropertyName = configPropertyName;
    }

    public GestaltConfigException(String message, Throwable cause) {
        super(message, cause);
        this.configPropertyName = null;
    }

    public GestaltConfigException(String message, String configPropertyName, Throwable cause) {
        super(message, cause);
        this.configPropertyName = configPropertyName;
    }

    public String getConfigPropertyName() {
        return configPropertyName;
    }

}

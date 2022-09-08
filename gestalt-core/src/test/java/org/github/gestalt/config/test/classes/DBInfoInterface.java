package org.github.gestalt.config.test.classes;

public interface DBInfoInterface {
    default int getPort() {
        return 10;
    }
    String getUri();
    String getPassword();
}

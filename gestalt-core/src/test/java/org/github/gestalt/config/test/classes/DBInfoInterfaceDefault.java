package org.github.gestalt.config.test.classes;

public interface DBInfoInterfaceDefault {
    default int getPort() {
        return 10;
    }

    String getUri();

    String getPassword();
}

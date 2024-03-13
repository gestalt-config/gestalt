package org.github.gestalt.config.model;

import org.github.gestalt.config.annotations.Config;

import java.util.Optional;

public interface DBInfoInterfaceOptional {
    Optional<Integer> getPort();
    Optional<String> getUri();
    Optional<String> getPassword();
    @Config(defaultVal = "200")
    Integer getConnections();
}

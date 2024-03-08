package org.github.gestalt.config.model;

import java.util.Optional;

public interface DBInfoInterfaceOptional {
    Optional<Integer> getPort();
    Optional<String> getUri();
    Optional<String> getPassword();
}

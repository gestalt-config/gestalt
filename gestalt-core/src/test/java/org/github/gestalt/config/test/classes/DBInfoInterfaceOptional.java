package org.github.gestalt.config.test.classes;

import java.util.Optional;

public interface DBInfoInterfaceOptional {
    Optional<Integer> getPort();
    Optional<String> getUri();
    Optional<String> getPassword();
}

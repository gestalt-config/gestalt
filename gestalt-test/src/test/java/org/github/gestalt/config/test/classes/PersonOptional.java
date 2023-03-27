package org.github.gestalt.config.test.classes;

import java.util.Optional;

public record PersonOptional(Optional<String> name, Optional<Integer> id) {
}


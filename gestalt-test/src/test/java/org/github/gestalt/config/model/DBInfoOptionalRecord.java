package org.github.gestalt.config.model;


import org.github.gestalt.config.annotations.Config;

import java.util.Optional;

public record DBInfoOptionalRecord(
    Optional<Integer> port,
    Optional<String> uri,
    Optional<String> password,
    @Config(defaultVal = "200") Integer connections) {
}

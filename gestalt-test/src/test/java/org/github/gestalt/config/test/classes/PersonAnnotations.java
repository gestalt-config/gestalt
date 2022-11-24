package org.github.gestalt.config.test.classes;

import org.github.gestalt.config.annotations.Config;

public record PersonAnnotations(String name, @Config(path = "identity", defaultVal = "1234") Integer id) {
}

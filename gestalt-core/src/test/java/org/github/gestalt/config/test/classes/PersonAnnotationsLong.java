package org.github.gestalt.config.test.classes;

import org.github.gestalt.config.annotations.Config;

public record PersonAnnotationsLong(String name, @Config(path = "identity.user", defaultVal = "1234") Integer id) {
}

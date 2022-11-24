package org.github.gestalt.config.test.classes;

import org.github.gestalt.config.annotations.Config;

public record PersonBadAnnotations(String name, @Config(path = "identity", defaultVal = "abc") Integer id) {
}

package org.github.gestalt.config.test.classes;

import org.github.gestalt.config.annotations.Config;

public record Person3(@Config(path = "test") Integer id, String name) {
}

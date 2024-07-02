package org.github.gestalt.config.test.classes;

import jakarta.annotation.Nullable;

public record PersonNullable(String name, @Nullable Integer id) {
}


package org.config.gestalt.token;

import java.util.Objects;

public class ObjectToken extends Token {
    private final String name;

    public ObjectToken(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ObjectToken)) {
            return false;
        }
        ObjectToken that = (ObjectToken) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}

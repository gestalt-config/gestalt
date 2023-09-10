package org.github.gestalt.config.cdi;

import java.util.Objects;

public final class ConfigClassWithPrefix {
    private final Class<?> klass;
    private final String prefix;

    public ConfigClassWithPrefix(Class<?> klass, String prefix) {
        this.klass = klass;
        this.prefix = prefix;
    }

    public static ConfigClassWithPrefix configClassWithPrefix(Class<?> klass, String prefix) {
        return new ConfigClassWithPrefix(klass, prefix);
    }

    public Class<?> getKlass() {
        return this.klass;
    }

    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ConfigClassWithPrefix)) {
            return false;
        }
        ConfigClassWithPrefix that = (ConfigClassWithPrefix) o;
        return Objects.equals(klass, that.klass) && Objects.equals(prefix, that.prefix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.klass, this.prefix);
    }
}

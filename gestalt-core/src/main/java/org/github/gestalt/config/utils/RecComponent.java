package org.github.gestalt.config.utils;


import java.lang.reflect.Type;

/**
 * A record component, which has a name, a type and an index.
 *
 * <p>(If running on Java 14+, this should be a record class ;) )
 *
 * <p>The latter is the index of the record components in the class file's
 * record attribute, required to invoke the record's canonical constructor .
 */
public class RecComponent {
    private final String name;
    private final Type type;
    private final Class<?> klass;
    private final int index;

    public RecComponent(String name, Type type, Class<?> klass, int index) {
        this.name = name;
        this.type = type;
        this.klass = klass;
        this.index = index;
    }

    public String name() {
        return name;
    }

    public Type typeGeneric() {
        return type;
    }

    public int index() {
        return index;
    }

    public Class<?> type() {
        return klass;
    }
}

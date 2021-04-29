package org.github.gestalt.config.utils;


/**
 * A record component, which has a name, a type and an index.
 *
 * <p> (If running on Java 14+, this should be a record class ;) )
 *
 * <p> The latter is the index of the record components in the class file's
 * record attribute, required to invoke the record's canonical constructor .
 */
public class RecComponent {
    private final String name;
    private final Class<?> type;
    private final int index;

    public RecComponent(String name, Class<?> type, int index) {
        this.name = name;
        this.type = type;
        this.index = index;
    }

    public String name() {
        return name;
    }

    public Class<?> type() {
        return type;
    }

    public int index() {
        return index;
    }
}

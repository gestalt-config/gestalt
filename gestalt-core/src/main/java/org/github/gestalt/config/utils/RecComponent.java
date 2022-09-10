package org.github.gestalt.config.utils;


import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
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
    private final Annotation[] annotations;
    private final Method accessor;

    /**
     * Create a Record component.
     *
     * @param name name of the Record component
     * @param type type of Record component
     * @param klass class of the Record component
     * @param index index of the Record component
     */
    public RecComponent(String name, Type type, Class<?> klass, Annotation[] annotations, Method accessor, int index) {
        this.name = name;
        this.type = type;
        this.klass = klass;
        this.index = index;
        this.annotations = annotations;
        this.accessor = accessor;
    }

    /**
     * get the name of the Record component.
     *
     * @return name of the Record component
     */
    public String name() {
        return name;
    }

    /**
     * get the type of the Record component.
     *
     * @return type of the Record component
     */
    public Type typeGeneric() {
        return type;
    }

    /**
     * get the index of the Record component.
     *
     * @return index of the Record component
     */
    public int index() {
        return index;
    }

    /**
     * get the class of the Record component.
     *
     * @return class of the Record component
     */
    public Class<?> type() {
        return klass;
    }

    /**
     * Returns annotations that are directly present on this element.
     *
     * @return Returns annotations that are directly present on this element.
     */
    public Annotation[] getDeclaredAnnotations() {
        return annotations;
    }

    /**
     * Returns a Method that represents the accessor for this record component.
     *
     * @return Returns a Method that represents the accessor for this record component.
     */
    public Method getAccessor() {
        return accessor;
    }
}

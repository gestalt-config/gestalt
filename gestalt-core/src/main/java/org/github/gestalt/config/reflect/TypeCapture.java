package org.github.gestalt.config.reflect;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * a java doesn't have reified generics so if we are trying to get the type of a list at runtime
 * we are unable to.
 * This class is modeled on the TypeToken from Guava, TypeLiteral from guice and jackson.
 *
 * @param <T> type to capture
 * @author Colin Redmond
 */
public class TypeCapture<T> {
    /**
     * Raw type for this TypeCapture.
     */
    protected final Class<?> rawType;

    /**
     * type for this TypeCapture.
     */
    protected final Type type;

    /**
     * hashcode for this TypeCapture.
     */
    protected final int hashCode;

    /**
     * The default constructor is used in the form new TypeCapture&#60;List&#60;String&#62;&#62;() { };
     * This will capture the type of List&#60;string&#62; and save it, allowing us to know at runtime the type of a list.
     */
    protected TypeCapture() {
        this.type = getSuperclassTypeParameter(getClass());
        this.rawType = buildRawType(type);
        this.hashCode = type.hashCode();
    }

    /**
     * Create a TypeCapture using a generic type, this will suffer from type erasure, so only use if for non generic classes.
     *
     * @param klass class to capture
     */
    protected TypeCapture(Class<T> klass) {
        Objects.requireNonNull(klass);
        this.type = klass;
        this.rawType = buildRawType(type);
        this.hashCode = type.hashCode();
    }

    /**
     * Create a TypeCapture using a Type.
     *
     * @param klass java Type
     */
    protected TypeCapture(Type klass) {
        Objects.requireNonNull(klass);
        this.type = klass;
        this.rawType = buildRawType(type);
        this.hashCode = type.hashCode();
    }

    /**
     * When we only have a class, we can get the type from the class.
     * This should not be used with classes that have generic type parameters.
     *
     * @param klass to get type of.
     * @param <T> type of capture
     * @return the TypeCapture
     */
    public static <T> TypeCapture<T> of(Class<T> klass) {   // NOPMD
        return new TypeCapture<>(klass);
    }

    /**
     * When we only have a class, we can get the type from the class.
     * This should not be used with classes that have generic type parameters.
     *
     * @param klass type to capture.
     * @param <T> type of capture
     * @return the TypeCapture
     */
    public static <T> TypeCapture<T> of(Type klass) {       // NOPMD
        return new TypeCapture<>(klass);
    }

    /**
     * If this class has a generic parameter.
     *
     * @return If this class has a generic parameter
     */
    public boolean hasParameter() {
        return type instanceof ParameterizedType;
    }

    /**
     * Get the TypeCapture of the first generic parameter or null if there is none.
     *
     * @return the TypeCapture of the first generic parameter or null if there is none.
     */
    public TypeCapture<?> getFirstParameterType() {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterized = (ParameterizedType) type;
            return TypeCapture.of(parameterized.getActualTypeArguments()[0]);
        } else {
            return null;
        }
    }

    /**
     * Get the TypeCapture of the second generic parameter or null if there is none.
     *
     * @return the TypeCapture of the second generic parameter or null if there is none.
     */
    public TypeCapture<?> getSecondParameterType() {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterized = (ParameterizedType) type;
            if (parameterized.getActualTypeArguments().length > 1) {
                return TypeCapture.of(parameterized.getActualTypeArguments()[1]);
            }
        }

        return null;
    }

    /**
     * Get all generic parameter types.
     *
     * @return list of all generic parameter types.
     */
    public List<TypeCapture<?>> getParameterTypes() {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterized = (ParameterizedType) type;
            return Arrays.stream(parameterized.getActualTypeArguments())
                         .map(TypeCapture::of)
                         .collect(Collectors.toList());
        } else {
            return null;
        }
    }

    /**
     * Returns the Class representing the component type of an array.
     * If this class does not represent an array class this method returns null.
     *
     * @return component type
     */
    public Class<?> getComponentType() {
        if (type instanceof Class<?>) {
            Class<?> klass = (Class<?>) type;
            return klass.getComponentType();
        } else {
            return null;
        }
    }

    /**
     * Get the raw type of this class.
     *
     * @return the raw type of this class.
     */
    public Class<?> getRawType() {
        return rawType;
    }

    /**
     * Get the name of the class.
     *
     * @return name of the class
     */
    public String getName() {
        return type.getTypeName();
    }

    /**
     * If this class is assignable from a type.
     *
     * @param classType type to check if we are asignable
     * @return If this class is assignable from a type.
     */
    public boolean isAssignableFrom(Type classType) {
        return rawType.isAssignableFrom(buildRawType(classType));
    }

    /**
     * If this type is a array.
     *
     * @return If this type is a array
     */
    public boolean isArray() {
        return getRawType().isArray();
    }

    /**
     * If this type is an Enum.
     *
     * @return If this type is an Enum
     */
    public boolean isEnum() {
        return getRawType().isEnum();
    }

    /**
     * Get the Super class for a type.
     *
     * @param subclass class we are looking for the super class of
     * @return Super class for a type
     */
    protected Type getSuperclassTypeParameter(Class<?> subclass) {
        Type superclass = subclass.getGenericSuperclass();
        if (superclass instanceof Class) {
            throw new RuntimeException("Missing type parameter.");
        }
        ParameterizedType parameterized = (ParameterizedType) superclass;
        return parameterized.getActualTypeArguments()[0];
    }

    /**
     * Lifted from Guice TypeLiteral.
     * https://github.com/google/guice/blob/master/core/src/com/google/inject/TypeLiteral.java
     *
     * @param type java type
     * @return class of the raw type
     */
    protected Class<?> buildRawType(Type type) {
        if (type instanceof Class<?>) {
            // type is a normal class.
            return (Class<?>) type;

        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;

            // I'm not exactly sure why getRawType() returns Type instead of Class.
            // Neal isn't either but suspects some pathological case related
            // to nested classes exists.
            return (Class<?>) parameterizedType.getRawType();

        } else if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) type).getGenericComponentType();
            return Array.newInstance(buildRawType(componentType), 0).getClass();

        } else if (type instanceof TypeVariable || type instanceof WildcardType) {
            // we could use the variable's bounds, but that'll won't work if there are multiple.
            // having a raw type that's more general than necessary is okay
            return Object.class;

        } else {
            throw new IllegalArgumentException(
                "Expected a Class, ParameterizedType, or GenericArrayType, but <" + type + "> is of type " + type.getClass().getName());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TypeCapture)) {
            return false;
        }
        TypeCapture<?> that = (TypeCapture<?>) o;
        return hashCode == that.hashCode && rawType.equals(that.rawType) && type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rawType, type, hashCode);
    }
}

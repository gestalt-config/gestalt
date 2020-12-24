package org.config.gestalt.reflect;

import java.lang.reflect.*;
import java.util.Objects;

public class TypeCapture<T> {
    final Class<? super T> rawType;
    final Type type;
    final int hashCode;

    protected TypeCapture() {
        this.type = getSuperclassTypeParameter(getClass());
        this.rawType = (Class<? super T>) buildRawType(type);
        this.hashCode = type.hashCode();
    }

    private TypeCapture(Class<T> klass) {
        this.type = klass;
        this.rawType = (Class<? super T>) buildRawType(type);
        this.hashCode = type.hashCode();
    }

    public static <T> TypeCapture<T> of(Class<T> klass) {       // NOPMD
        return new TypeCapture(klass);
    }

    public boolean hasParameter() {
        return type instanceof ParameterizedType;
    }

    public Class<?> getParameterType() {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterized = (ParameterizedType) type;
            return (Class<?>) parameterized.getActualTypeArguments()[0];
        } else {
            return null;
        }
    }

    public Class<?> getComponentType() {
        if (type instanceof Class<?>) {
            Class<?> klass = (Class<?>) type;
            return klass.getComponentType();
        } else {
            return null;
        }
    }

    public Class<?> getRawType() {
        return rawType;
    }

    public String getName() {
        return type.getTypeName();
    }

    public boolean isAssignableFrom(Type classType) {
        return rawType.isAssignableFrom(buildRawType(classType));
    }


    private Type getSuperclassTypeParameter(Class<?> subclass) {
        Type superclass = subclass.getGenericSuperclass();
        if (superclass instanceof Class) {
            throw new RuntimeException("Missing type parameter.");
        }
        ParameterizedType parameterized = (ParameterizedType) superclass;
        return parameterized.getActualTypeArguments()[0];
    }

    // Lifted from Guice TypeLiteral
    // https://github.com/google/guice/blob/master/core/src/com/google/inject/TypeLiteral.java
    private Class<?> buildRawType(Type type) {
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
        if (this == o) return true;
        if (!(o instanceof TypeCapture)) return false;
        TypeCapture<?> that = (TypeCapture<?>) o;
        return hashCode == that.hashCode && rawType.equals(that.rawType) && type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rawType, type, hashCode);
    }
}

package org.config.gestalt.reflect;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TypeCapture<T> {
    protected Class<?> rawType;
    protected Type type;
    protected int hashCode;

    protected TypeCapture() {
        this.type = getSuperclassTypeParameter(getClass());
        this.rawType = buildRawType(type);
        this.hashCode = type.hashCode();
    }

    protected TypeCapture(Class<T> klass) {
        this.type = klass;
        this.rawType = buildRawType(type);
        this.hashCode = type.hashCode();
    }

    protected TypeCapture(Type klass) {
        this.type = klass;
        this.rawType = buildRawType(type);
        this.hashCode = type.hashCode();
    }

    public static <T> TypeCapture<T> of(Class<T> klass) {   // NOPMD
        return new TypeCapture<>(klass);
    }

    public static <T> TypeCapture<T> of(Type klass) {       // NOPMD
        return new TypeCapture<>(klass);
    }

    public boolean hasParameter() {
        return type instanceof ParameterizedType;
    }

    public TypeCapture<?> getFirstParameterType() {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterized = (ParameterizedType) type;
            return TypeCapture.of(parameterized.getActualTypeArguments()[0]);
        } else {
            return null;
        }
    }

    public TypeCapture<?> getSecondParameterType() {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterized = (ParameterizedType) type;
            if (parameterized.getActualTypeArguments().length > 1) {
                return TypeCapture.of(parameterized.getActualTypeArguments()[1]);
            }
        }

        return null;
    }

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
     * @return
     */
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


    protected Type getSuperclassTypeParameter(Class<?> subclass) {
        Type superclass = subclass.getGenericSuperclass();
        if (superclass instanceof Class) {
            throw new RuntimeException("Missing type parameter.");
        }
        ParameterizedType parameterized = (ParameterizedType) superclass;
        return parameterized.getActualTypeArguments()[0];
    }

    // Lifted from Guice TypeLiteral
    // https://github.com/google/guice/blob/master/core/src/com/google/inject/TypeLiteral.java
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

    public boolean isArray() {
        return getRawType().isArray();
    }

    public boolean isEnum() {
        return getRawType().isEnum();
    }
}

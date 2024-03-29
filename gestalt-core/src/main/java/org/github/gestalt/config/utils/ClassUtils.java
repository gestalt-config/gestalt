/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copied from https://github.com/apache/commons-lang/blob/master/src/main/java/org/apache/commons/lang3/ClassUtils.java
 */

package org.github.gestalt.config.utils;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Operates on classes without using reflection.
 *
 * <p>
 * This class handles invalid {@code null} inputs as best it can. Each method documents its behavior in more detail.
 * </p>
 *
 * <p>
 * The notion of a {@code canonical name} includes the human readable name for the type, for example {@code int[]}. The
 * non-canonical method variants work with the JVM names, such as {@code [I}.
 * </p>
 *
 * @since 2.0
 */
@SuppressWarnings("EscapedEntity")
public final class ClassUtils {

    /**
     * Maps primitive {@link Class}es to their corresponding wrapper {@link Class}.
     */
    private static final Map<Class<?>, Class<?>> primitiveWrapperMap = new HashMap<>();
    /**
     * Maps wrapper {@link Class}es to their corresponding primitive types.
     */
    private static final Map<Class<?>, Class<?>> wrapperPrimitiveMap = new HashMap<>();


    static {
        primitiveWrapperMap.put(Boolean.TYPE, Boolean.class);
        primitiveWrapperMap.put(Byte.TYPE, Byte.class);
        primitiveWrapperMap.put(Character.TYPE, Character.class);
        primitiveWrapperMap.put(Short.TYPE, Short.class);
        primitiveWrapperMap.put(Integer.TYPE, Integer.class);
        primitiveWrapperMap.put(Long.TYPE, Long.class);
        primitiveWrapperMap.put(Double.TYPE, Double.class);
        primitiveWrapperMap.put(Float.TYPE, Float.class);
        primitiveWrapperMap.put(Void.TYPE, Void.TYPE);
    }

    static {
        primitiveWrapperMap.forEach((primitiveClass, wrapperClass) -> {
            if (!primitiveClass.equals(wrapperClass)) {
                wrapperPrimitiveMap.put(wrapperClass, primitiveClass);
            }
        });
    }

    /**
     * ClassUtils instances should NOT be constructed in standard programming. Instead, the class should be used as
     * {@code ClassUtils.getShortClassName(cls)}.
     *
     * <p>
     * This constructor is public to permit tools that require a JavaBean instance to operate.
     * </p>
     */
    private ClassUtils() {
    }

    /**
     * Checks if one {@link Class} can be assigned to a variable of another {@link Class}.
     *
     * <p>
     * Unlike the {@link Class#isAssignableFrom(java.lang.Class)} method, this method takes into account widenings of
     * primitive classes and {@code null}s.
     * </p>
     *
     * <p>
     * Primitive widenings allow an int to be assigned to a long, float or double. This method returns the correct result
     * for these cases.
     * </p>
     *
     * <p>
     * {@code null} may be assigned to any reference type. This method will return {@code true} if {@code null} is passed in
     * and the toClass is non-primitive.
     * </p>
     *
     * <p>
     * Specifically, this method tests whether the type represented by the specified {@link Class} parameter can be
     * converted to the type represented by this {@link Class} object via an identity conversion widening primitive or
     * widening reference conversion. See <em><a href="https://docs.oracle.com/javase/specs/">The Java Language
     * Specification</a></em>, sections 5.1.1, 5.1.2 and 5.1.4 for details.
     * </p>
     *
     * <p>
     * <strong>Since Lang 3.0,</strong> this method will default behavior for calculating assignability between primitive
     * and wrapper types <em>corresponding to the running Java version</em>; i.e. autoboxing will be the default behavior in
     * VMs running Java versions &gt; 1.5.
     * </p>
     *
     * @param cls     the Class to check, may be null
     * @param toClass the Class to try to assign into, returns false if null
     * @return {@code true} if assignment possible
     */
    public static boolean isAssignable(final Class<?> cls, final Class<?> toClass) {
        return isAssignable(cls, toClass, true);
    }

    /**
     * Checks if one {@link Class} can be assigned to a variable of another {@link Class}.
     *
     * <p>
     * Unlike the {@link Class#isAssignableFrom(java.lang.Class)} method, this method takes into account widenings of
     * primitive classes and {@code null}s.
     * </p>
     *
     * <p>
     * Primitive widenings allow an int to be assigned to a long, float or double. This method returns the correct result
     * for these cases.
     * </p>
     *
     * <p>
     * {@code null} may be assigned to any reference type. This method will return {@code true} if {@code null} is passed in
     * and the toClass is non-primitive.
     * </p>
     *
     * <p>
     * Specifically, this method tests whether the type represented by the specified {@link Class} parameter can be
     * converted to the type represented by this {@link Class} object via an identity conversion widening primitive or
     * widening reference conversion. See <em><a href="https://docs.oracle.com/javase/specs/">The Java Language
     * Specification</a></em>, sections 5.1.1, 5.1.2 and 5.1.4 for details.
     * </p>
     *
     * @param cls        the Class to check, may be null
     * @param toClass    the Class to try to assign into, returns false if null
     * @param autoboxing whether to use implicit autoboxing/unboxing between primitives and wrappers
     * @return {@code true} if assignment possible
     */
    public static boolean isAssignable(Class<?> cls, final Class<?> toClass, final boolean autoboxing) {
        if (toClass == null) {
            return false;
        }
        // have to check for null, as isAssignableFrom doesn't
        if (cls == null) {
            return !toClass.isPrimitive();
        }
        // autoboxing:
        if (autoboxing) {
            if (cls.isPrimitive() && !toClass.isPrimitive()) {
                cls = primitiveToWrapper(cls);  //NOPMD
                if (cls == null) {
                    return false;
                }
            }
            if (toClass.isPrimitive() && !cls.isPrimitive()) {
                cls = wrapperToPrimitive(cls);
                if (cls == null) {
                    return false;
                }
            }
        }
        if (cls.equals(toClass)) {
            return true;
        }
        if (cls.isPrimitive()) {
            if (!toClass.isPrimitive()) {
                return false;
            }
            if (Integer.TYPE.equals(cls)) {
                return Long.TYPE.equals(toClass) || Float.TYPE.equals(toClass) || Double.TYPE.equals(toClass);
            }
            if (Long.TYPE.equals(cls)) {
                return Float.TYPE.equals(toClass) || Double.TYPE.equals(toClass);
            }
            if (Boolean.TYPE.equals(cls)) {
                return false;
            }
            if (Double.TYPE.equals(cls)) {
                return false;
            }
            if (Float.TYPE.equals(cls)) {
                return Double.TYPE.equals(toClass);
            }
            if (Character.TYPE.equals(cls) || Short.TYPE.equals(cls)) {
                return Integer.TYPE.equals(toClass) || Long.TYPE.equals(toClass) || Float.TYPE.equals(toClass) ||
                    Double.TYPE.equals(toClass);
            }
            if (Byte.TYPE.equals(cls)) {
                return Short.TYPE.equals(toClass) || Integer.TYPE.equals(toClass) || Long.TYPE.equals(toClass) ||
                    Float.TYPE.equals(toClass) || Double.TYPE.equals(toClass);
            }
            // should never get here
            return false;
        }
        return toClass.isAssignableFrom(cls);
    }

    /**
     * Converts the specified primitive Class object to its corresponding wrapper Class object.
     *
     * <p>
     * NOTE: From v2.2, this method handles {@code Void.TYPE}, returning {@code Void.TYPE}.
     * </p>
     *
     * @param cls the class to convert, may be null
     * @return the wrapper class for {@code cls} or {@code cls} if {@code cls} is not a primitive. {@code null} if null input.
     * @since 2.1
     */
    public static Class<?> primitiveToWrapper(final Class<?> cls) {
        Class<?> convertedClass = cls;
        if (cls != null && cls.isPrimitive()) {
            convertedClass = primitiveWrapperMap.get(cls);
        }
        return convertedClass;
    }

    /**
     * Converts the specified wrapper class to its corresponding primitive class.
     *
     * <p>
     * This method is the counter part of {@code primitiveToWrapper()}. If the passed in class is a wrapper class for a
     * primitive type, this primitive type will be returned (e.g. {@code Integer.TYPE} for {@code Integer.class}). For other
     * classes, or if the parameter is <b>null</b>, the return value is <b>null</b>.
     * </p>
     *
     * @param cls the class to convert, may be <b>null</b>
     * @return the corresponding primitive type if {@code cls} is a wrapper class, <b>null</b> otherwise
     * @see #primitiveToWrapper(Class)
     * @since 2.4
     */
    public static Class<?> wrapperToPrimitive(final Class<?> cls) {
        return wrapperPrimitiveMap.get(cls);
    }

    /**
     * Returns whether the given {@code type} is a primitive or primitive wrapper ({@link Boolean}, {@link Byte},
     * {@link Character}, {@link Short}, {@link Integer}, {@link Long}, {@link Double}, {@link Float}).
     *
     * @param type The class to query or null.
     * @return true if the given {@code type} is a primitive or primitive wrapper ({@link Boolean}, {@link Byte},
     *     {@link Character}, {@link Short}, {@link Integer}, {@link Long}, {@link Double}, {@link Float}).
     * @since 3.1
     */
    public static boolean isPrimitiveOrWrapper(final Class<?> type) {
        if (type == null) {
            return false;
        }
        return type.isPrimitive() || isPrimitiveWrapper(type);
    }

    /**
     * Returns whether the given {@code type} is a primitive wrapper ({@link Boolean}, {@link Byte}, {@link Character},
     * {@link Short}, {@link Integer}, {@link Long}, {@link Double}, {@link Float}).
     *
     * @param type The class to query or null.
     * @return true if the given {@code type} is a primitive wrapper ({@link Boolean}, {@link Byte}, {@link Character},
     *     {@link Short}, {@link Integer}, {@link Long}, {@link Double}, {@link Float}).
     * @since 3.1
     */
    public static boolean isPrimitiveWrapper(final Class<?> type) {
        return wrapperPrimitiveMap.containsKey(type);
    }

    @SuppressWarnings("unchecked")
    public static <T> Pair<Boolean, T> isOptionalAndDefault(final Class<?> type) {
        if (Optional.class.isAssignableFrom(type)) {
            return new Pair<>(true, (T) Optional.empty());
        } else if (OptionalInt.class.isAssignableFrom(type)) {
            return new Pair<>(true, (T) OptionalInt.empty());
        } else if (OptionalLong.class.isAssignableFrom(type)) {
            return new Pair<>(true, (T) OptionalLong.empty());
        } else if (OptionalDouble.class.isAssignableFrom(type)) {
            return new Pair<>(true, (T) OptionalDouble.empty());
        } else {
            return new Pair<>(false, null);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getDefaultValue(Class<T> clazz) {
        return (T) Array.get(Array.newInstance(clazz, 1), 0);
    }

}

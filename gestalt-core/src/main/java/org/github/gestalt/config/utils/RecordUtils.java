package org.github.gestalt.config.utils;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Comparator;

import static java.lang.invoke.MethodType.methodType;

/**
 * Large parts of this code is borrowed from
 * https://github.com/FrauBoes/record-s11n-util/blob/main/src/invoke/InvokeUtils.java
 *
 * <p>Utility methods for record serialization, using MethodHandles.
 *
 * @author <a href="mailto:colin.redmond@outlook.com">Colin Redmond (c) 2023.
 */
public final class RecordUtils {

    private static final MethodHandle MH_IS_RECORD;
    private static final MethodHandle MH_GET_RECORD_COMPONENTS;
    private static final MethodHandle MH_GET_NAME;
    private static final MethodHandle MH_GET_TYPE;
    private static final MethodHandle MH_GET_GENERIC_TYPE;

    private static final MethodHandle MH_GET_DECLARED_ANNOTATION;

    private static final MethodHandle MH_GET_ACCESSOR;
    private static final MethodHandles.Lookup LOOKUP;

    static {
        MethodHandle MH_isRecord;
        MethodHandle MH_getRecordComponents;
        MethodHandle MH_getName;
        MethodHandle MH_getType;
        MethodHandle MH_getGenericType;
        MethodHandle MH_getDeclaredAnnotation;
        MethodHandle MH_getAccessor;
        LOOKUP = MethodHandles.lookup();

        try {
            // reflective machinery required to access the record components
            // without a static dependency on Java SE 14 APIs
            Class<?> c = Class.forName("java.lang.reflect.RecordComponent");
            MH_isRecord = LOOKUP.findVirtual(Class.class, "isRecord", methodType(boolean.class));
            MH_getRecordComponents = LOOKUP.findVirtual(Class.class, "getRecordComponents",
                                               methodType(Array.newInstance(c, 0).getClass()))
                                           .asType(methodType(Object[].class, Class.class));
            MH_getName = LOOKUP.findVirtual(c, "getName", methodType(String.class))
                               .asType(methodType(String.class, Object.class));
            MH_getType = LOOKUP.findVirtual(c, "getType", methodType(Class.class))
                               .asType(methodType(Class.class, Object.class));
            MH_getGenericType = LOOKUP.findVirtual(c, "getGenericType", methodType(Type.class))
                                      .asType(methodType(Type.class, Object.class));
            MH_getDeclaredAnnotation = LOOKUP.findVirtual(c, "getDeclaredAnnotations", methodType(Annotation[].class))
                                     .asType(methodType(Annotation[].class, Object.class));
            MH_getAccessor = LOOKUP.findVirtual(c, "getAccessor", methodType(Method.class))
                                   .asType(methodType(Method.class, Object.class));
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            // pre-Java-14
            MH_isRecord = null;
            MH_getRecordComponents = null;
            MH_getName = null;
            MH_getType = null;
            MH_getGenericType = null;
            MH_getDeclaredAnnotation = null;
            MH_getAccessor = null;
        } catch (IllegalAccessException unexpected) {
            throw new AssertionError(unexpected);
        }

        MH_IS_RECORD = MH_isRecord;
        MH_GET_RECORD_COMPONENTS = MH_getRecordComponents;
        MH_GET_NAME = MH_getName;
        MH_GET_TYPE = MH_getType;
        MH_GET_GENERIC_TYPE = MH_getGenericType;
        MH_GET_DECLARED_ANNOTATION = MH_getDeclaredAnnotation;
        MH_GET_ACCESSOR = MH_getAccessor;
    }

    private RecordUtils() {

    }

    /**
     * Returns true if, and only if, the given class is a record class.
     *
     * @param type class type
     * @return if this class is a record
     */
    public static boolean isRecord(Class<?> type) {
        try {
            return MH_IS_RECORD != null && (boolean) MH_IS_RECORD.invokeExact(type);
        } catch (Throwable t) {
            throw new RuntimeException("Could not determine type (" + type + ")", t);
        }
    }

    /**
     * Returns an ordered array of the record components for the given record
     * class. The order is imposed by the given comparator. If the given
     * comparator is null, the order is that of the record components in the
     * record attribute of the class file.
     *
     * @param <T> This is the type of the record
     * @param type class type
     * @param comparator how to sort the records
     * @return the record components
     */
    public static <T> RecComponent[] recordComponents(Class<T> type,
                                                      Comparator<RecComponent> comparator) {
        try {
            Object[] rawComponents = (Object[]) MH_GET_RECORD_COMPONENTS.invokeExact(type);
            RecComponent[] recordComponents = new RecComponent[rawComponents.length];
            for (int i = 0; i < rawComponents.length; i++) {
                final Object comp = rawComponents[i];
                recordComponents[i] = new RecComponent(
                    (String) MH_GET_NAME.invokeExact(comp),
                    (Type) MH_GET_GENERIC_TYPE.invokeExact(comp),
                    (Class<?>) MH_GET_TYPE.invokeExact(comp),
                    (Annotation[]) MH_GET_DECLARED_ANNOTATION.invokeExact(comp),
                    (Method) MH_GET_ACCESSOR.invokeExact(comp), i);
            }
            if (comparator != null) {
                Arrays.sort(recordComponents, comparator);
            }
            return recordComponents;
        } catch (Throwable t) {
            throw new RuntimeException("Could not retrieve record components (" + type.getName() + ")", t);
        }
    }

    /**
     * Retrieves the value of the record component for the given record object.
     *
     * @param recordObject record object to get its values
     * @param recordComponent components or records
     * @return record components
     */
    public static Object componentValue(Object recordObject,
                                        RecComponent recordComponent) {
        try {
            MethodHandle MH_get = LOOKUP.findVirtual(recordObject.getClass(),
                recordComponent.name(),
                methodType(recordComponent.type()));
            return (Object) MH_get.invoke(recordObject);
        } catch (Throwable t) {
            throw new RuntimeException("Could not retrieve record components (" + recordObject.getClass().getName() + ")", t);
        }
    }


    /**
     * Invokes the canonical constructor of a record class with the
     * given argument values.
     *
     * @param <T> This is the type of the record
     * @param recordType type of record
     * @param recordComponents record components
     * @param args objects used to create the  record
     * @return the record created
     */
    public static <T> T invokeCanonicalConstructor(Class<T> recordType,
                                                   RecComponent[] recordComponents,
                                                   Object[] args) {
        try {
            Class<?>[] paramTypes = Arrays.stream(recordComponents)
                                          .map(RecComponent::type)
                                          .toArray(Class<?>[]::new);
            MethodHandle MH_canonicalConstructor =
                LOOKUP.findConstructor(recordType, methodType(void.class, paramTypes))
                      .asType(methodType(Object.class, paramTypes));
            return (T) MH_canonicalConstructor.invokeWithArguments(args);
        } catch (Throwable t) {
            throw new RuntimeException("Could not construct type (" + recordType.getName() + ")", t);
        }
    }


}

package org.github.gestalt.config.utils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static java.lang.invoke.MethodType.methodType;

public final class SealedClassUtil {
    private static final MethodHandle MH_IS_MH_IS_SEALED;

    private static final MethodHandle MH_GET_PERMITTED_SUB_CLASS;
    private static final MethodHandles.Lookup LOOKUP;

    static {
        MethodHandle MH_isSealed;
        MethodHandle MH_getPermittedSubclasses;
        LOOKUP = MethodHandles.lookup();

        try {
            // reflective machinery required to access the record components
            // without a static dependency on Java SE 14 APIs
            MH_isSealed = LOOKUP.findVirtual(Class.class, "isSealed", methodType(boolean.class));
            MH_getPermittedSubclasses = LOOKUP.findVirtual(Class.class, "getPermittedSubclasses",
                    methodType(Class[].class));
        } catch (NoSuchMethodException e) {
            // pre-Java-15
            MH_isSealed = null;
            MH_getPermittedSubclasses = null;
        } catch (IllegalAccessException unexpected) {
            throw new AssertionError(unexpected);
        }

        MH_IS_MH_IS_SEALED = MH_isSealed;
        MH_GET_PERMITTED_SUB_CLASS = MH_getPermittedSubclasses;
    }

    private SealedClassUtil() {

    }

    /**
     * Returns true if, and only if, the given class is a sealed class.
     *
     * @param type class type
     * @return if this class is a sealed
     */
    public static boolean isSealed(Class<?> type) {
        try {
            return MH_IS_MH_IS_SEALED != null && (boolean) MH_IS_MH_IS_SEALED.invokeExact(type);
        } catch (Throwable t) {
            throw new RuntimeException("Could not determine type (" + type + ")", t);
        }
    }

    /**
     * Get permitted subclasses of a sealed class.
     *
     * @param type class type
     * @return permitted subclasses
     */
    public static Class[] getPermittedSubclasses(Class<?> type) {
        try {
            if (MH_GET_PERMITTED_SUB_CLASS == null) {
                return new Class[0];
            }
            Class<?>[] permitted = (Class<?>[]) MH_GET_PERMITTED_SUB_CLASS.invokeExact(type);
            if (permitted == null) {
                return new Class[0];
            }

            return permitted;
        } catch (Throwable t) {
            throw new RuntimeException("Could not get permitted subclasses for (" + type + ")", t);
        }
    }
}


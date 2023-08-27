package org.github.gestalt.config.utils;

import org.github.gestalt.config.Gestalt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ClassUtilsTest {

    @Test
    void isAssignable() {
        Assertions.assertFalse(ClassUtils.isAssignable(Integer.class, null));
        Assertions.assertTrue(ClassUtils.isAssignable(null, Integer.class));
        Assertions.assertFalse(ClassUtils.isAssignable(int.class, null));
        Assertions.assertFalse(ClassUtils.isAssignable(null, int.class));
        Assertions.assertFalse(ClassUtils.isAssignable(Integer.class, String.class));
        Assertions.assertTrue(ClassUtils.isAssignable(Integer.class, Number.class));
        Assertions.assertTrue(ClassUtils.isAssignable(Integer.class, Integer.class));
        Assertions.assertTrue(ClassUtils.isAssignable(Integer.class, int.class));
        Assertions.assertTrue(ClassUtils.isAssignable(Integer.class, long.class));
        Assertions.assertTrue(ClassUtils.isAssignable(Integer.class, float.class));
        Assertions.assertTrue(ClassUtils.isAssignable(Long.class, float.class));
        Assertions.assertTrue(ClassUtils.isAssignable(Long.class, double.class));
        Assertions.assertTrue(ClassUtils.isAssignable(Float.class, double.class));
        Assertions.assertFalse(ClassUtils.isAssignable(Float.class, int.class));
        Assertions.assertTrue(ClassUtils.isAssignable(Boolean.class, boolean.class));
        Assertions.assertTrue(ClassUtils.isAssignable(boolean.class, Boolean.class));
        Assertions.assertTrue(ClassUtils.isAssignable(Boolean.class, Boolean.class));
        Assertions.assertTrue(ClassUtils.isAssignable(boolean.class, boolean.class));
        Assertions.assertFalse(ClassUtils.isAssignable(Boolean.class, String.class));
        Assertions.assertFalse(ClassUtils.isAssignable(Boolean.class, int.class));
        Assertions.assertTrue(ClassUtils.isAssignable(Short.class, short.class));
        Assertions.assertTrue(ClassUtils.isAssignable(Double.class, double.class));
        Assertions.assertTrue(ClassUtils.isAssignable(Double.class, Double.class));
        Assertions.assertTrue(ClassUtils.isAssignable(double.class, Double.class));
        Assertions.assertTrue(ClassUtils.isAssignable(double.class, double.class));
        Assertions.assertFalse(ClassUtils.isAssignable(Double.class, int.class));
        Assertions.assertTrue(ClassUtils.isAssignable(Character.class, int.class));

        Assertions.assertTrue(ClassUtils.isAssignable(Character.class, double.class));
        Assertions.assertTrue(ClassUtils.isAssignable(Character.class, float.class));
        Assertions.assertTrue(ClassUtils.isAssignable(Character.class, long.class));
        Assertions.assertTrue(ClassUtils.isAssignable(Short.class, int.class));
        Assertions.assertTrue(ClassUtils.isAssignable(Character.class, char.class));
        Assertions.assertTrue(ClassUtils.isAssignable(Short.class, short.class));
        Assertions.assertTrue(ClassUtils.isAssignable(Byte.class, short.class));
        Assertions.assertTrue(ClassUtils.isAssignable(Byte.class, byte.class));
        Assertions.assertTrue(ClassUtils.isAssignable(Byte.class, float.class));
        Assertions.assertTrue(ClassUtils.isAssignable(Byte.class, long.class));
        Assertions.assertTrue(ClassUtils.isAssignable(Byte.class, int.class));
        Assertions.assertTrue(ClassUtils.isAssignable(Byte.class, double.class));
        Assertions.assertTrue(ClassUtils.isAssignable(int.class, Integer.class));
        Assertions.assertFalse(ClassUtils.isAssignable(int.class, Long.class));
        Assertions.assertFalse(ClassUtils.isAssignable(float.class, Double.class));
        Assertions.assertFalse(ClassUtils.isAssignable(int.class, Gestalt.class));
        Assertions.assertFalse(ClassUtils.isAssignable(Integer.class, Gestalt.class));
        Assertions.assertFalse(ClassUtils.isAssignable(Gestalt.class, int.class));
        Assertions.assertFalse(ClassUtils.isAssignable(Gestalt.class, Integer.class));

        Assertions.assertTrue(ClassUtils.isAssignable(int.class, long.class, false));
        Assertions.assertTrue(ClassUtils.isAssignable(int.class, float.class, false));
        Assertions.assertTrue(ClassUtils.isAssignable(int.class, double.class, false));

        Assertions.assertTrue(ClassUtils.isAssignable(long.class, float.class, false));
        Assertions.assertTrue(ClassUtils.isAssignable(long.class, double.class, false));

        Assertions.assertTrue(ClassUtils.isAssignable(char.class, int.class, false));
        Assertions.assertTrue(ClassUtils.isAssignable(char.class, long.class, false));
        Assertions.assertTrue(ClassUtils.isAssignable(char.class, float.class, false));
        Assertions.assertTrue(ClassUtils.isAssignable(char.class, double.class, false));

        Assertions.assertTrue(ClassUtils.isAssignable(byte.class, short.class, false));
        Assertions.assertTrue(ClassUtils.isAssignable(byte.class, int.class, false));
        Assertions.assertTrue(ClassUtils.isAssignable(byte.class, long.class, false));
        Assertions.assertTrue(ClassUtils.isAssignable(byte.class, float.class, false));
        Assertions.assertTrue(ClassUtils.isAssignable(byte.class, double.class, false));
        Assertions.assertFalse(ClassUtils.isAssignable(Integer.class, int.class, false));
    }

    @Test
    void isPrimitiveWrapper() {
        Assertions.assertTrue(ClassUtils.isPrimitiveOrWrapper(Integer.class));
        Assertions.assertTrue(ClassUtils.isPrimitiveOrWrapper(Double.class));
        Assertions.assertTrue(ClassUtils.isPrimitiveOrWrapper(int.class));
        Assertions.assertFalse(ClassUtils.isPrimitiveOrWrapper(String.class));
        Assertions.assertFalse(ClassUtils.isPrimitiveOrWrapper(null));
    }

    @Test
    void primitiveToWrapper() {
        Assertions.assertEquals(ClassUtils.primitiveToWrapper(int.class), Integer.class);
        Assertions.assertEquals(ClassUtils.primitiveToWrapper(long.class), Long.class);
        Assertions.assertEquals(ClassUtils.primitiveToWrapper(Integer.class), Integer.class);

        Assertions.assertNull(ClassUtils.primitiveToWrapper(null));
    }
}



package org.github.gestalt.config.reflect;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

class TypeCaptureTest {

    @Test
    void isAssignableFromList() {
        TypeCapture<List<String>> type = new TypeCapture<List<String>>() {
        };
        Assertions.assertTrue(type.hasParameter());
        Assertions.assertEquals("java.util.List<java.lang.String>", type.getName());
        Assertions.assertEquals(String.class, type.getFirstParameterType().type);
        Assertions.assertFalse(type.isAssignableFrom(Integer.class));
        Assertions.assertFalse(type.isAssignableFrom(Double.class));
        Assertions.assertTrue(type.isAssignableFrom(List.class));
    }

    @Test
    void isAssignableFromInteger() {
        TypeCapture<Integer> type = new TypeCapture<Integer>() {
        };
        Assertions.assertFalse(type.hasParameter());
        Assertions.assertNull(type.getFirstParameterType());
        Assertions.assertEquals("java.lang.Integer", type.getName());
        Assertions.assertTrue(type.isAssignableFrom(Integer.class));
        Assertions.assertFalse(type.isAssignableFrom(Double.class));
        Assertions.assertFalse(type.isAssignableFrom(List.class));
    }

    @Test
    void isAssignableFromDate() {
        TypeCapture<Date> type = new TypeCapture<Date>() {
        };
        Assertions.assertFalse(type.hasParameter());
        Assertions.assertNull(type.getFirstParameterType());
        Assertions.assertEquals("java.util.Date", type.getName());
        Assertions.assertTrue(type.isAssignableFrom(Date.class));
        Assertions.assertFalse(type.isAssignableFrom(Integer.class));
        Assertions.assertFalse(type.isAssignableFrom(List.class));

        type = TypeCapture.of(Date.class);
        Assertions.assertFalse(type.hasParameter());
        Assertions.assertNull(type.getFirstParameterType());
        Assertions.assertEquals("java.util.Date", type.getName());
        Assertions.assertTrue(type.isAssignableFrom(Date.class));
        Assertions.assertFalse(type.isAssignableFrom(Integer.class));
        Assertions.assertFalse(type.isAssignableFrom(List.class));
    }

    @Test
    void isAssignableFromHolder() {
        TypeCapture<Holder<Integer>> type = new TypeCapture<Holder<Integer>>() {
        };
        Assertions.assertEquals(Integer.class, type.getFirstParameterType().type);
        Assertions.assertEquals("org.github.gestalt.config.reflect.TypeCaptureTest$Holder<java.lang.Integer>",
            type.getName());
        Assertions.assertTrue(type.hasParameter());
        Assertions.assertTrue(type.isAssignableFrom(Holder.class));
        Assertions.assertFalse(type.isAssignableFrom(Integer.class));
        Assertions.assertFalse(type.isAssignableFrom(List.class));
    }

    @Test
    void isAssignableFromBaseClass() {
        TypeCapture<BaseClass> type = new TypeCapture<BaseClass>() {
        };
        Assertions.assertFalse(type.hasParameter());
        Assertions.assertEquals("org.github.gestalt.config.reflect.TypeCaptureTest$BaseClass", type.getName());

        Assertions.assertNull(type.getFirstParameterType());
        Assertions.assertTrue(type.isAssignableFrom(BaseClass.class));
        Assertions.assertTrue(type.isAssignableFrom(InheritedClass.class));
        Assertions.assertFalse(type.isAssignableFrom(Integer.class));
        Assertions.assertFalse(type.isAssignableFrom(List.class));
    }

    @Test
    void isAssignableFromInheritedClass() {
        TypeCapture<InheritedClass> type = new TypeCapture<InheritedClass>() {
        };
        Assertions.assertFalse(type.hasParameter());
        Assertions.assertEquals("org.github.gestalt.config.reflect.TypeCaptureTest$InheritedClass", type.getName());

        Assertions.assertNull(type.getFirstParameterType());
        Assertions.assertFalse(type.isAssignableFrom(BaseClass.class));
        Assertions.assertTrue(type.isAssignableFrom(InheritedClass.class));
        Assertions.assertFalse(type.isAssignableFrom(Integer.class));
        Assertions.assertFalse(type.isAssignableFrom(List.class));
    }

    @Test
    void isAssignableArray() {
        TypeCapture<Integer[]> type = new TypeCapture<Integer[]>() {
        };
        Assertions.assertNull(type.getFirstParameterType());
        Assertions.assertEquals("java.lang.Integer[]", type.getName());
        Assertions.assertFalse(type.hasParameter());
        Assertions.assertFalse(type.isAssignableFrom(Holder.class));
        Assertions.assertFalse(type.isAssignableFrom(Integer.class));
        Assertions.assertFalse(type.isAssignableFrom(List.class));
        Assertions.assertTrue(type.isAssignableFrom(Integer[].class));

        type = TypeCapture.of(Integer[].class);
        Assertions.assertNull(type.getFirstParameterType());
        Assertions.assertEquals("java.lang.Integer[]", type.getName());
        Assertions.assertFalse(type.hasParameter());
        Assertions.assertFalse(type.isAssignableFrom(Holder.class));
        Assertions.assertFalse(type.isAssignableFrom(Integer.class));
        Assertions.assertFalse(type.isAssignableFrom(List.class));
        Assertions.assertTrue(type.isAssignableFrom(Integer[].class));
    }

    @Test
    void isAssignableGenericArray() {
        TypeCapture type = new TypeCapture<Object[]>() {
        };
        Assertions.assertNull(type.getFirstParameterType());
        Assertions.assertEquals("java.lang.Object[]", type.getName());
        Assertions.assertFalse(type.hasParameter());
        Assertions.assertFalse(type.isAssignableFrom(Holder.class));
        Assertions.assertFalse(type.isAssignableFrom(Integer.class));
        Assertions.assertFalse(type.isAssignableFrom(List.class));
        Assertions.assertTrue(type.isAssignableFrom(Integer[].class));

        type = TypeCapture.of(Object[].class);
        Assertions.assertNull(type.getFirstParameterType());
        Assertions.assertEquals("java.lang.Object[]", type.getName());
        Assertions.assertFalse(type.hasParameter());
        Assertions.assertFalse(type.isAssignableFrom(Holder.class));
        Assertions.assertFalse(type.isAssignableFrom(Integer.class));
        Assertions.assertFalse(type.isAssignableFrom(List.class));
        Assertions.assertTrue(type.isAssignableFrom(Integer[].class));
    }

    @Test
    void isAssignableGenericArray2() {
        TypeCapture type = new TypeCapture<List<Object>>() {
        };
        Assertions.assertEquals(Object.class, type.getFirstParameterType().type);
        Assertions.assertTrue(type.hasParameter());
        Assertions.assertFalse(type.isAssignableFrom(Holder.class));
        Assertions.assertFalse(type.isAssignableFrom(Integer.class));
        Assertions.assertTrue(type.isAssignableFrom(List.class));
        Assertions.assertFalse(type.isAssignableFrom(Integer[].class));
    }

    public static class Holder<T> {
        public T value;
    }

    public static class BaseClass {
        public String data1 = "100";
    }

    public static class InheritedClass extends BaseClass {
        public Integer data2 = 100;
    }

}

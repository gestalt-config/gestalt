package org.github.gestalt.config.utils;

import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class RecComponentTest {

    @Test
    void testRecComponentConstructorAndGetters() throws Exception {
        String name = "testName";
        Type type = mock(Type.class);
        Class<?> klass = String.class;
        Annotation[] annotations = new Annotation[0];
        Method accessor = String.class.getMethod("length");
        int index = 1;

        RecComponent component = new RecComponent(name, type, klass, annotations, accessor, index);

        assertEquals(name, component.name());
        assertEquals(type, component.typeGeneric());
        assertEquals(klass, component.type());
        assertEquals(index, component.index());
        assertArrayEquals(annotations, component.getDeclaredAnnotations());
        assertEquals(accessor, component.getAccessor());
    }

    @Test
    void testRecComponentWithNullValues() {
        RecComponent component = new RecComponent(null, null, null, null, null, 0);

        assertNull(component.name());
        assertNull(component.typeGeneric());
        assertNull(component.type());
        assertEquals(0, component.index());
        assertNull(component.getDeclaredAnnotations());
        assertNull(component.getAccessor());
    }
}

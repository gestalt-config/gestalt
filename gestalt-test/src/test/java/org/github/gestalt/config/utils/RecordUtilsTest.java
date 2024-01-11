package org.github.gestalt.config.utils;

import org.github.gestalt.config.annotations.Config;
import org.github.gestalt.config.test.classes.DBPool;
import org.github.gestalt.config.test.classes.Person;
import org.github.gestalt.config.test.classes.Person3;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Comparator;

class RecordUtilsTest {

    @Test
    void isRecord() {
        Assertions.assertFalse(RecordUtils.isRecord(DBPool.class));
        Assertions.assertFalse(RecordUtils.isRecord(Integer.class));
        Assertions.assertTrue(RecordUtils.isRecord(Person.class));
    }

    @Test
    void recordComponents() {
        RecComponent[] recComponents = RecordUtils.recordComponents(Person.class, Comparator.comparing(RecComponent::name));

        Assertions.assertEquals(2, recComponents.length);
        Assertions.assertEquals(1, recComponents[0].index());
        Assertions.assertEquals("id", recComponents[0].name());
        Assertions.assertEquals(Integer.class, recComponents[0].typeGeneric());
        Assertions.assertEquals(0, recComponents[1].index());
        Assertions.assertEquals("name", recComponents[1].name());
    }

    @Test
    void componentValue() {
        RecComponent[] recComponents = RecordUtils.recordComponents(Person.class, Comparator.comparing(RecComponent::name));
        Assertions.assertEquals("id", recComponents[0].name());

        Object value = RecordUtils.componentValue(new Person("Tim", 52), recComponents[0]);
        Assertions.assertInstanceOf(Integer.class, value);
        Assertions.assertEquals(52, (Integer) value);
        Assertions.assertEquals(String.class, recComponents[1].typeGeneric());
    }

    @Test
    void componentAnnotations() {
        RecComponent[] recComponents = RecordUtils.recordComponents(Person3.class, Comparator.comparing(RecComponent::name));
        Assertions.assertEquals("id", recComponents[0].name());

        Object value = RecordUtils.componentValue(new Person3(52, "Tim"), recComponents[0]);
        Assertions.assertInstanceOf(Integer.class, value);
        Assertions.assertEquals(52, (Integer) value);
        Assertions.assertEquals(String.class, recComponents[1].typeGeneric());
        Assertions.assertNotNull(recComponents[0].getAccessor().getAnnotation(Config.class));
        Assertions.assertEquals("test", recComponents[0].getAccessor().getAnnotation(Config.class).path());
    }

    @Test
    void invokeCanonicalConstructor() {
        RecComponent[] recComponents = new RecComponent[2];

        recComponents[0] = new RecComponent("name", String.class, String.class, null, null, 0);
        recComponents[1] = new RecComponent("id", Integer.class, Integer.class, null, null, 1);

        Object[] values = new Object[2];
        values[0] = "tim";
        values[1] = 52;
        Person tim = RecordUtils.invokeCanonicalConstructor(Person.class, recComponents, values);

        Assertions.assertEquals("tim", tim.name());
        Assertions.assertEquals(52, tim.id());
    }
}

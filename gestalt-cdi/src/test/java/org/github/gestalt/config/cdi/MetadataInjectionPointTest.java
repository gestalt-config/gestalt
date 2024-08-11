package org.github.gestalt.config.cdi;

import jakarta.enterprise.inject.spi.InjectionPoint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MetadataInjectionPointTest {

    @Test
    void getType() {
        MetadataInjectionPoint metadataInjectionPoint = new MetadataInjectionPoint();
        Assertions.assertEquals(InjectionPoint.class, metadataInjectionPoint.getType());
    }

    @Test
    void getQualifiers() {
        MetadataInjectionPoint metadataInjectionPoint = new MetadataInjectionPoint();
        Assertions.assertEquals(1, metadataInjectionPoint.getQualifiers().size());
    }

    @Test
    void getBean() {
        MetadataInjectionPoint metadataInjectionPoint = new MetadataInjectionPoint();
        Assertions.assertNull(metadataInjectionPoint.getBean());
    }

    @Test
    void getMember() {
        MetadataInjectionPoint metadataInjectionPoint = new MetadataInjectionPoint();
        Assertions.assertNull(metadataInjectionPoint.getMember());
    }

    @Test
    void getAnnotated() {
        MetadataInjectionPoint metadataInjectionPoint = new MetadataInjectionPoint();
        Assertions.assertNull(metadataInjectionPoint.getAnnotated());
    }

    @Test
    void isDelegate() {
        MetadataInjectionPoint metadataInjectionPoint = new MetadataInjectionPoint();
        Assertions.assertFalse(metadataInjectionPoint.isDelegate());
    }

    @Test
    void isTransient() {
        MetadataInjectionPoint metadataInjectionPoint = new MetadataInjectionPoint();
        Assertions.assertFalse(metadataInjectionPoint.isTransient());
    }
}

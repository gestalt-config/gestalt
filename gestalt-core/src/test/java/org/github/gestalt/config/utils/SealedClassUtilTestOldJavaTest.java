package org.github.gestalt.config.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SealedClassUtilTestOldJavaTest {

    @Test
    void isSealed() {
        Assertions.assertFalse(SealedClassUtil.isSealed(Boolean.class));
    }

    @Test
    void getPermittedSubclassesNonSealed() {
        Class<?>[] retrieved = SealedClassUtil.getPermittedSubclasses(Boolean.class);

        Assertions.assertTrue(retrieved.length == 0);
    }
}

package org.github.gestalt.config.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TripleTest {

    @Test
    void getFirst() {
        Triple<Integer, Long, String> triple = new Triple<>(1, 2l, "test");
        Assertions.assertEquals(1, triple.getFirst());
        Assertions.assertEquals(2l, triple.getSecond());
        Assertions.assertEquals("test", triple.getThird());
    }

    @Test
    void testToString() {
        Triple<Integer, Long, String> triple = new Triple<>(1, 2l, "test");
        Assertions.assertEquals("Pair{first=1, second=2, third=test}", triple.toString());
    }

    @Test
    void testEquals() {
        Triple<Integer, Long, String> triple = new Triple<>(1, 2l, "test");
        Triple<Integer, Long, String> triple2 = new Triple<>(2, 3l, "abc");
        Assertions.assertEquals(triple, triple);
        Assertions.assertNotEquals(triple, triple2);
        Assertions.assertNotEquals(Long.valueOf(12l), triple2);
    }

    @Test
    void testHashCode() {
        Triple<Integer, Long, String> triple = new Triple<>(1, 2l, "test");
        Assertions.assertEquals(3557521l, triple.hashCode());
    }
}

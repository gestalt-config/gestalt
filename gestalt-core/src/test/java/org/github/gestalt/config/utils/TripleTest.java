package org.github.gestalt.config.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TripleTest {

    @Test
    void getFirst() {
        Triple<Integer, Long, String> triple = new Triple<>(1, 2L, "test");
        Assertions.assertEquals(1, triple.getFirst());
        Assertions.assertEquals(2L, triple.getSecond());
        Assertions.assertEquals("test", triple.getThird());
    }

    @Test
    void testToString() {
        Triple<Integer, Long, String> triple = new Triple<>(1, 2L, "test");
        Assertions.assertEquals("Pair{first=1, second=2, third=test}", triple.toString());
    }

    @Test
    void testEquals() {
        Triple<Integer, Long, String> triple = new Triple<>(1, 2L, "test");
        Triple<Integer, Long, String> triple2 = new Triple<>(2, 3L, "abc");
        Triple<Integer, Long, String> triple3 = new Triple<>(1, 2L, "abc");
        Triple<Integer, Long, String> triple4 = new Triple<>(1, 3L, "abc");
        Assertions.assertEquals(triple, triple);
        Assertions.assertNotEquals(triple, triple2);
        Assertions.assertNotEquals(triple2, Long.valueOf(12L));
        Assertions.assertNotEquals(triple, triple3);
        Assertions.assertNotEquals(triple, triple4);
    }

    @Test
    void testHashCode() {
        Triple<Integer, Long, String> triple = new Triple<>(1, 2L, "test");
        Assertions.assertEquals(3557521L, triple.hashCode());

        Triple<Integer, Long, String> triple2 = new Triple<>(1, null, null);
        Assertions.assertEquals(961, triple2.hashCode());

        Triple<Integer, Long, String> triple3 = new Triple<>(null, 1L, null);
        Assertions.assertEquals(31, triple3.hashCode());
    }
}

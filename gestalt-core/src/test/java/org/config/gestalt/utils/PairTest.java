package org.config.gestalt.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;

class PairTest {

    @Test
    void getFirst() {
        Pair<Integer, String> pair = new Pair<>(1, "one");
        Assertions.assertEquals(1, pair.getFirst());
    }

    @Test
    void getSecond() {
        Pair<Integer, String> pair = new Pair<>(1, "one");
        Assertions.assertEquals("one", pair.getSecond());
    }

    @Test
    void testToString() {
        Pair<Integer, String> pair = new Pair<>(1, "one");
        Assertions.assertEquals("Pair{first=1, second=one}", pair.toString());
    }

    @Test
    void testEquals() {
        Pair<Integer, String> pair = new Pair<>(1, "one");
        Pair<Integer, String> pair2 = new Pair<>(2, "one");
        Pair<Integer, String> pairEquals = new Pair<>(1, "one");

        Assertions.assertEquals(pair, pairEquals);
        Assertions.assertNotEquals(pair, pair2);
        Assertions.assertNotEquals(pair, "hello");
    }

    @Test
    void testHashCode() {
        Pair<Integer, String> pair = new Pair<>(1, "one");
        Assertions.assertEquals(Objects.hash(1, "one"), pair.hashCode());
    }
}

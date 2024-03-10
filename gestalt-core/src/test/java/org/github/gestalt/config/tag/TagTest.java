package org.github.gestalt.config.tag;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TagTest {

    @Test
    void ofTest() {
        Tag tag = Tag.of("toy", "ball");
        Assertions.assertEquals("toy", tag.getKey());
        Assertions.assertEquals("ball", tag.getValue());
    }

    @Test
    void notNull() {
        Assertions.assertThrows(NullPointerException.class, () -> Tag.of("toy", null));
    }

    @Test
    void testEquals() {
        Tag tag = Tag.of("toy", "ball");
        Tag tag2 = Tag.of("toy", "ball");
        Tag tag3 = Tag.of("toy", "bat");
        Tag tag4 = Tag.of("car", "vw");

        Assertions.assertEquals(tag, tag);
        Assertions.assertEquals(tag, tag2);
        Assertions.assertNotEquals(tag, tag3);
        Assertions.assertNotEquals(tag, tag4);
        Assertions.assertNotEquals(tag, 10);
    }

    @Test
    void testHashCode() {
        Tag tag = Tag.of("toy", "ball");
        Tag tag2 = Tag.of("toy", "ball");
        Tag tag3 = Tag.of("toy", "bat");

        Assertions.assertNotEquals(12345, tag.hashCode());
        Assertions.assertEquals(tag.hashCode(), tag.hashCode());
        Assertions.assertEquals(tag.hashCode(), tag2.hashCode());
        Assertions.assertNotEquals(tag.hashCode(), tag3.hashCode());
    }

    @Test
    void testToString() {
        Tag tag = Tag.of("toy", "ball");
        Assertions.assertEquals("Tag{key='toy', value='ball'}", tag.toString());
    }
}

package org.github.gestalt.config.tag;

import org.github.gestalt.config.exceptions.GestaltException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;


class TagsTest {

    @Test
    void ofTag() {
        Tags tags = Tags.of(Tag.of("toy", "ball"), Tag.of("toy", "bat"));

        Assertions.assertTrue(tags.getTags().contains(Tag.of("toy", "ball")));
        Assertions.assertTrue(tags.getTags().contains(Tag.of("toy", "bat")));
    }

    @Test
    void ofTagDuplicateVarg() {
        Tags tags = Tags.of(Tag.of("toy", "ball"), Tag.of("toy", "bat"), Tag.of("toy", "ball"));

        Assertions.assertTrue(tags.getTags().contains(Tag.of("toy", "ball")));
        Assertions.assertTrue(tags.getTags().contains(Tag.of("toy", "bat")));
    }

    @Test
    void ofTagDuplicateList() {
        Tags tags = Tags.of(List.of(Tag.of("toy", "ball"), Tag.of("toy", "bat"), Tag.of("toy", "ball")));

        Assertions.assertTrue(tags.getTags().contains(Tag.of("toy", "ball")));
        Assertions.assertTrue(tags.getTags().contains(Tag.of("toy", "bat")));
    }

    @Test
    void testOfVargString() throws GestaltException {
        Tags tags = Tags.of("toy", "ball", "toy", "bat");

        Assertions.assertTrue(tags.getTags().contains(Tag.of("toy", "ball")));
        Assertions.assertTrue(tags.getTags().contains(Tag.of("toy", "bat")));
    }

    @Test
    void testOfVargStringOddNumbers() {
        Assertions.assertThrows(GestaltException.class, () -> Tags.of("toy", "ball", "toy"));
    }

    @Test
    void testOfList() {
        List<Tag> tagList = List.of(Tag.of("toy", "ball"), Tag.of("toy", "bat"));
        Tags tags = Tags.of(tagList);

        Assertions.assertTrue(tags.getTags().contains(Tag.of("toy", "ball")));
        Assertions.assertTrue(tags.getTags().contains(Tag.of("toy", "bat")));
    }

    @Test
    void testEquals() {
        List<Tag> tagList = List.of(Tag.of("toy", "ball"), Tag.of("toy", "bat"));
        Tags tags = Tags.of(tagList);

        Tags tags2 = Tags.of(Tag.of("toy", "ball"), Tag.of("toy", "bat"));
        Tags tags3 = Tags.of(Tag.of("toy", "ball"), Tag.of("toy", "car"));

        Assertions.assertEquals(tags, tags);
        Assertions.assertEquals(tags, tags2);
        Assertions.assertNotEquals(tags, tags3);
        Assertions.assertNotEquals(tags, 10);
    }

    @Test
    void testHashCode() {
        List<Tag> tagList = List.of(Tag.of("toy", "ball"), Tag.of("toy", "bat"));
        Tags tags = Tags.of(tagList);

        Tags tags2 = Tags.of(Tag.of("toy", "ball"), Tag.of("toy", "bat"));
        Tags tags3 = Tags.of(Tag.of("toy", "ball"), Tag.of("toy", "car"));

        Assertions.assertEquals(tags.hashCode(), tags.hashCode());
        Assertions.assertEquals(tags.hashCode(), tags2.hashCode());
        Assertions.assertNotEquals(tags.hashCode(), tags3.hashCode());
    }

    @Test
    void testToString() {
        List<Tag> tagList = List.of(Tag.of("toy", "ball"), Tag.of("toy", "bat"));
        Tags tags = Tags.of(tagList);

        Assertions.assertEquals("Tags{[Tag{key='toy', value='ball'}, Tag{key='toy', value='bat'}]}", tags.toString());
    }
}

package org.github.gestalt.config.tag;

import org.github.gestalt.config.exceptions.GestaltException;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Represents a collection of tags that can be applied to data.
 *
 * @author <a href="mailto:colin.redmond@outlook.com">Colin Redmond (c) 2023.
 */
public final class Tags {
    private final Set<Tag> tags;

    private static final Tags defaultTags = new Tags(Set.of());

    private Tags(Set<Tag> tags) {
        this.tags = new HashSet<>(tags);
    }

    /**
     * Create an empty collection of tags.
     *
     * @return Tags
     */
    public static Tags of() {                   //NOPMD
        return defaultTags;
    }

    /**
     * Create a tags from a list of tag.
     *
     * @param tags list of tag
     * @return Tags
     */
    public static Tags of(List<Tag> tags) {     //NOPMD
        return new Tags(new HashSet<>(tags));
    }

    /**
     * Create a tags from a varg of tags.
     *
     * @param tag varg of tag
     * @return Tags
     */
    public static Tags of(Tag... tag) {         //NOPMD
        return new Tags(Arrays.stream(tag).collect(Collectors.toSet()));
    }

    /**
     * Create a tags from a varg of strings. Must have pairs of key and values where it is key1, value1, key2, value2...
     *
     * @param tagsVarg string vargs
     * @return Tags
     * @throws GestaltException if there is a missmatch in key and value, where we have a key but no value.
     */
    public static Tags of(String... tagsVarg) throws GestaltException {  //NOPMD
        if (tagsVarg.length % 2 != 0) {
            throw new GestaltException("Tags must have come in pairs, received odd number of tags: " + tagsVarg.length);
        }

        int capacity = Math.floorDiv(tagsVarg.length, 2);
        Set<Tag> tags = new HashSet<>(capacity);
        for (int i = 0; i < tagsVarg.length; i = i + 2) {
            tags.add(Tag.of(tagsVarg[i], tagsVarg[i + 1]));
        }

        return new Tags(tags);
    }

    /**
     * Get the tags.
     *
     * @return tags
     */
    public Set<Tag> getTags() {
        return tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Tags)) {
            return false;
        }
        Tags tags1 = (Tags) o;
        return tags.equals(tags1.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tags);
    }
}

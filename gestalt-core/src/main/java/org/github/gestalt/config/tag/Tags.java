package org.github.gestalt.config.tag;

import org.github.gestalt.config.exceptions.GestaltException;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Represents a collection of tags that can be applied to data.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class Tags {
    private static final Tags DEFAULT_TAGS = new Tags(Set.of());
    private final Set<Tag> internalTags;

    private Tags(Set<Tag> tags) {
        this.internalTags = new HashSet<>(tags);
    }

    /**
     * Create an empty collection of tags.
     *
     * @return Tags
     */
    public static Tags of() {                   //NOPMD
        return DEFAULT_TAGS;
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
     * Create a tags from a set of tag.
     *
     * @param tags set of tag
     * @return Tags
     */
    public static Tags of(Set<Tag> tags) {     //NOPMD
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

    public static Tags of(Tags... tags) {         //NOPMD
        return new Tags(Arrays.stream(tags)
            .flatMap(it -> it.getTags().stream())
            .collect(Collectors.toSet()));
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

    public Tags and(Tag... tags) {
        if (blankVarargs(tags)) {
            return this;
        } else {
            Set<Tag> newTags = new HashSet<>(internalTags.size() + tags.length);
            newTags.addAll(internalTags);
            newTags.addAll(new HashSet<>(Arrays.asList(tags)));
            return new Tags(newTags);
        }
    }

    public Tags and(Tags tags) {
        if (tags == null || tags.getTags() == null || tags.getTags().isEmpty()) {
            return this;
        } else {
            Set<Tag> newTags = new HashSet<>(internalTags.size() + tags.getTags().size());
            newTags.addAll(internalTags);
            newTags.addAll(tags.getTags());
            return new Tags(newTags);
        }
    }

    public boolean isSubsetOf(Tags tags) {
        return tags.internalTags.containsAll(this.internalTags);
    }

    private static boolean blankVarargs(Tag[] tags) {
        return tags == null || tags.length == 0 || tags.length == 1 && tags[0] == null;
    }


    /**
     * Create and return a new tag of type profile with value.
     *
     * @param value the profile
     * @return tags with the profile
     */
    public static Tags profile(String value) {
        return of(Tag.of("profile", value));
    }

    /**
     * Create and return a new tag of type profile with value.
     *
     * @param value the profiles
     * @return tags with the profiles
     */
    public static Tags profiles(String... value) {
        if (value.length == 1) {
            return profile(value[0]);
        } else {
            return of(
                Arrays.stream(value)
                    .map(it -> Tag.of("profile", it))
                    .collect(Collectors.toList())
            );
        }
    }

    /**
     * Create and return a new tag of type environment with value.
     *
     * @param value the environment
     * @return tags with the environment
     */
    public static Tags environment(String value) {
        return of(Tag.of("environment", value));
    }

    /**
     * Create and return a new tag of type environment with value.
     *
     * @param value the environments
     * @return tags with the environments
     */
    public static Tags environments(String... value) {
        if (value.length == 1) {
            return environment(value[0]);
        } else {
            return of(
                Arrays.stream(value)
                    .map(it -> Tag.of("environment", it))
                    .collect(Collectors.toList())
            );
        }
    }

    /**
     * Get the tags.
     *
     * @return tags
     */
    public Set<Tag> getTags() {
        return internalTags;
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
        return internalTags.equals(tags1.internalTags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(internalTags);
    }

    @Override
    public String toString() {
        return "Tags{" + internalTags + '}';
    }
}

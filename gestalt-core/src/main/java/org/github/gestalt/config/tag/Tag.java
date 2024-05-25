package org.github.gestalt.config.tag;

import java.util.Objects;

/**
 * Represents a tag that can be applied to data.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class Tag {
    private final String key;
    private final String value;

    private Tag(String key, String value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);

        this.key = key;
        this.value = value;
    }

    /**
     * Create a tag from a key and value.
     *
     * @param key   the key for the tag
     * @param value the value of the tag
     * @return the new tag
     */
    public static Tag of(String key, String value) {        //NOPMD

        return new Tag(key, value);
    }

    /**
     * Create and return a new tag of type profile with value.
     *
     * @param value the profile
     * @return tags with the profile
     */
    public static Tag profile(String value) {
        return Tag.of("profile", value);
    }

    /**
     * Create and return a new tag of type environment with value.
     *
     * @param value the environment
     * @return tags with the environment
     */
    public static Tag environment(String value) {
        return Tag.of("environment", value);
    }

    /**
     * Get the key for the tag.
     *
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Get the value for the tag.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Tag)) {
            return false;
        }
        Tag tag = (Tag) o;
        return key.equals(tag.key) && value.equals(tag.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return "Tag{" +
            "key='" + key + '\'' +
            ", value='" + value + '\'' +
            '}';
    }
}

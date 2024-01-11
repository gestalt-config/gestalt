package org.github.gestalt.config.token;

import java.util.Objects;

/**
 * A Token for a Tag.
 * Not currently used.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class TagToken extends Token {
    private final String tag;
    private final String value;

    /**
     * constructor.
     *
     * @param tag tag name
     * @param value tag value
     */
    public TagToken(String tag, String value) {
        this.tag = tag;
        this.value = value;
    }

    /**
     * Get the tag name.
     *
     * @return tag name
     */
    public String getTag() {
        return tag;
    }

    /**
     * Get Tag value.
     *
     * @return Tag Value
     */
    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TagToken)) {
            return false;
        }
        TagToken tagToken = (TagToken) o;
        return Objects.equals(tag, tagToken.tag) &&
            Objects.equals(value, tagToken.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag, value);
    }
}

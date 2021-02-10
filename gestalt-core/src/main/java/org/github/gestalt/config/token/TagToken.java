package org.github.gestalt.config.token;

import java.util.Objects;

public class TagToken extends Token {
    private final String tag;
    private final String value;

    public TagToken(String tag, String value) {
        this.tag = tag;
        this.value = value;
    }

    public String getTag() {
        return tag;
    }

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

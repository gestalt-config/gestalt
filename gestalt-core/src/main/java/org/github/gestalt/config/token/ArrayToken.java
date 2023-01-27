package org.github.gestalt.config.token;

import java.util.Objects;

/**
 * Token for an array that has an index.
 * Must be a positive number
 *
 * @author <a href="mailto:colin.redmond@outlook.com">Colin Redmond (c) 2023.
 */
public class ArrayToken extends Token {
    private final int index;

    /**
     * Must be a positive number.
     *
     * @param index for the array token
     */
    public ArrayToken(int index) {
        this.index = index;
    }

    /**
     * array index for this token.
     *
     * @return index
     */
    public int getIndex() {
        return index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ArrayToken)) {
            return false;
        }
        ArrayToken that = (ArrayToken) o;
        return index == that.index;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index);
    }
}

package org.config.gestalt.token;

import java.util.Objects;

public class ArrayToken extends Token {
    private final int index;

    // a -1 one means that no array index was specified so place it in order of seen
    public ArrayToken(int index) {
        this.index = index;
    }

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

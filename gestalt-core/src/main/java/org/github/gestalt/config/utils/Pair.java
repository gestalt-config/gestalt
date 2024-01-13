package org.github.gestalt.config.utils;

import java.util.Objects;

/**
 * A simple class that holds a pair of values. There are many implementations of this but i didnt want to pull in
 * another library only for a simple class.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class Pair<A, B> {

    private final A first;
    private final B second;

    /**
     * Create a pair of values.
     *
     * @param first  first of pair
     * @param second second of pair
     */
    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Get the first of the pair.
     *
     * @return first of the pair
     */
    public A getFirst() {
        return first;
    }

    /**
     * Get the second of the pair.
     *
     * @return second of the pair
     */
    public B getSecond() {
        return second;
    }

    @Override
    public String toString() {
        return "Pair{" +
            "first=" + first +
            ", second=" + second +
            '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Pair)) {
            return false;
        }
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(first, pair.first) &&
            Objects.equals(second, pair.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }
}

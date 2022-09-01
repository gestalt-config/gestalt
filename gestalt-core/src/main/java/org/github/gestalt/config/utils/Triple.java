package org.github.gestalt.config.utils;

import java.util.Objects;

/**
 * A simple class that holds a pair of values. There are many implementations of this but i didnt want to pull in
 * another library only for a simple class.
 *
 * @author Colin Redmond
 */
public class Triple<A, B, C> {

    private final A first;
    private final B second;
    private final C third;

    /**
     * Create a pair of values.
     *
     * @param first first of pair
     * @param second second of pair
     */
    public Triple(A first, B second, C third) {
        this.first = first;
        this.second = second;
        this.third = third;
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

    public C getThird() {
        return third;
    }

    @Override
    public String toString() {
        return "Pair{" +
            "first=" + first +
            ", second=" + second +
            ", third=" + third +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Triple)) {
            return false;
        }

        Triple<?, ?, ?> triple = (Triple<?, ?, ?>) o;

        if (!Objects.equals(first, triple.first)) {
            return false;
        }
        if (!Objects.equals(second, triple.second)) {
            return false;
        }
        return Objects.equals(third, triple.third);
    }

    @Override
    public int hashCode() {
        int result = first != null ? first.hashCode() : 0;
        result = 31 * result + (second != null ? second.hashCode() : 0);
        result = 31 * result + (third != null ? third.hashCode() : 0);
        return result;
    }
}

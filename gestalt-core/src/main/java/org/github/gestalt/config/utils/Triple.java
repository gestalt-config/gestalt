package org.github.gestalt.config.utils;

import java.util.Objects;

/**
 * A simple class that holds a pair of values. There are many implementations of this but i didnt want to pull in
 * another library only for a simple class.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class Triple<A, B, C> {

    private final A first;
    private final B second;
    private final C third;

    /**
     * Create a pair of values.
     *
     * @param first  first of Triple
     * @param second second of Triple
     * @param third  third of a Triple
     */
    public Triple(A first, B second, C third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    /**
     * Get the first of the Triple.
     *
     * @return first of the Triple
     */
    public A getFirst() {
        return first;
    }

    /**
     * Get the second of the Triple.
     *
     * @return second of the Triple
     */
    public B getSecond() {
        return second;
    }

    /**
     * Get the third of the Triple.
     *
     * @return third of the Triple
     */
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

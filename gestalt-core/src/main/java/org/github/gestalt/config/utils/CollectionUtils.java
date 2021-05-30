package org.github.gestalt.config.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Utility class for collections.
 *
 * @author Colin Redmond
 */
public final class CollectionUtils {

    private CollectionUtils() {
    }

    /**
     * Returns a distinct list based on the value Extractor.
     *
     * @param valueExtractor function to extract the value we are looking for distinct.
     * @param <T> the type of the collection
     * @return the list of districts
     */
    public static <T> Predicate<T> distinctBy(Function<? super T, Object> valueExtractor) {
        Map<Object, Boolean> distinctMap = new ConcurrentHashMap<>();
        return t -> distinctMap.putIfAbsent(valueExtractor.apply(t), Boolean.TRUE) == null;
    }
}

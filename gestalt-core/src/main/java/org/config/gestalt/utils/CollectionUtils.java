package org.config.gestalt.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public final class CollectionUtils {

    private CollectionUtils() {
    }

    /**
     * Returns a distinct list based on the value Extractor.
     *
     * @param valueExtractor function to extract the value we are looking for distinct.
     */
    public static <T> Predicate<T> distinctBy(Function<? super T, Object> valueExtractor) {
        Map<Object, Boolean> distinctMap = new ConcurrentHashMap<>();
        return t -> distinctMap.putIfAbsent(valueExtractor.apply(t), Boolean.TRUE) == null;
    }
}

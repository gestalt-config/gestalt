package org.github.gestalt.config.utils;

import org.github.gestalt.config.annotations.ConfigPriority;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Utility class for collections.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
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

    /**
     * Sorts the list based on the annotation ConfigPriority.
     * if the object is missing the annotation it will not be included.
     *
     * @param configPriorityList list of config priorities
     * @param ascending if we should sort ascending
     * @param <T> generic type
     * @return list of ordered ConfigPriority based on value, if value is missing it will be omitted
     */
    public static <T> List<T> buildOrderedConfigPriorities(List<T> configPriorityList, boolean ascending) {
        return configPriorityList.stream()
                                 .filter(it -> it.getClass().isAnnotationPresent(ConfigPriority.class))
                                 .sorted((t1, t2) -> {
                                     ConfigPriority cp1 = t1.getClass().getAnnotation(ConfigPriority.class);
                                     int cp1Value = 0;
                                     if (cp1 != null) { // shouldn't be null as we check there is an annotation, but just in case
                                         cp1Value = cp1.value();
                                     }

                                     ConfigPriority cp2 = t2.getClass().getAnnotation(ConfigPriority.class);
                                     int cp2Value = 0;
                                     if (cp2 != null) { // shouldn't be null as we check there is an annotation, but just in case
                                         cp2Value = cp2.value();
                                     }

                                     return ascending ? cp1Value - cp2Value : cp2Value - cp1Value;
                                 }).collect(Collectors.toList());
    }
}

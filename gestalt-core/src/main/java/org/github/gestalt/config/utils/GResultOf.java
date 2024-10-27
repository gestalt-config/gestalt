package org.github.gestalt.config.utils;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.metadata.MetaDataValue;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Class that holds the results, errors or both.
 *
 * @param <T> type of validatedOf
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class GResultOf<T> {
    private final T results;
    private final List<ValidationError> errors;
    private final boolean isDefault;
    private final Map<String, List<MetaDataValue<?>>> metadata;

    private GResultOf(T results, List<ValidationError> errors, boolean isDefault, Map<String, List<MetaDataValue<?>>> metadata) {
        this.results = results;
        this.errors = Objects.requireNonNullElse(errors, Collections.emptyList());
        this.isDefault = isDefault;
        this.metadata = metadata;
    }

    /**
     * Create a GResultOf with a valid result.
     *
     * @param answer valid results
     * @param <T>    type of GResultOf
     * @return GResultOf
     */
    public static <T> GResultOf<T> result(T answer) {
        return new GResultOf<>(answer, Collections.emptyList(), false, Map.of());
    }

    /**
     * Create a GResultOf with a valid result.
     *
     * @param answer valid results
     * @param isDefault if this is a default value
     * @param <T>    type of GResultOf
     * @return GResultOf
     */
    public static <T> GResultOf<T> result(T answer, boolean isDefault) {
        return new GResultOf<>(answer, Collections.emptyList(), isDefault, Map.of());
    }

    /**
     * Create a GResultOf with errors.
     *
     * @param errors list of ValidationError
     * @param <T>    type of GResultOf
     * @return GResultOf
     */
    public static <T> GResultOf<T> errors(List<ValidationError> errors) {
        return new GResultOf<>(null, errors, false, Map.of());
    }

    /**
     * Create a GResultOf with a error.
     *
     * @param errors list of ValidationError
     * @param <T>    type of GResultOf
     * @return GResultOf
     */
    public static <T> GResultOf<T> errors(ValidationError errors) {
        return new GResultOf<>(null, Collections.singletonList(errors), false, Map.of());
    }

    /**
     * Create a GResultOf with a results and an error.
     *
     * @param answer valid results
     * @param errors list of ValidationError
     * @param <T>    type of GResultOf
     * @return GResultOf
     */
    public static <T> GResultOf<T> resultOf(T answer, ValidationError errors) {
        return new GResultOf<>(answer, List.of(errors), false, Map.of());
    }

    /**
     * Create a GResultOf with a results and an error.
     *
     * @param answer valid results
     * @param errors list of ValidationError
     * @param <T>    type of GResultOf
     * @return GResultOf
     */
    public static <T> GResultOf<T> resultOf(T answer, List<ValidationError> errors) {
        return new GResultOf<>(answer, errors, false, Map.of());
    }

    /**
     * Create a GResultOf with a results and an error.
     *
     * @param answer valid results
     * @param errors list of ValidationError
     * @param metadata metadata associated with the result
     * @param <T>    type of GResultOf
     * @return GResultOf
     */
    public static <T> GResultOf<T> resultOf(T answer, List<ValidationError> errors, Map<String, List<MetaDataValue<?>>> metadata) {
        return new GResultOf<>(answer, errors, false, metadata);
    }

    /**
     * Create a GResultOf with a results and an error.
     *
     * @param answer valid results
     * @param errors list of ValidationError
     * @param isDefault if this is a default result.
     * @param metadata metadata associated with the result.
     * @param <T>    type of GResultOf
     * @return GResultOf
     */
    public static <T> GResultOf<T> resultOf(T answer, List<ValidationError> errors, boolean isDefault,
                                            Map<String, List<MetaDataValue<?>>> metadata) {
        return new GResultOf<>(answer, errors, isDefault, metadata);
    }

    /**
     * Get if this result is a default value (optional or provided default).
     *
     * @return this result is a default value (optional or provided default)
     */
    public boolean isDefault() {
        return isDefault;
    }

    /**
     * returns true if the GResultOf has errors.
     *
     * @return true if the GResultOf has errors
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * returns true if the GResultOf has errors of level.
     *
     * @param level level of the error we are checking for.
     * @return true if the GResultOf has errors of level
     */
    public boolean hasErrors(ValidationLevel level) {
        return errors.stream().anyMatch(it -> it.level().equals(level));
    }

    /**
     * return a list of ValidationError.
     *
     * @return a list of ValidationError
     */
    public List<ValidationError> getErrors() {
        return errors;
    }

    /**
     * return a list of ValidationError for a level.
     *
     * @param level of errors we are looking for.
     * @return errors that match the level.
     */
    public List<ValidationError> getErrors(ValidationLevel level) {
        return errors.stream().filter(it -> it.level().equals(level)).collect(Collectors.toList());
    }

    /**
     * return a list of ValidationError not of a level.
     *
     * @param level of errors, we are not looking for.
     * @return errors that do not match the level.
     */
    public List<ValidationError> getErrorsNotLevel(ValidationLevel level) {
        return errors.stream().filter(it -> !it.level().equals(level)).collect(Collectors.toList());
    }

    /**
     * returns true if this GResultOf has results.
     *
     * @return true if this GResultOf has results.
     */
    public boolean hasResults() {
        return results != null;
    }

    /**
     * returns the results.
     *
     * @return the results.
     */
    public T results() {
        return results;
    }


    /**
     * If a result is present, return the result, otherwise throws an exception produced by the exceptionSupplier.
     *
     * @param exceptionSupplier throw supplied exception if no results
     * @param <X>               Type of the exception thrown
     * @throws X exception to be thrown by supplier
     */
    public <X extends Throwable> void throwIfNoResults(Supplier<? extends X> exceptionSupplier) throws X {
        if (!hasResults()) {
            throw exceptionSupplier.get();
        }
    }

    /**
     * If a result is present map the result with the resultFunction. If there are errors it will return a GResultOf with the errors.
     * If there is no result, then it will return a GResultOf with errors.
     *
     * @param resultFunction function to map the result
     * @param <U>            The type of the mapped GResultOf to return
     * @return a mapped GResultOf with the result and any errors.
     */
    public <U> GResultOf<U> mapWithError(Function<T, U> resultFunction) {
        if (!hasResults()) {
            return GResultOf.errors(errors);
        }

        return GResultOf.resultOf(resultFunction.apply(results), errors, isDefault, metadata);
    }

    /**
     * If there are results, return the results with any errors.
     * If there are no results return the errors along with the missing error.
     *
     * @param resultFunction function to map the result
     * @param missingError   error to return if there is no result
     * @param <U>            The type of the mapped GResultOf to return
     * @return a mapped GResultOf with the result.
     */
    public <U> GResultOf<U> mapWithError(Function<T, U> resultFunction, ValidationError missingError) {
        if (!hasResults()) {
            if (hasErrors()) {
                return GResultOf.errors(errors);
            } else {
                return GResultOf.errors(missingError);
            }
        }

        return GResultOf.resultOf(resultFunction.apply(results), errors, isDefault, metadata);
    }

    /**
     * Get the metadata associate with this key.
     *
     * @param key the metadata associate with this key.
     * @return the metadata associate with this key.
     */
    public List<MetaDataValue<?>> getMetadata(String key) {
        return metadata.get(key);
    }

    /**
     * Get the metadata map.
     *
     * @return the metadata map
     */
    public Map<String, List<MetaDataValue<?>>> getMetadata() {
        return metadata;
    }

    /**
     * Check if the metadata has a value for a key.
     *
     * @param key key to check in the metadata.
     *
     * @return if the metadata has a value for a key
     */
    public boolean hasMetadata(String key) {
        return metadata.containsKey(key);
    }
}

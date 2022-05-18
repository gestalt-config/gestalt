package org.github.gestalt.config.utils;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.entity.ValidationLevel;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Class that holds the results, errors or both.
 *
 * @param <T> type of validatedOf
 * @author Colin Redmond
 */
public final class ValidateOf<T> {
    private final T results;
    private final List<ValidationError> errors;

    private ValidateOf(T results, List<ValidationError> errors) {
        this.results = results;
        this.errors = Objects.requireNonNullElse(errors, Collections.emptyList());
    }

    /**
     * Create a ValidateOf with a valid result.
     *
     * @param answer valid results
     * @param <T> type of ValidateOf
     * @return ValidateOf
     */
    public static <T> ValidateOf<T> valid(T answer) {
        return new ValidateOf<>(answer, Collections.emptyList());
    }

    /**
     * Create a ValidateOf with errors.
     *
     * @param errors list of ValidationError
     * @param <T> type of ValidateOf
     * @return ValidateOf
     */
    public static <T> ValidateOf<T> inValid(List<ValidationError> errors) {
        return new ValidateOf<>(null, errors);
    }

    /**
     * Create a ValidateOf with a error.
     *
     * @param errors list of ValidationError
     * @param <T> type of ValidateOf
     * @return ValidateOf
     */
    public static <T> ValidateOf<T> inValid(ValidationError errors) {
        return new ValidateOf<>(null, Collections.singletonList(errors));
    }

    /**
     * Create a ValidateOf with a results and an error.
     *
     * @param answer valid results
     * @param errors list of ValidationError
     * @param <T> type of ValidateOf
     * @return ValidateOf
     */
    public static <T> ValidateOf<T> validateOf(T answer, List<ValidationError> errors) {
        return new ValidateOf<>(answer, errors);
    }

    /**
     * returns true if the ValidateOf has errors.
     *
     * @return true if the ValidateOf has errors
     */
    public Boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * returns true if the ValidateOf has errors of level.
     *
     * @param level level of the error we are checking for.
     * @return true if the ValidateOf has errors of level
     */
    public Boolean hasErrors(ValidationLevel level) {
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
     * returns true if this ValidateOf has results.
     *
     * @return true if this ValidateOf has results.
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

}

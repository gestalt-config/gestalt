package org.config.gestalt.utils;

import org.config.gestalt.entity.ValidationError;
import org.config.gestalt.entity.ValidationLevel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class ValidateOf<T> {
    private final T results;
    private final List<ValidationError> errors;

    private ValidateOf(T results, List<ValidationError> errors) {
        this.results = results;
        if (errors != null) {
            this.errors = errors;
        } else {
            this.errors = Collections.emptyList();
        }
    }

    public static <T> ValidateOf<T> valid(T answer) {
        return new ValidateOf<>(answer, Collections.emptyList());
    }

    public static <T> ValidateOf<T> inValid(List<ValidationError> errors) {
        return new ValidateOf<>(null, errors);
    }

    public static <T> ValidateOf<T> inValid(ValidationError errors) {
        return new ValidateOf<>(null, Collections.singletonList(errors));
    }

    public static <T> ValidateOf<T> validateOf(T answer, List<ValidationError> errors) {
        return new ValidateOf<>(answer, errors);
    }

    public Boolean hasErrors() {
        return !errors.isEmpty();
    }

    public Boolean hasErrors(ValidationLevel level) {
        return errors.stream().anyMatch(it -> it.level().equals(level));
    }

    public List<ValidationError> getErrors() {
        return errors;
    }

    public List<ValidationError> getErrors(ValidationLevel level) {
        return errors.stream().filter(it -> it.level().equals(level)).collect(Collectors.toList());
    }

    public boolean hasResults() {
        return results != null;
    }

    public T results() {
        return results;
    }

}

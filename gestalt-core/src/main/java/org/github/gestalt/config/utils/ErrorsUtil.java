package org.github.gestalt.config.utils;

import org.github.gestalt.config.entity.ValidationError;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility to build an error message from a list of validations.
 *
 * @author Colin Redmond
 */
public final class ErrorsUtil {
    private ErrorsUtil() {

    }

    /**
     * Create an error message from a message and the list of errors.
     *
     * @param message to prepend the error
     * @param errors list of validation errors.
     * @return string of the error
     */
    public static String buildErrorMessage(String message, List<ValidationError> errors) {
        return message + "\n - " + buildErrorMessage(errors);
    }

    /**
     * build an error message from a list of validations.
     *
     * @param errors list of validation errors.
     * @return string of the error
     */
    public static String buildErrorMessage(List<ValidationError> errors) {
        return errors.stream()
            .map(error -> "level: " + error.level() + ", message: " + error.description()).collect(Collectors.joining("\n - "));
    }
}

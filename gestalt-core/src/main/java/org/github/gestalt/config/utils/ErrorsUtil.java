package org.github.gestalt.config.utils;

import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.entity.ValidationLevel;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility to build an error message from a list of validations.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class ErrorsUtil {
    private ErrorsUtil() {

    }

    /**
     * Create an error message from a message and the list of errors.
     *
     * @param message to prepend the error
     * @param errors  list of validation errors.
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

    /**
     * Check if a result has errors that we should fail on.
     *
     * @param results results to check for an error.
     * @param gestaltConfig configuration on which errors should fail.
     * @param <T> type of the result.
     * @return if we should fails on the results.
     */
    public static <T> boolean checkErrorsShouldFail(GResultOf<T> results, GestaltConfig gestaltConfig) {
        if (results.hasErrors()) {
            return !results.getErrors().stream().allMatch(it -> ignoreError(it, gestaltConfig)) || !results.hasResults();
        } else {
            return false;
        }
    }

    /**
     * If we should ignore a specific error.
     *
     * @param error the error to check if we should ignore.
     * @param gestaltConfig configuration on which errors should fail.
     * @return if we should fail on this error.
     */
    public static boolean ignoreError(ValidationError error, GestaltConfig gestaltConfig) {
        if (error.level().equals(ValidationLevel.WARN) && gestaltConfig.isTreatWarningsAsErrors()) {
            return false;
        } else if (error instanceof ValidationError.ArrayMissingIndex && !gestaltConfig.isTreatMissingArrayIndexAsError()) {
            return true;
        } else if (error.hasNoResults() && !gestaltConfig.isTreatMissingValuesAsErrors()) {
            return true;
        } else if (error.level().equals(ValidationLevel.MISSING_OPTIONAL_VALUE) &&
            !gestaltConfig.isTreatMissingDiscretionaryValuesAsErrors()) {
            return true;
        }

        return error.level() == ValidationLevel.WARN || error.level() == ValidationLevel.DEBUG;
    }
}

package org.config.gestalt.utils;

import org.config.gestalt.entity.ValidationError;

import java.util.List;
import java.util.stream.Collectors;

public final class ErrorsUtil {
    private ErrorsUtil() {

    }

    public static String buildErrorMessage(String message, List<ValidationError> errors) {
        return message + "\n - " + buildErrorMessage(errors);
    }

    public static String buildErrorMessage(List<ValidationError> errors) {
        return errors.stream()
            .map(error -> "level: " + error.level() + ", message: " + error.description()).collect(Collectors.joining("\n - "));
    }
}

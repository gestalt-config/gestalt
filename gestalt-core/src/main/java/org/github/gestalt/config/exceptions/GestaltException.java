package org.github.gestalt.config.exceptions;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.utils.ErrorsUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Exception from Gestalt.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public class GestaltException extends Exception {

    /**
     * Any nested exceptions.
     */
    private final List<GestaltException> exceptions = new ArrayList<>();

    /**
     * Create GestaltException with a message.
     *
     * @param message message for the exception
     */
    public GestaltException(String message) {
        super(message);
    }

    /**
     * Create GestaltException with an exception.
     *
     * @param e Exception to wrap
     */
    public GestaltException(Exception e) {
        super(e);
    }

    /**
     * Create GestaltException with a message and exception.
     *
     * @param message message for the exception
     * @param e       Exception to wrap
     */
    public GestaltException(String message, Exception e) {
        super(message, e);
    }

    /**
     * Create GestaltException with a list of validation errors and a message.
     *
     * @param message message for the exception
     * @param errors  list of validation errors
     */
    public GestaltException(String message, List<ValidationError> errors) {
        super(ErrorsUtil.buildErrorMessage(message, errors));
    }

    /**
     * Create GestaltException with a list of exceptions to wrap.
     *
     * @param exceptions list of exceptions to wrap
     */
    public GestaltException(List<GestaltException> exceptions) {
        this.exceptions.addAll(exceptions);
    }

    @Override
    public String getMessage() {
        if (!exceptions.isEmpty()) {
            return exceptions.stream()
                .map(GestaltException::getMessage)
                .collect(Collectors.joining(", "));
        } else {
            return super.getMessage();
        }
    }
}

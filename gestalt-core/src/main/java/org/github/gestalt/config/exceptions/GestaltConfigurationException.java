package org.github.gestalt.config.exceptions;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.utils.ErrorsUtil;

import java.util.List;

/**
 * Configuration type exception for gestalt.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class GestaltConfigurationException extends GestaltException {
    /**
     * Create a GestaltConfigurationException with a message and a list of ValidationError.
     *
     * @param message message for the error
     * @param errors  list of ValidationErrors
     */
    public GestaltConfigurationException(String message, List<ValidationError> errors) {
        super(ErrorsUtil.buildErrorMessage(message, errors));
    }

    /**
     * Create a GestaltConfigurationException with a message.
     *
     * @param message message for the error
     */
    public GestaltConfigurationException(String message) {
        super(message);
    }
}

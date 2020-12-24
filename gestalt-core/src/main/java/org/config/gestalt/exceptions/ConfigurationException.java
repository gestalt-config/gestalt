package org.config.gestalt.exceptions;

import org.config.gestalt.entity.ValidationError;
import org.config.gestalt.utils.ErrorsUtil;

import java.util.List;

public class ConfigurationException extends GestaltException {
    public ConfigurationException(String message, List<ValidationError> errors) {
        super(ErrorsUtil.buildErrorMessage(message, errors));
    }

    public ConfigurationException(String message) {
        super(message);
    }
}

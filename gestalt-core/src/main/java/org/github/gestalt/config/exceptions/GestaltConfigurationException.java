package org.github.gestalt.config.exceptions;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.utils.ErrorsUtil;

import java.util.List;

/**
 * Configuration type exception for gestalt.
 *
 * @author Colin Redmond
 */
public class GestaltConfigurationException extends GestaltException {
    public GestaltConfigurationException(String message, List<ValidationError> errors) {
        super(ErrorsUtil.buildErrorMessage(message, errors));
    }

    public GestaltConfigurationException(String message) {
        super(message);
    }
}

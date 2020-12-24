package org.config.gestalt.exceptions;

import org.config.gestalt.entity.ValidationError;
import org.config.gestalt.utils.ErrorsUtil;

import java.util.List;

public class GestaltException extends Exception {

    public GestaltException(String message) {
        super(message);
    }

    public GestaltException(Exception e) {
        super(e);
    }

    public GestaltException(String message, Exception e) {
        super(message, e);
    }

    public GestaltException(String message, List<ValidationError> errors) {
        super(ErrorsUtil.buildErrorMessage(message, errors));
    }
}

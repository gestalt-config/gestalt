package org.github.gestalt.config.exceptions;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.utils.ErrorsUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Exception from Gestalt.
 *
 * @author Colin Redmond
 */
public class GestaltException extends Exception {

    List<GestaltException> exceptions = new ArrayList<>();

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

    public GestaltException(List<GestaltException> exceptions) {
        this.exceptions.addAll(exceptions);
    }
}

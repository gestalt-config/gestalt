package org.github.gestalt.config.utils;

import org.github.gestalt.config.entity.ValidationError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;


class ErrorsUtilTest {

    @Test
    void testBuildErrorMessage() {
        String error = ErrorsUtil.buildErrorMessage("my errors: ", List.of(new ValidationError.ArrayInvalidIndex(1, "db.hosts")));

        Assertions.assertEquals("my errors: \n" +
            " - level: ERROR, message: Invalid array index: 1 for path: db.hosts", error);
    }

    @Test
    void testBuildErrorMessageAsList() {
        String error = ErrorsUtil.buildErrorMessage(
            Collections.singletonList(new ValidationError.ArrayInvalidIndex(1, "db.hosts")));

        Assertions.assertEquals("level: ERROR, message: Invalid array index: 1 for path: db.hosts", error);
    }
}

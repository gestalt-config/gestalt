package org.github.gestalt.config.validation;

import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages all validations. Gestalt calls the manager who then forwards the calls to all registered validators.
 * It collates the results and returns either the object or the errors.
 *
 *  @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class ValidationManager {

    private final List<ConfigValidator> configValidators;

    public ValidationManager(List<ConfigValidator> configValidators) {
        this.configValidators = configValidators;
    }

    /**
     * Add a list of validators to the manager.
     *
     * @param validatorsSet list of validators
     */
    public void addValidators(List<ConfigValidator> validatorsSet) {
        this.configValidators.addAll(validatorsSet);
    }

    /**
     * Calls all the validators, and it collates the results and returns either the object or the errors.
     *
     * @param obj object to validate.
     * @param path path the object was located at
     * @param klass the type of object.
     * @param tags any tags used to retrieve te object
     * @param <T> Class of the object.
     * @return The validation results with either errors or a successful  obj.
     */
    public <T> GResultOf<T> validator(T obj, String path, TypeCapture<T> klass, Tags tags) {
        var errors = configValidators.stream()
            .map(it -> it.validator(obj, path, klass, tags))
            .flatMap(it -> it.getErrors().stream())
            .collect(Collectors.toList());

        if (errors.isEmpty()) {
            return GResultOf.result(obj);
        } else {
            return GResultOf.errors(errors);
        }
    }
}

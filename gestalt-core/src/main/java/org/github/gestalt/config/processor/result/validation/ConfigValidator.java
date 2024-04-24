package org.github.gestalt.config.processor.result.validation;

import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;

/**
 * Interface for validating objects.
 *
 *  @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public interface ConfigValidator {

    /**
     * If your Validator needs access to the Gestalt Config.
     *
     * @param config Gestalt configuration
     */
    default void applyConfig(GestaltConfig config) {}

    /**
     * Returns the {@link GResultOf} with the validation results. If the object is ok it will return the result with no errors.
     * If there are validation errors they will be returned.
     *
     * @param obj object to validate.
     * @param path path the object was located at
     * @param klass the type of object.
     * @param tags any tags used to retrieve te object
     * @return The validation results with either errors or a successful  obj.
     * @param <T> Class of the object.
     */
    <T> GResultOf<T> validator(T obj, String path, TypeCapture<T> klass, Tags tags);
}

package org.github.gestalt.config.processor.result;

import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;

/**
 * Interface for Processing the results of getting a configuration.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public interface ResultProcessor {

    /**
     * If your Result Processor needs access to the Gestalt Config.
     *
     * @param config Gestalt configuration
     */
    default void applyConfig(GestaltConfig config) {
    }

    /**
     * Returns the {@link GResultOf} with any processed results.
     * You can modify the results, errors or any combination.
     * If your post processor does nothing to the node, return the original node.
     *
     * @param results    GResultOf to process.
     * @param path       path the object was located at
     * @param isOptional if the result is optional (an Optional or has a default.
     * @param defaultVal value to return in the event of failure.
     * @param klass      the type of object.
     * @param tags       any tags used to retrieve te object
     * @param <T>        Class of the object.
     * @return The validation results with either errors or a successful  obj.
     * @throws GestaltException for any exceptions while processing the results, such as if there are errors in the result.
     */
    <T> GResultOf<T> processResults(GResultOf<T> results, String path, boolean isOptional, T defaultVal, TypeCapture<T> klass, Tags tags)
        throws GestaltException;
}

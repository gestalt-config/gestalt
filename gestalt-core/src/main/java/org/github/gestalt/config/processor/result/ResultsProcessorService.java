package org.github.gestalt.config.processor.result;

import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;

import java.util.List;

/**
 * Manages all result processors. Gestalt calls the manager who then forwards the calls to all registered result processors
 * in order of priority. It passes the processed results of one into the result processor of the next.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public interface ResultsProcessorService {

    /**
     * Add a list of ResultProcessor to the manager.
     *
     * @param resultProcessorSet list of validators
     */
    void addResultProcessors(List<ResultProcessor> resultProcessorSet);

    /**
     * Calls all registered result processors in order of priority.
     * It passes the processed results of one into the result processor of the next.
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
    <T> GResultOf<T> processResults(GResultOf<T> results, String path, boolean isOptional, T defaultVal,
                                    TypeCapture<T> klass, Tags tags) throws GestaltException;
}

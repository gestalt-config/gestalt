package org.github.gestalt.config.processor.result;

import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;

import java.util.List;

/**
 * Manages all result processors. Gestalt calls the manager who then forwards the calls to all registered result processors
 * in order of priority. It passes the processed results of one into the result processor of the next.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class ResultsProcessorManager {

    private final List<ResultProcessor> resultProcessors;

    public ResultsProcessorManager(List<ResultProcessor> resultProcessors) {
        this.resultProcessors = resultProcessors;
    }

    /**
     * Add a list of validators to the manager.
     *
     * @param validatorsSet list of validators
     */
    public void addValidators(List<ResultProcessor> validatorsSet) {
        this.resultProcessors.addAll(validatorsSet);
    }

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
     */
    public <T> GResultOf<T> processResults(GResultOf<T> results, String path, boolean isOptional, T defaultVal,
                                           TypeCapture<T> klass, Tags tags) {

        GResultOf<T> processedResults = results;
        for (var resultProcessor : resultProcessors) {
            processedResults = resultProcessor.processResults(processedResults, path, isOptional, defaultVal, klass, tags);
        }

        return processedResults;
    }
}

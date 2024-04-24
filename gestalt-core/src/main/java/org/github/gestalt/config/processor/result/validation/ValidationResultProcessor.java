package org.github.gestalt.config.processor.result.validation;

import org.github.gestalt.config.GestaltCore;
import org.github.gestalt.config.annotations.ConfigPriority;
import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.observations.ObservationManager;
import org.github.gestalt.config.processor.result.ResultProcessor;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.ClassUtils;
import org.github.gestalt.config.utils.GResultOf;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

@ConfigPriority(300)
public class ValidationResultProcessor implements ResultProcessor {

    // using the GestaltCore logger
    private static final System.Logger logger = System.getLogger(GestaltCore.class.getName());

    private GestaltConfig gestaltConfig;
    private ObservationManager observationManager;
    private final List<ConfigValidator> configValidators;

    public ValidationResultProcessor() {
        configValidators = new ArrayList<>();
        ServiceLoader<ConfigValidator> loader = ServiceLoader.load(ConfigValidator.class);
        loader.forEach(configValidators::add);
    }

    public ValidationResultProcessor(List<ConfigValidator> configValidators, ObservationManager observationManager) {
        this.configValidators = configValidators;
        this.observationManager = observationManager;
    }

    @Override
    public void applyConfig(GestaltConfig config) {
        this.gestaltConfig = config;

        configValidators.forEach(it -> it.applyConfig(config));
    }


    @Override
    public <T> GResultOf<T> processResults(GResultOf<T> results, String path, boolean isOptional,
                                           T defaultVal, TypeCapture<T> klass, Tags tags) throws GestaltException {
        // if we have a result, lets validate the result.
        // otherwise return what we have.
        if (results.hasResults() && shouldValidate(klass)) {
            T obj = results.results();
            List<ValidationError> errors = configValidators.stream()
                .map(it -> it.validator(obj, path, klass, tags))
                .flatMap(it -> it.getErrors().stream())
                .collect(Collectors.toList());

            // if there are validation errors we can either fail with an exception or return the default value.
            if (!errors.isEmpty()) {
                updateValidationObservations(errors);

                if (!isOptional) {
                    throw new GestaltException("Validation failed for config path: " + path +
                        ", and class: " + klass.getName(), errors);
                } else {
                    // return an empty result, that the optional processor wil populate
                    return GResultOf.result(null);
                }
            }
        }

        // We have either logged or thrown exceptions for the errors, so strip them from the results.
        return results;
    }


    private <T> boolean shouldValidate(TypeCapture<T> klass) {
        return !klass.isAssignableFrom(String.class) && !ClassUtils.isPrimitiveOrWrapper(klass.getRawType());
    }

    private <T> void updateValidationObservations(List<ValidationError> errors) {
        if (gestaltConfig != null && gestaltConfig.isObservationsEnabled() && observationManager != null) {
            observationManager.recordObservation("get.config.validation.error", errors.size(), Tags.of());
        }
    }
}

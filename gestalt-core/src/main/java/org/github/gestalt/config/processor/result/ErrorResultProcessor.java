package org.github.gestalt.config.processor.result;

import org.github.gestalt.config.GestaltCore;
import org.github.gestalt.config.annotations.ConfigPriority;
import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.ErrorsUtil;
import org.github.gestalt.config.utils.GResultOf;

import static java.lang.System.Logger.Level.DEBUG;
import static org.github.gestalt.config.utils.ErrorsUtil.checkErrorsShouldFail;

@ConfigPriority(200)
public class ErrorResultProcessor implements ResultProcessor {

    // using the GestaltCore logger
    private static final System.Logger logger = System.getLogger(GestaltCore.class.getName());

    private GestaltConfig gestaltConfig;

    @Override
    public void applyConfig(GestaltConfig config) {
        this.gestaltConfig = config;
    }


    @Override
    public <T> GResultOf<T> processResults(GResultOf<T> results, String path, boolean isOptional, T defaultVal,
                                           TypeCapture<T> klass, Tags tags) throws GestaltException {
        if (checkErrorsShouldFail(results, gestaltConfig)) {
            if (!isOptional) {
                throw new GestaltException("Failed getting config path: " + path + ", for class: " + klass.getName(),
                    results.getErrors());
            } else {
                if (logger.isLoggable(gestaltConfig.getLogLevelForMissingValuesWhenDefaultOrOptional())) {
                    String errorMsg = ErrorsUtil.buildErrorMessage("Failed getting config path: " + path +
                        ", for class: " + klass.getName() + " returning empty Optional", results.getErrors());
                    logger.log(gestaltConfig.getLogLevelForMissingValuesWhenDefaultOrOptional(), errorMsg);
                }
                return GResultOf.result(null);
            }
        } else if (results.hasErrors() && logger.isLoggable(DEBUG)) {
            String errorMsg = ErrorsUtil.buildErrorMessage("Errors getting config path: " + path +
                ", for class: " + klass.getName(), results.getErrors());
            logger.log(DEBUG, errorMsg);
        }

        // We have either logged or thrown exceptions for the errors, so strip them from the results.
        return GResultOf.result(results.results());
    }

}

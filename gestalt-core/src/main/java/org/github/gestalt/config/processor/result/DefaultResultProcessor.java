package org.github.gestalt.config.processor.result;

import org.github.gestalt.config.annotations.ConfigPriority;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;

@ConfigPriority(600)
public class DefaultResultProcessor implements ResultProcessor {

    @Override
    public <T> GResultOf<T> processResults(GResultOf<T> results, String path, boolean isOptional,
                                           T defaultVal, TypeCapture<T> klass, Tags tags) throws GestaltException {

        // If this is an optional config, and it doesn't have a result, return the optional value.
        if (isOptional && !results.hasResults()) {
            return GResultOf.result(defaultVal, true);
        }

        // otherwise return the original result.
        return results;
    }
}

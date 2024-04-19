package org.github.gestalt.config.processor;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.processor.result.ResultProcessor;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;

public class TestResultProcessor implements ResultProcessor {

    public boolean isOk = true;

    public TestResultProcessor(boolean isOk) {
        this.isOk = isOk;
    }

    @Override
    public <T> GResultOf<T> processResults(GResultOf<T> results, String path, boolean isOptional, T defaultVal,
                                           TypeCapture<T> klass, Tags tags) {
        if (isOk) {
            return GResultOf.result(results.results());
        } else {
            return GResultOf.errors(new ValidationError(ValidationLevel.ERROR) {
                @Override
                public String description() {
                    return "something broke";
                }
            });
        }
    }
}

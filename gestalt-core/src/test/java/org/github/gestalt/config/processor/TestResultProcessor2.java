package org.github.gestalt.config.processor;

import org.github.gestalt.config.annotations.ConfigPriority;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.processor.result.ResultProcessor;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;

@ConfigPriority(450)
public class TestResultProcessor2 implements ResultProcessor {

    public boolean isOk = true;

    public TestResultProcessor2(boolean isOk) {
        this.isOk = isOk;
    }

    @Override
    public <T> GResultOf<T> processResults(GResultOf<T> results, String path, boolean isOptional, T defaultVal,
                                           TypeCapture<T> klass, Tags tags) {
        if (isOk) {
            return results;
        } else {
            return GResultOf.errors(new ValidationError(ValidationLevel.ERROR) {
                @Override
                public String description() {
                    return "something broke2";
                }
            });
        }
    }
}

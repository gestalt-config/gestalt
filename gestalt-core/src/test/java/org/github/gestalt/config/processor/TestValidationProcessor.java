package org.github.gestalt.config.processor;

import org.github.gestalt.config.annotations.ConfigPriority;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.processor.result.validation.ConfigValidator;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;

@ConfigPriority(600)
public class TestValidationProcessor implements ConfigValidator {

    public boolean isOk = true;

    public TestValidationProcessor(boolean isOk) {
        this.isOk = isOk;
    }


    @Override
    public <T> GResultOf<T> validator(T obj, String path, TypeCapture<T> klass, Tags tags) {
        if (isOk) {
            return GResultOf.result(obj);
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

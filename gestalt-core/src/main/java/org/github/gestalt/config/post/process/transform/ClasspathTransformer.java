package org.github.gestalt.config.post.process.transform;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.utils.ValidateOf;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Allows you to load the contents of a file on the classpath into a string substitution.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public final class ClasspathTransformer implements Transformer {

    private final int prefixLength = (name() + ":").length();       // NOPMD

    @Override
    public String name() {
        return "classpath";
    }

    @Override
    public ValidateOf<String> process(String path, String key, String rawValue) {
        if (key != null) {
            try {
                String resource = rawValue.substring(prefixLength);
                InputStream is = getClass().getClassLoader().getResourceAsStream(resource);
                if (is == null) {
                    is = ClasspathTransformer.class.getResourceAsStream(resource);
                    if (is == null) {
                        return ValidateOf.inValid(new ValidationError.ExceptionReadingFileDuringTransform(path, key,
                            "Unable to load classpath resource from " + resource));
                    }
                }
                var fileBytes = is.readAllBytes();
                return ValidateOf.valid(new String(fileBytes, Charset.defaultCharset()));
            } catch (IOException e) {
                return ValidateOf.inValid(new ValidationError.ExceptionReadingFileDuringTransform(path, key, e.getMessage()));
            }
        } else {
            return ValidateOf.inValid(new ValidationError.InvalidStringSubstitutionPostProcess(path, rawValue, name()));
        }
    }
}

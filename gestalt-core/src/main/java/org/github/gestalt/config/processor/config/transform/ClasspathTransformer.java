package org.github.gestalt.config.processor.config.transform;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.utils.GResultOf;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Allows you to load the contents of a file on the classpath into a string substitution.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class ClasspathTransformer implements Transformer {

    private final int prefixLength = (name() + ":").length();       // NOPMD

    @Override
    public String name() {
        return "classpath";
    }

    @Override
    public GResultOf<String> process(String path, String key, String rawValue) {
        GResultOf<String> result;
        if (key != null) {
            InputStream is = null;
            try {
                String resource = rawValue.substring(prefixLength);
                is = getClass().getClassLoader().getResourceAsStream(resource);
                if (is == null) {
                    is = ClasspathTransformer.class.getResourceAsStream(resource);
                    if (is == null) {
                        return GResultOf.errors(new ValidationError.ExceptionReadingFileDuringTransform(path, key,
                            "Unable to load classpath resource from " + resource));
                    }
                }
                var fileBytes = is.readAllBytes();
                result = GResultOf.result(new String(fileBytes, Charset.defaultCharset()));
            } catch (IOException e) {
                result = GResultOf.errors(new ValidationError.ExceptionReadingFileDuringTransform(path, key, e.getMessage()));
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        result = GResultOf.errors(new ValidationError.ExceptionReadingFileDuringTransform(path, key, e.getMessage()));
                    }
                }
            }
        } else {
            result = GResultOf.errors(new ValidationError.InvalidStringSubstitutionPostProcess(path, rawValue, name()));
        }

        return result;
    }
}

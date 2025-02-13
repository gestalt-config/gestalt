package org.github.gestalt.config.processor.config.transform;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.utils.GResultOf;

import java.nio.charset.Charset;
import java.util.Base64;

/**
 * Allows you to encode a string to base 64.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class Base64EncoderTransformer implements Transformer {
    @Override
    public String name() {
        return "base64Encode";
    }

    @Override
    public GResultOf<String> process(String path, String key, String rawValue) {
        if (key != null) {
            String encodedBytes = Base64.getEncoder().encodeToString(key.getBytes(Charset.defaultCharset()));
            return GResultOf.result(encodedBytes);
        } else {
            return GResultOf.errors(new ValidationError.InvalidStringSubstitutionPostProcess(path, rawValue, name()));
        }
    }
}

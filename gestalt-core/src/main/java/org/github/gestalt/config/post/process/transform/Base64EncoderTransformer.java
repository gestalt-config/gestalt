package org.github.gestalt.config.post.process.transform;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.utils.ValidateOf;

import java.nio.charset.Charset;
import java.util.Base64;

/**
 * Allows you to encode a string to base 64.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public class Base64EncoderTransformer implements Transformer {
    @Override
    public String name() {
        return "base64Encode";
    }

    @Override
    public ValidateOf<String> process(String path, String key, String rawValue) {
        if (key != null) {
            String encodedBytes = Base64.getEncoder().encodeToString(key.getBytes(Charset.defaultCharset()));
            return ValidateOf.valid(encodedBytes);
        } else {
            return ValidateOf.inValid(new ValidationError.InvalidStringSubstitutionPostProcess(path, rawValue, name()));
        }
    }
}

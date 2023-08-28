package org.github.gestalt.config.post.process.transform;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.utils.ValidateOf;

import java.net.URLDecoder;
import java.nio.charset.Charset;

/**
 * Allows you to URL decode a string.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public final class URLDecoderTransformer implements Transformer {
    @Override
    public String name() {
        return "urlDecode";
    }

    @Override
    public ValidateOf<String> process(String path, String key, String rawValue) {
        if (key != null) {
            try {
                return ValidateOf.valid(URLDecoder.decode(key, Charset.defaultCharset()));
            } catch (IllegalArgumentException e) {
                return ValidateOf.inValid(new ValidationError.InvalidBase64DecodeString(path, key, e.getMessage()));
            }
        } else {
            return ValidateOf.inValid(new ValidationError.InvalidStringSubstitutionPostProcess(path, rawValue, name()));
        }
    }
}

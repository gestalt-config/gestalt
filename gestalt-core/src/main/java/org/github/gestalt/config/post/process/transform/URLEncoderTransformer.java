package org.github.gestalt.config.post.process.transform;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.utils.ValidateOf;

import java.net.URLEncoder;
import java.nio.charset.Charset;

/**
 * Allows you to URL encode a string.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public class URLEncoderTransformer implements Transformer {
    @Override
    public String name() {
        return "urlEncode";
    }

    @Override
    public ValidateOf<String> process(String path, String key, String rawValue) {
        if (key != null) {
            return ValidateOf.valid(URLEncoder.encode(key, Charset.defaultCharset()));
        } else {
            return ValidateOf.inValid(new ValidationError.InvalidStringSubstitutionPostProcess(path, rawValue, name()));
        }
    }
}

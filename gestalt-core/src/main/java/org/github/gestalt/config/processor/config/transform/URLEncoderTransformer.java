package org.github.gestalt.config.processor.config.transform;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.utils.GResultOf;

import java.net.URLEncoder;
import java.nio.charset.Charset;

/**
 * Allows you to URL encode a string.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class URLEncoderTransformer implements Transformer {
    @Override
    public String name() {
        return "urlEncode";
    }

    @Override
    public GResultOf<String> process(String path, String key, String rawValue) {
        if (key != null) {
            return GResultOf.result(URLEncoder.encode(key, Charset.defaultCharset()));
        } else {
            return GResultOf.errors(new ValidationError.InvalidStringSubstitutionPostProcess(path, rawValue, name()));
        }
    }
}

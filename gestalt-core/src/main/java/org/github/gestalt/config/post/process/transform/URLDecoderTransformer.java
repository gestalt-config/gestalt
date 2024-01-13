package org.github.gestalt.config.post.process.transform;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.utils.GResultOf;

import java.net.URLDecoder;
import java.nio.charset.Charset;

/**
 * Allows you to URL decode a string.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class URLDecoderTransformer implements Transformer {
    @Override
    public String name() {
        return "urlDecode";
    }

    @Override
    public GResultOf<String> process(String path, String key, String rawValue) {
        if (key != null) {
            try {
                return GResultOf.result(URLDecoder.decode(key, Charset.defaultCharset()));
            } catch (IllegalArgumentException e) {
                return GResultOf.errors(new ValidationError.InvalidBase64DecodeString(path, key, e.getMessage()));
            }
        } else {
            return GResultOf.errors(new ValidationError.InvalidStringSubstitutionPostProcess(path, rawValue, name()));
        }
    }
}

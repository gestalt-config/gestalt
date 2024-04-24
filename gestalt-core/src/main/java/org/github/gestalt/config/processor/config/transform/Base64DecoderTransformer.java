package org.github.gestalt.config.processor.config.transform;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.utils.GResultOf;

import java.nio.charset.Charset;
import java.util.Base64;

/**
 * Allows you to decode a base 64 encoded string.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class Base64DecoderTransformer implements Transformer {
    @Override
    public String name() {
        return "base64Decode";
    }

    @Override
    public GResultOf<String> process(String path, String key, String rawValue) {
        if (key != null) {
            try {
                byte[] decodedBytes = Base64.getDecoder().decode(key);
                return GResultOf.result(new String(decodedBytes, Charset.defaultCharset()));
            } catch (IllegalArgumentException e) {
                return GResultOf.errors(new ValidationError.InvalidBase64DecodeString(path, key, e.getMessage()));
            }
        } else {
            return GResultOf.errors(new ValidationError.InvalidStringSubstitutionPostProcess(path, rawValue, name()));
        }
    }
}

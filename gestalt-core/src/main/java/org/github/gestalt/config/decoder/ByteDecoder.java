package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;

import java.nio.charset.Charset;

/**
 * Decode Byte.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class ByteDecoder extends LeafDecoder<Byte> {

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "Byte";
    }

    @Override
    public boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type) {
        return Byte.class.isAssignableFrom(type.getRawType()) || byte.class.isAssignableFrom(type.getRawType());
    }

    @Override
    protected GResultOf<Byte> leafDecode(String path, ConfigNode node, DecoderContext decoderContext) {
        GResultOf<Byte> results;

        String value = node.getValue().orElse("");
        if (value.length() == 1) {
            results = GResultOf.result(value.getBytes(Charset.defaultCharset())[0]);
        } else {
            results = GResultOf.errors(new ValidationError.DecodingByteTooLong(path, node, decoderContext.getSecretConcealer()));
        }

        return results;
    }
}

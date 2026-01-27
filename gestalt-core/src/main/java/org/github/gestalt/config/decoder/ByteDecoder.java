package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;

import java.nio.charset.Charset;

/**
 * Decode Byte.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class ByteDecoder extends LeafDecoder<Byte> {

    private boolean treatEmptyStringsAsNull = false;

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "Byte";
    }

    @Override
    public void applyConfig(GestaltConfig config) {
        this.treatEmptyStringsAsNull = config.isTreatEmptyStringsAsNull();
    }

    @Override
    public boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type) {
        return Byte.class.isAssignableFrom(type.getRawType()) || byte.class.isAssignableFrom(type.getRawType());
    }

    @Override
    protected GResultOf<Byte> leafDecode(String path, ConfigNode node, DecoderContext decoderContext) {
        String value = node.getValue().orElse("");

        // Check if empty string should be treated as null
        if (value.isEmpty() && treatEmptyStringsAsNull) {
            return GResultOf.result(null);
        }

        if (value.length() == 1) {
            return GResultOf.result(value.getBytes(Charset.defaultCharset())[0]);
        } else if (value.length() > 1) {
            return GResultOf.resultOf(value.getBytes(Charset.defaultCharset())[0],
                new ValidationError.DecodingByteTooLong(path, node, decoderContext));
        } else {
            return GResultOf.errors(new ValidationError.DecodingEmptyByte(path, node, decoderContext));
        }
    }
}

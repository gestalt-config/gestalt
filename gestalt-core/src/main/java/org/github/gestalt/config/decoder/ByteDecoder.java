package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.utils.ValidateOf;

import java.nio.charset.Charset;

/**
 * Decode Byte.
 *
 * @author <a href="mailto:colin.redmond@outlook.com">Colin Redmond (c) 2023.
 */
public class ByteDecoder extends LeafDecoder<Byte> {

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "Byte";
    }

    @Override
    public boolean matches(TypeCapture<?> klass) {
        return Byte.class.isAssignableFrom(klass.getRawType()) || byte.class.isAssignableFrom(klass.getRawType());
    }

    @Override
    protected ValidateOf<Byte> leafDecode(String path, ConfigNode node) {
        ValidateOf<Byte> results;

        String value = node.getValue().orElse("");
        if (value.length() == 1) {
            results = ValidateOf.valid(value.getBytes(Charset.defaultCharset())[0]);
        } else {
            results = ValidateOf.inValid(new ValidationError.DecodingByteTooLong(path, node));
        }

        return results;
    }
}

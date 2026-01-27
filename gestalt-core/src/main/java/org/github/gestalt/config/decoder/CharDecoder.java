package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;

import java.util.ArrayList;
import java.util.List;

/**
 * Decode char.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class CharDecoder extends LeafDecoder<Character> {

    private boolean treatEmptyStringsAsNull = false;

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "Character";
    }

    @Override
    public void applyConfig(GestaltConfig config) {
        this.treatEmptyStringsAsNull = config.isTreatEmptyStringsAsNull();
    }

    @Override
    public boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type) {
        return Character.class.isAssignableFrom(type.getRawType()) || char.class.isAssignableFrom(type.getRawType());
    }

    @Override
    protected GResultOf<Character> leafDecode(String path, ConfigNode node, DecoderContext decoderContext) {
        Character results = null;
        List<ValidationError> error = new ArrayList<>();

        String value = node.getValue().orElse("");

        // Check if empty string should be treated as null
        if (value.isEmpty() && treatEmptyStringsAsNull) {
            return GResultOf.result(null);
        }

        if (!value.isEmpty()) {
            results = value.charAt(0);
        }

        if (value.length() != 1) {
            error.add(new ValidationError.DecodingCharWrongSize(path, node, decoderContext));
        }

        return GResultOf.resultOf(results, error);
    }
}

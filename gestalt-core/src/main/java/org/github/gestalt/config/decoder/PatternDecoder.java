package org.github.gestalt.config.decoder;

import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;

import java.util.regex.Pattern;

/**
 * Decode a Pattern.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class PatternDecoder extends LeafDecoder<Pattern> {

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "Pattern";
    }

    @Override
    public boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type) {
        return Pattern.class.isAssignableFrom(type.getRawType());
    }

    @Override
    protected GResultOf<Pattern> leafDecode(String path, ConfigNode node, DecoderContext decoderContext) {
        Pattern pattern = Pattern.compile(node.getValue().orElse(""), Pattern.CASE_INSENSITIVE);
        return GResultOf.result(pattern);
    }
}

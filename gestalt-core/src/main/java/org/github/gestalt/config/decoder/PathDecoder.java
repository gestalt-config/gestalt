package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Decode a Path.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class PathDecoder extends LeafDecoder<Path> {

    private boolean treatEmptyStringsAsNull = false;

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "Path";
    }

    @Override
    public void applyConfig(GestaltConfig config) {
        this.treatEmptyStringsAsNull = config.isTreatEmptyStringsAsNull();
    }

    @Override
    public boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type) {
        return Path.class.isAssignableFrom(type.getRawType());
    }

    @Override
    protected GResultOf<Path> leafDecode(String path, ConfigNode node, DecoderContext decoderContext) {
        String value = node.getValue().orElse("");

        // Check if empty string should be treated as null
        if (value.isEmpty() && treatEmptyStringsAsNull) {
            return GResultOf.result(null);
        }

        Path file = Paths.get(value);
        return GResultOf.result(file);
    }
}

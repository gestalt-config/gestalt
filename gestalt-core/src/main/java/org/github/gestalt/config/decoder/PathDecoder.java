package org.github.gestalt.config.decoder;

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

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "Path";
    }

    @Override
    public boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type) {
        return Path.class.isAssignableFrom(type.getRawType());
    }

    @Override
    protected GResultOf<Path> leafDecode(String path, ConfigNode node, DecoderContext decoderContext) {
        Path file = Paths.get(node.getValue().orElse(""));
        return GResultOf.result(file);
    }
}

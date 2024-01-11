package org.github.gestalt.config.decoder;

import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.ValidateOf;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Decode a Path.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
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
    protected ValidateOf<Path> leafDecode(String path, ConfigNode node) {
        Path file = Paths.get(node.getValue().orElse(""));
        return ValidateOf.valid(file);
    }
}

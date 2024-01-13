package org.github.gestalt.config.decoder;

import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Decode a File.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class FileDecoder extends LeafDecoder<File> {

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "File";
    }

    @Override
    public boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type) {
        return File.class.isAssignableFrom(type.getRawType());
    }

    @Override
    protected GResultOf<File> leafDecode(String path, ConfigNode node) {
        Path file = Paths.get(node.getValue().orElse(""));
        return GResultOf.result(file.toFile());
    }
}

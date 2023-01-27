package org.github.gestalt.config.decoder;

import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.utils.ValidateOf;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Decode a Path.
 *
 * @author <a href="mailto:colin.redmond@outlook.com">Colin Redmond (c) 2023.
 */
public class PathDecoder extends LeafDecoder<Path> {

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "Path";
    }

    @Override
    public boolean matches(TypeCapture<?> klass) {
        return Path.class.isAssignableFrom(klass.getRawType());
    }

    @Override
    protected ValidateOf<Path> leafDecode(String path, ConfigNode node) {
        Path file = Paths.get(node.getValue().orElse(""));
        return ValidateOf.valid(file);
    }
}

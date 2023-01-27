package org.github.gestalt.config.decoder;

import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.utils.ValidateOf;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Decode a File.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public class FileDecoder extends LeafDecoder<File> {

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "File";
    }

    @Override
    public boolean matches(TypeCapture<?> klass) {
        return File.class.isAssignableFrom(klass.getRawType());
    }

    @Override
    protected ValidateOf<File> leafDecode(String path, ConfigNode node) {
        Path file = Paths.get(node.getValue().orElse(""));
        return ValidateOf.valid(file.toFile());
    }
}

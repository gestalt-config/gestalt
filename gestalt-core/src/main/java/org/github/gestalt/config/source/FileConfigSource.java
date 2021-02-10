package org.github.gestalt.config.source;

import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Load a config source from a local file.
 *
 * @author Colin Redmond
 */
public class FileConfigSource implements ConfigSource {
    private static final Logger logger = LoggerFactory.getLogger(FileConfigSource.class.getName());

    private final Path path;
    private final UUID id = UUID.randomUUID();

    public FileConfigSource(File file) throws GestaltException {
        this(Objects.requireNonNull(file, "file can not be null").toPath());
    }

    public FileConfigSource(Path path) throws GestaltException {
        this.path = validatePath(path);
    }

    private Path validatePath(Path path) throws GestaltException {
        Objects.requireNonNull(path, "Path can not be null");
        if (!Files.exists(path)) {
            throw new GestaltException("File does not exist from path: " + path.toString());
        } else if (!Files.isRegularFile(path)) {
            throw new GestaltException("Path is not a regular file: " + path.toString());
        } else if (!Files.isReadable(path)) {
            throw new GestaltException("Path is not a readable: " + path.toString());
        } else if ("".equals(format(path))) {
            logger.debug("Unable to find a format for the file: {}", path.toString());
        }
        return path;
    }

    public Path getPath() {
        return path;
    }

    @Override
    public boolean hasStream() {
        return true;
    }

    @Override
    public InputStream loadStream() throws GestaltException {
        try {
            return Files.newInputStream(path);
        } catch (IOException e) {
            throw new GestaltException("Unable to load file from path " + path.toString(), e);
        }
    }

    @Override
    public boolean hasList() {
        return false;
    }

    @Override
    public List<Pair<String, String>> loadList() throws GestaltException {
        throw new GestaltException("Unsupported operation loadList on an FileConfigSource");
    }

    @Override
    public String format() {
        return format(this.path);
    }

    protected String format(Path path) {
        String fileName = path.getFileName().toString();
        int index = fileName.lastIndexOf('.');
        if (index > 0) {
            return fileName.substring(index + 1);
        } else {
            return "";
        }
    }

    @Override
    public String name() {
        return "File source: " + path.toString();
    }

    @Override
    public UUID id() {  //NOPMD
        return id;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FileConfigSource)) {
            return false;
        }
        FileConfigSource that = (FileConfigSource) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

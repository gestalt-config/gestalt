package org.github.gestalt.config.source;

import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.Pair;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Load a config source from a classpath resource using the getResourceAsStream method.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class ClassPathConfigSource implements ConfigSource {
    private final String resource;
    private final UUID id = UUID.randomUUID();
    private final Tags tags;

    /**
     * Default constructor for the ClassPathConfigSource.
     *
     * @param resource name of the resource to load from the class path.
     * @throws GestaltException any exceptions
     */
    public ClassPathConfigSource(String resource) throws GestaltException {
        this(resource, Tags.of());
    }

    /**
     * Default constructor for the ClassPathConfigSource.
     *
     * @param resource name of the resource to load from the class path.
     * @param tags     tags associated with the source
     * @throws GestaltException any exceptions
     * @deprecated tags should be
     */
    @Deprecated(since = "0.26.0", forRemoval = true)
    public ClassPathConfigSource(String resource, Tags tags) throws GestaltException {
        this.resource = resource;
        if (resource == null) {
            throw new GestaltException("Class path resource cannot be null");
        }
        this.tags = tags;
    }

    @Override
    public boolean hasStream() {
        return true;
    }

    @Override
    public InputStream loadStream() throws GestaltException {

        InputStream is = getClass().getClassLoader().getResourceAsStream(resource);
        if (is == null) {
            is = ClassPathConfigSource.class.getResourceAsStream(resource);
            if (is == null) {
                throw new GestaltException("Unable to load classpath resource from " + resource);
            }
        }
        return is;
    }

    @Override
    public boolean hasList() {
        return false;
    }

    @Override
    public List<Pair<String, String>> loadList() throws GestaltException {
        throw new GestaltException("Unsupported operation loadList on an ClassPathConfigSource");
    }

    @Override
    public String format() {
        return format(resource);
    }

    /**
     * Finds the extension of a file to get the file format.
     *
     * @param fileName the name of the file
     * @return the extension of the file
     */
    private String format(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index > 0) {
            return fileName.substring(index + 1);
        } else {
            return "";
        }
    }

    @Override
    public String name() {
        return "Class Path resource: " + resource;
    }

    @Override
    public UUID id() {  //NOPMD
        return id;
    }

    @Override
    public Tags getTags() {
        return tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ClassPathConfigSource)) {
            return false;
        }
        ClassPathConfigSource that = (ClassPathConfigSource) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

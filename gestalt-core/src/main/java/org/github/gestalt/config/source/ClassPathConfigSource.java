package org.github.gestalt.config.source;

import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.utils.Pair;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Load a config source from a classpath resource using the getResourceAsStream method.
 *
 * @author Colin Redmond
 */
public class ClassPathConfigSource implements ConfigSource {
    private final String resource;
    private final UUID id = UUID.randomUUID();

    public ClassPathConfigSource(String resource) throws GestaltException {
        this.resource = resource;
        if (resource == null) {
            throw new GestaltException("Class path resource cannot be null");
        }
    }

    @Override
    public boolean hasStream() {
        return true;
    }

    @Override
    public InputStream loadStream() throws GestaltException {
        InputStream is = ClassPathConfigSource.class.getResourceAsStream(resource);
        if (is == null) {
            throw new GestaltException("Unable to load classpath resource from " + resource);
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

    protected String format(String fileName) {
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

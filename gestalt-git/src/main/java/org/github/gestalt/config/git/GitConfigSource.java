package org.github.gestalt.config.git;


import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.utils.Pair;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Loads a file from git.
 *
 * @author Colin Redmond
 */
public class GitConfigSource implements ConfigSource {
    private String path;
    private final UUID id = UUID.randomUUID();

    public GitConfigSource() throws GestaltException {

    }

    @Override
    public boolean hasStream() {
        return true;
    }

    @Override
    public InputStream loadStream() throws GestaltException {

    }

    @Override
    public boolean hasList() {
        return false;
    }

    @Override
    public List<Pair<String, String>> loadList() throws GestaltException {
        throw new GestaltException("Unsupported operation loadList on an S3ConfigSource");
    }

    @Override
    public String format() {
        return format(path);
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
        return "Git Config Source key: ";
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
        if (!(o instanceof GitConfigSource)) {
            return false;
        }
        GitConfigSource that = (GitConfigSource) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

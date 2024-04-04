package org.github.gestalt.config.source;

import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.Pair;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TestSource implements ConfigSource {

    private final UUID id;
    private final Tags tags;

    public TestSource() {
        this.tags = Tags.of();
        this.id = UUID.randomUUID();
    }

    public TestSource(Tags tags) {
        this.tags = tags;
        this.id = UUID.randomUUID();
    }

    public TestSource(UUID id) {
        this.id = id;
        this.tags = Tags.of();
    }

    public TestSource(UUID id, Tags tags) {
        this.id = id;
        this.tags = tags;
    }

    @Override
    public boolean hasStream() {
        return false;
    }

    @Override
    public InputStream loadStream() {
        return null;
    }

    @Override
    public boolean hasList() {
        return false;
    }

    @Override
    public List<Pair<String, String>> loadList() {
        return null;
    }

    @Override
    public String format() {
        return null;
    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public UUID id() {      //NOPMD
        return id;
    }

    @Override
    @SuppressWarnings("removal")
    public Tags getTags() {
        return tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TestSource)) {
            return false;
        }
        TestSource that = (TestSource) o;
        return Objects.equals(id, that.id) && Objects.equals(tags, that.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, tags);
    }
}

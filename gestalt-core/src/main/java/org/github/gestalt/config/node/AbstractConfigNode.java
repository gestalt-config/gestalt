package org.github.gestalt.config.node;

import org.github.gestalt.config.metadata.MetaDataValue;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.secret.rules.SecretConcealer;

import java.util.*;

public abstract class AbstractConfigNode implements ConfigNode {
    protected final Map<String, List<MetaDataValue<?>>> metadata;

    public AbstractConfigNode(Map<String, List<MetaDataValue<?>>> metadata) {
        this.metadata = Map.copyOf(Objects.requireNonNullElse(metadata, Collections.emptyMap()));
    }

    @Override
    public abstract NodeType getNodeType();

    @Override
    public Optional<String> getValue() {
        return Optional.empty();
    }

    @Override
    public boolean hasValue() {
        return false;
    }

    @Override
    public Optional<ConfigNode> getIndex(int index) {
        return Optional.empty();
    }

    @Override
    public Optional<ConfigNode> getKey(String key) {
        return Optional.empty();
    }

    @Override
    public int size() { //NOPMD
        return 0;
    }

    @Override
    public List<MetaDataValue<?>> getMetadata(String key) {
        return metadata.getOrDefault(key, List.of());
    }

    @Override
    public Map<String, List<MetaDataValue<?>>> getMetadata() {
        return metadata;
    }

    @Override
    public boolean hasMetadata(String key) {
        return metadata.containsKey(key);
    }


    @Override
    public abstract String printer(String path, SecretConcealer secretConcealer, SentenceLexer lexer);
}

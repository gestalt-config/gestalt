package org.github.gestalt.config.entity;

import org.github.gestalt.config.decoder.DecoderContext;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.reload.CoreReloadListener;
import org.github.gestalt.config.tag.Tags;

import java.util.Optional;

/**
 * A Container for a configuration value that supports dynamic config reloading.
 * Allows users to get the config value.
 *
 * @param <T> Type of the Config Container
 */
public class ConfigContainer<T> implements CoreReloadListener {

    private static final System.Logger logger = System.getLogger(ConfigContainer.class.getName());

    // Path used to get the config on reload.
    protected final String path;
    // Tags used to get the config on reload.
    protected final Tags tags;
    // Decoder Context hold gestalt to get the config on reload.
    protected final DecoderContext decoderContext;
    // Type of the Configuration value.
    protected final TypeCapture<T> klass;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    Optional<T> value;

    public ConfigContainer(String path, Tags tags, DecoderContext decoderContext, T value, TypeCapture<T> klass) {
        this.path = path;
        this.tags = tags;
        this.decoderContext = decoderContext;
        this.klass = klass;
        this.value = Optional.of(value);
    }

    public T getValue() throws GestaltException {
        return value.orElseThrow(() -> new GestaltException("No results for config path: " + path + ", tags: " + tags +
            ", and class: " + klass.getName()));
    }

    public void setValue(T value) {
        this.value = Optional.of(value);
    }

    @Override
    public void reload() {
        value = decoderContext.getGestalt().getConfigOptional(path, klass, tags);

        if (value.isEmpty()) {
           logger.log(System.Logger.Level.WARNING, "On Reload, no results for config path: " + path + ", tags: " + tags +
               ", and class: " + klass.getName());
        }
    }
}

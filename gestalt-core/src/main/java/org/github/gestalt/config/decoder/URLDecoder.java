package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Decode a URL.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class URLDecoder extends LeafDecoder<URL> {

    private boolean treatEmptyStringsAsNull = false;

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "URL";
    }

    @Override
    public void applyConfig(GestaltConfig config) {
        this.treatEmptyStringsAsNull = config.isTreatEmptyStringsAsNull();
    }

    @Override
    public boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type) {
        return URL.class.isAssignableFrom(type.getRawType());
    }

    @Override
    protected GResultOf<URL> leafDecode(String path, ConfigNode node, DecoderContext decoderContext) {
        var value = node.getValue().orElse("");

        // Check if empty string should be treated as null
        if (value.isEmpty() && treatEmptyStringsAsNull) {
            return GResultOf.result(null);
        }

        try {
            return GResultOf.result(new URL(value));
        } catch (MalformedURLException e) {
            return GResultOf.errors(
                new ValidationError.ErrorDecodingException(path, node, name(), e.getLocalizedMessage(), decoderContext));
        }
    }
}

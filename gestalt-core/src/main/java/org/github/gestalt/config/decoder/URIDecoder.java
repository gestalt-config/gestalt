package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Decode a URL.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class URIDecoder extends LeafDecoder<URI> {

    private boolean treatEmptyStringsAsNull = false;

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "URI";
    }

    @Override
    public void applyConfig(GestaltConfig config) {
        this.treatEmptyStringsAsNull = config.isTreatEmptyStringsAsNull();
    }

    @Override
    public boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type) {
        return URI.class.isAssignableFrom(type.getRawType());
    }

    @Override
    protected GResultOf<URI> leafDecode(String path, ConfigNode node, DecoderContext decoderContext) {
        var value = node.getValue().orElse("");

        // Check if empty string should be treated as null
        if (value.isEmpty() && treatEmptyStringsAsNull) {
            return GResultOf.result(null);
        }

        try {
            return GResultOf.result(new URI(value));
        } catch (URISyntaxException e) {
            return GResultOf.errors(
                new ValidationError.ErrorDecodingException(path, node, name(), e.getLocalizedMessage(), decoderContext));
        }
    }
}

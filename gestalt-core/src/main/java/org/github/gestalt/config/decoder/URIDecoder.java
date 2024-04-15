package org.github.gestalt.config.decoder;

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
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class URIDecoder extends LeafDecoder<URI> {

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "URI";
    }

    @Override
    public boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type) {
        return URI.class.isAssignableFrom(type.getRawType());
    }

    @Override
    protected GResultOf<URI> leafDecode(String path, ConfigNode node, DecoderContext decoderContext) {
        var value = node.getValue().orElse("");
        try {
            return GResultOf.result(new URI(value));
        } catch (URISyntaxException e) {
            return GResultOf.errors(
                new ValidationError.ErrorDecodingException(path, node, name(), e.getLocalizedMessage(), decoderContext));
        }
    }
}

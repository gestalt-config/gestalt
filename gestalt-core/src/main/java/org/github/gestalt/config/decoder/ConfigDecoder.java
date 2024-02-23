package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.Config;
import org.github.gestalt.config.entity.ConfigContainer;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;

import java.util.Optional;

/**
 * Decodes a generic optional type.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class ConfigDecoder implements Decoder<ConfigContainer<?>> {

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "Config";
    }

    @Override
    public boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type) {
        return ConfigContainer.class.isAssignableFrom(type.getRawType());
    }

    @Override
    public GResultOf<ConfigContainer<?>> decode(String path, Tags tags, ConfigNode node, TypeCapture<?> type, DecoderContext decoderContext) {
        // decode the generic type of the optional. Then we will wrap the result into an Optional
        GResultOf<?> configValue = decoderContext.getDecoderService()
            .decodeNode(path, tags, node, type.getFirstParameterType(), decoderContext);

            return configValue.mapWithError((result) -> new ConfigContainer(path, tags, decoderContext, result, type));
        }
    }
}

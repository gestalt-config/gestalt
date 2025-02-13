package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.StringUtils;

import java.time.Duration;

/**
 * Decode a duration.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class DurationDecoder extends LeafDecoder<Duration> {

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "Duration";
    }

    @Override
    public boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type) {
        return Duration.class.isAssignableFrom(type.getRawType());
    }

    @Override
    protected GResultOf<Duration> leafDecode(String path, ConfigNode node, DecoderContext decoderContext) {
        GResultOf<Duration> results;

        String value = node.getValue().orElse("");
        if (StringUtils.isInteger(value)) {
            try {
                long longVal = Long.parseLong(value);
                results = GResultOf.result(Duration.ofMillis(longVal));
            } catch (NumberFormatException e) {
                results = GResultOf.errors(
                    new ValidationError.ErrorDecodingException(path, node, name(), e.getMessage(), decoderContext));
            }
        } else {
            try {
                results = GResultOf.result(Duration.parse(value));
            } catch (Exception e) {
                results = GResultOf.errors(
                    new ValidationError.ErrorDecodingException(path, node, name(), e.getMessage(), decoderContext));
            }
        }
        return results;
    }
}

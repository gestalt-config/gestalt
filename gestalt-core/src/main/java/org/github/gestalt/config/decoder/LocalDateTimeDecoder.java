package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Decode a LocalDateTime.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class LocalDateTimeDecoder extends LeafDecoder<LocalDateTime> {

    private DateTimeFormatter formatter;

    /**
     * Default local date time decoder using ISO_DATE_TIME.
     */
    public LocalDateTimeDecoder() {
        this.formatter = DateTimeFormatter.ISO_DATE_TIME;
    }

    /**
     * Local Date time decode that takes a formatter.
     *
     * @param formatter DateTimeFormatter pattern
     */
    public LocalDateTimeDecoder(String formatter) {
        if (formatter != null && !formatter.isEmpty()) {
            this.formatter = DateTimeFormatter.ofPattern(formatter);
        } else {
            this.formatter = DateTimeFormatter.ISO_DATE_TIME;
        }
    }

    @Override
    public void applyConfig(GestaltConfig config) {
        if (config.getDateDecoderFormat() != null && this.formatter.equals(DateTimeFormatter.ISO_DATE_TIME)) {
            this.formatter = config.getDateDecoderFormat();
        }
    }

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "LocalDateTime";
    }

    @Override
    public boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type) {
        return LocalDateTime.class.isAssignableFrom(type.getRawType());
    }

    @Override
    protected GResultOf<LocalDateTime> leafDecode(String path, ConfigNode node, DecoderContext decoderContext) {
        GResultOf<LocalDateTime> results;

        String value = node.getValue().orElse("");
        try {
            results = GResultOf.result(LocalDateTime.parse(value, formatter));
        } catch (DateTimeParseException e) {
            results = GResultOf.errors(
                new ValidationError.ErrorDecodingException(path, node, name(), e.getMessage(), decoderContext.getSecretConcealer()));
        }
        return results;
    }
}

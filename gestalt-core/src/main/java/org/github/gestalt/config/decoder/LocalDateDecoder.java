package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Decode a LocalDate.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class LocalDateDecoder extends LeafDecoder<LocalDate> {

    private DateTimeFormatter formatter;

    /**
     * Default local date decoder using ISO_DATE_TIME.
     */
    public LocalDateDecoder() {
        this.formatter = DateTimeFormatter.ISO_LOCAL_DATE;
    }

    /**
     * Local Date decode that takes a formatter.
     *
     * @param formatter DateTimeFormatter pattern
     */
    public LocalDateDecoder(String formatter) {
        if (formatter != null && !formatter.isEmpty()) {
            this.formatter = DateTimeFormatter.ofPattern(formatter);
        } else {
            this.formatter = DateTimeFormatter.ISO_LOCAL_DATE;
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
        return "LocalDate";
    }

    @Override
    public boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type) {
        return LocalDate.class.isAssignableFrom(type.getRawType());
    }

    @Override
    protected GResultOf<LocalDate> leafDecode(String path, ConfigNode node) {
        GResultOf<LocalDate> results;

        String value = node.getValue().orElse("");
        try {
            results = GResultOf.result(LocalDate.parse(value, formatter));
        } catch (DateTimeParseException e) {
            results = GResultOf.errors(new ValidationError.ErrorDecodingException(path, node, name()));
        }
        return results;
    }
}

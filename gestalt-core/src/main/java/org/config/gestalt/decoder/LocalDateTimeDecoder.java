package org.config.gestalt.decoder;

import org.config.gestalt.entity.ValidationError;
import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.utils.ValidateOf;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Decode a LocalDateTime
 *
 * @author Colin Redmond
 */
public class LocalDateTimeDecoder extends LeafDecoder<LocalDateTime> {

    private final DateTimeFormatter formatter;

    public LocalDateTimeDecoder() {
        this.formatter = DateTimeFormatter.ISO_DATE_TIME;
    }

    public LocalDateTimeDecoder(String formatter) {
        if (formatter != null && !formatter.isEmpty()) {
            this.formatter = DateTimeFormatter.ofPattern(formatter);
        } else {
            this.formatter = DateTimeFormatter.ISO_DATE_TIME;
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
    public boolean matches(TypeCapture<?> klass) {
        return klass.isAssignableFrom(LocalDateTime.class);
    }

    @Override
    protected ValidateOf<LocalDateTime> leafDecode(String path, ConfigNode node) {
        ValidateOf<LocalDateTime> results;

        String value = node.getValue().orElse("");
        try {
            results = ValidateOf.valid(LocalDateTime.parse(value, formatter));
        } catch (DateTimeParseException e) {
            results = ValidateOf.inValid(new ValidationError.ErrorDecodingException(path, node, name()));
        }
        return results;
    }
}

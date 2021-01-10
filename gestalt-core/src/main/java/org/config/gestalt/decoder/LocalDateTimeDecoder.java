package org.config.gestalt.decoder;

import org.config.gestalt.entity.ValidationError;
import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.utils.ValidateOf;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class LocalDateTimeDecoder extends LeafDecoder<LocalDateTime> {

    private final DateTimeFormatter format;

    public LocalDateTimeDecoder() {
        this.format = DateTimeFormatter.ISO_DATE_TIME;
    }

    public LocalDateTimeDecoder(String format) {
        this.format = DateTimeFormatter.ofPattern(format);
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
            results = ValidateOf.valid(LocalDateTime.parse(value, format));
        } catch (DateTimeParseException e) {
            results = ValidateOf.inValid(new ValidationError.ErrorDecodingException(path, node, name()));
        }
        return results;
    }
}

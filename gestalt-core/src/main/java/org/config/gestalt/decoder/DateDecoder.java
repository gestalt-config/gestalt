package org.config.gestalt.decoder;

import org.config.gestalt.entity.ValidationError;
import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.utils.ValidateOf;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

public class DateDecoder extends LeafDecoder<Date> {

    private final DateTimeFormatter formatter;

    public DateDecoder() {
        this.formatter = DateTimeFormatter.ISO_DATE_TIME;
    }

    public DateDecoder(String formatter) {
        if(formatter  != null && !formatter.isEmpty()) {
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
        return "Date";
    }

    @Override
    public boolean matches(TypeCapture<?> klass) {
        return klass.isAssignableFrom(Date.class);
    }

    @Override
    protected ValidateOf<Date> leafDecode(String path, ConfigNode node) {
        ValidateOf<Date> results;

        String value = node.getValue().orElse("");
        try {
            LocalDateTime ldt = LocalDateTime.parse(value, formatter);
            Instant instant = ldt.atZone(ZoneId.systemDefault()).toInstant();
            results = ValidateOf.valid(Date.from(instant));
        } catch (DateTimeParseException e) {
            results = ValidateOf.inValid(new ValidationError.ErrorDecodingException(path, node, name()));
        }
        return results;
    }
}

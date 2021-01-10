package org.config.gestalt.decoder;

import org.config.gestalt.entity.ValidationError;
import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.utils.ValidateOf;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class LocalDateDecoder extends LeafDecoder<LocalDate> {

    private final DateTimeFormatter formatter;

    public LocalDateDecoder() {
        this.formatter = DateTimeFormatter.ISO_LOCAL_DATE;
    }

    public LocalDateDecoder(String formatter) {
        if (formatter != null && !formatter.isEmpty()) {
            this.formatter = DateTimeFormatter.ofPattern(formatter);
        } else {
            this.formatter = DateTimeFormatter.ISO_LOCAL_DATE;
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
    public boolean matches(TypeCapture<?> klass) {
        return klass.isAssignableFrom(LocalDate.class);
    }

    @Override
    protected ValidateOf<LocalDate> leafDecode(String path, ConfigNode node) {
        ValidateOf<LocalDate> results;

        String value = node.getValue().orElse("");
        try {
            results = ValidateOf.valid(LocalDate.parse(value, formatter));
        } catch (DateTimeParseException e) {
            results = ValidateOf.inValid(new ValidationError.ErrorDecodingException(path, node, name()));
        }
        return results;
    }
}

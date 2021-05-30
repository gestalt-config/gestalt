package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.utils.ValidateOf;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Decode a LocalDate.
 *
 * @author Colin Redmond
 */
public class LocalDateDecoder extends LeafDecoder<LocalDate> {

    private final DateTimeFormatter formatter;

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

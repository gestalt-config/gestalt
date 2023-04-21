package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.utils.ValidateOf;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

/**
 * Decode a date.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public class DateDecoder extends LeafDecoder<Date> {

    private DateTimeFormatter formatter;

    /**
     * Default date decoder using ISO_DATE_TIME.
     */
    public DateDecoder() {
        this.formatter = DateTimeFormatter.ISO_DATE_TIME;
    }

    /**
     * Date decode that takes a formatter.
     *
     * @param formatter DateTimeFormatter pattern
     */
    public DateDecoder(String formatter) {
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
        return "Date";
    }

    @Override
    public boolean matches(TypeCapture<?> klass) {
        return Date.class.isAssignableFrom(klass.getRawType());
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

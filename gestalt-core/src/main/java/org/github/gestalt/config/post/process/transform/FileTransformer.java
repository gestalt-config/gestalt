package org.github.gestalt.config.post.process.transform;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.utils.ValidateOf;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Allows you to load the contents of a file into a string substitution.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public class FileTransformer implements Transformer {

    private final int prefixLength = (name() + ":").length();       // NOPMD

    @Override
    public String name() {
        return "file";
    }

    @Override
    public ValidateOf<String> process(String path, String key, String rawValue) {
        if (key != null) {
            try {
                String filePath = rawValue.substring(prefixLength);
                return ValidateOf.valid(Files.readString(Path.of(filePath), Charset.defaultCharset()));
            } catch (Exception e) {
                return ValidateOf.inValid(new ValidationError.ExceptionReadingFileDuringTransform(path, key, e.getMessage()));
            }
        } else {
            return ValidateOf.inValid(new ValidationError.InvalidStringSubstitutionPostProcess(path, rawValue, name()));
        }
    }
}

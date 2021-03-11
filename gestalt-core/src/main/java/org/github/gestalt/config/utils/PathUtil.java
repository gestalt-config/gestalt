package org.github.gestalt.config.utils;

import org.github.gestalt.config.token.ArrayToken;
import org.github.gestalt.config.token.ObjectToken;
import org.github.gestalt.config.token.TagToken;
import org.github.gestalt.config.token.Token;

import java.util.List;

/**
 * Utility class for paths.
 *
 * @author Colin Redmond
 */
public final class PathUtil {
    private PathUtil() {

    }

    /**
     * Returns the path for a list of tokens.
     *
     * @param tokens list of tokens on the path.
     * @return the path built from the tokens
     */
    public static String toPath(List<Token> tokens) {
        StringBuilder pathBuilder = new StringBuilder();
        tokens.forEach(token -> {
            if (token instanceof ObjectToken) {
                if (pathBuilder.length() != 0) {
                    pathBuilder.append('.');
                }
                pathBuilder.append(((ObjectToken) token).getName());
            } else if (token instanceof ArrayToken) {
                pathBuilder.append('[');
                pathBuilder.append(((ArrayToken) token).getIndex());
                pathBuilder.append(']');
            } else if (token instanceof TagToken) {
                pathBuilder.append(((TagToken) token).getTag());
                pathBuilder.append('=');
                pathBuilder.append(((TagToken) token).getValue());
            }
        });

        return pathBuilder.toString();
    }

    public static String pathForKey(String path, String key) {
        return path == null || path.isEmpty() ? key : path + "." + key;
    }

    public static String pathForIndex(String path, int index) {
        return path == null || path.isEmpty() ? "[" + index + "]" : path + "[" + index + "]";
    }
}


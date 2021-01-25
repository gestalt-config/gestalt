package org.config.gestalt.utils;

import org.config.gestalt.token.ArrayToken;
import org.config.gestalt.token.ObjectToken;
import org.config.gestalt.token.TagToken;
import org.config.gestalt.token.Token;

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
}


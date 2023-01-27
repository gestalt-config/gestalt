package org.github.gestalt.config.utils;

import org.github.gestalt.config.token.ArrayToken;
import org.github.gestalt.config.token.ObjectToken;
import org.github.gestalt.config.token.TagToken;
import org.github.gestalt.config.token.Token;

import java.util.List;

/**
 * Utility class for paths.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
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

    /**
     * used to generate a path wit the next key in the format path.key .
     *
     * @param path current path
     * @param key current key
     * @return path for key
     */
    public static String pathForKey(String path, String key) {
        return path == null || path.isEmpty() ? key : path + "." + key;
    }

    /**
     * used to generate a path wit the next index in the format path[index] .
     *
     * @param path current path
     * @param index current index
     * @return path for index
     */
    public static String pathForIndex(String path, int index) {
        return path == null || path.isEmpty() ? "[" + index + "]" : path + "[" + index + "]";
    }
}


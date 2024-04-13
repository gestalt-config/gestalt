package org.github.gestalt.config.utils;

import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.token.ArrayToken;
import org.github.gestalt.config.token.ObjectToken;
import org.github.gestalt.config.token.TagToken;
import org.github.gestalt.config.token.Token;

import java.util.List;

/**
 * Utility class for paths.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class PathUtil {
    private PathUtil() {

    }

    /**
     * Returns the path for a list of tokens.
     *
     * @param lexer lexer used to get the delimiter to build the path
     * @param tokens list of tokens on the path.
     * @return the path built from the tokens
     */
    public static String toPath(SentenceLexer lexer, List<Token> tokens) {
        StringBuilder pathBuilder = new StringBuilder();
        tokens.forEach(token -> {
            if (token instanceof ObjectToken) {
                if (pathBuilder.length() != 0) {
                    pathBuilder.append(lexer.getNormalizedDeliminator());
                }
                pathBuilder.append(((ObjectToken) token).getName());
            } else if (token instanceof ArrayToken) {
                pathBuilder.append(lexer.getNormalizedArrayOpenTag());
                pathBuilder.append(((ArrayToken) token).getIndex());
                pathBuilder.append(lexer.getNormalizedArrayCloseTag());
            } else if (token instanceof TagToken) {
                pathBuilder.append(((TagToken) token).getTag());
                pathBuilder.append(lexer.getNormalizedMapTag());
                pathBuilder.append(((TagToken) token).getValue());
            }
        });

        return pathBuilder.toString();
    }

    /**
     * used to generate a path wit the next key in the format path.key .
     *
     * @param lexer lexer used to get the delimiter to build the path
     * @param path current path
     * @param key  current key
     * @return path for key
     */
    public static String pathForKey(SentenceLexer lexer, String path, String key) {
        return path == null || path.isEmpty() ? key : path + lexer.getNormalizedDeliminator() + key;
    }

    /**
     * used to generate a path wit the next index in the format path[index] .
     *
     * @param lexer lexer used to get the delimiter to build the path
     * @param path  current path
     * @param index current index
     * @return path for index
     */
    public static String pathForIndex(SentenceLexer lexer, String path, int index) {
        return path == null || path.isEmpty() ? forIndex(lexer, index) : path + forIndex(lexer, index);
    }

    /**
     * used to generate the next index in the format [index] .
     *
     * @param lexer lexer used to get the delimiter to build the path
     * @param index current index
     * @return path for index
     */
    public static String forIndex(SentenceLexer lexer, int index) {
        return  lexer.getNormalizedArrayOpenTag() + index + lexer.getNormalizedArrayCloseTag();
    }
}


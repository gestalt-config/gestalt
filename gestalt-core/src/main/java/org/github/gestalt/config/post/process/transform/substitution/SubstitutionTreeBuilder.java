package org.github.gestalt.config.post.process.transform.substitution;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.utils.Pair;
import org.github.gestalt.config.utils.ValidateOf;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builds a substitution tree to support nested substitutions.
 *
 * @author Colin Redmond (c) 2023.
 */
public class SubstitutionTreeBuilder {

    private final String openingToken;
    private final String closingToken;

    private static final int escapeChar = '\\';

    private final int firstOpeningToken;
    private final int openingTokenLength;
    private final int firstClosingToken;
    private final int closingTokenLength;

    private final Pattern patternReplaceOpen;
    private final Pattern patternReplaceClose;

    public SubstitutionTreeBuilder(String openingToken, String closingToken) {
        this.openingToken = openingToken;
        this.closingToken = closingToken;

        this.firstOpeningToken = openingToken.codePointAt(0);
        this.openingTokenLength = openingToken.length();
        this.firstClosingToken = closingToken.codePointAt(0);
        this.closingTokenLength = closingToken.length();

        this.patternReplaceOpen = Pattern.compile(Pattern.quote(Character.toString(escapeChar) + openingToken));
        this.patternReplaceClose = Pattern.compile(Pattern.quote(Character.toString(escapeChar) + closingToken));
    }

    public ValidateOf<List<SubstitutionNode>> build(String path, String value) {
        List<SubstitutionNode> nodes;
        var results = buildInternal(path, value, 0, 0);

        if (results.hasResults() && results.results().getFirst() instanceof SubstitutionNode.TransformNode) {
            nodes = ((SubstitutionNode.TransformNode) results.results().getFirst()).getSubNodes();
        } else {
            nodes = new ArrayList<>();
        }
        return ValidateOf.validateOf(nodes, results.getErrors());
    }

    public ValidateOf<Pair<SubstitutionNode, Integer>> buildInternal(String path, String value, int index, int depth) {
        List<ValidationError> errors = new ArrayList<>();
        List<SubstitutionNode> nodes = new ArrayList<>();

        int lastNodeEnd = index;
        int length = value.length();
        for (int i = index; i < length; i++) {
            int codePointAt = value.codePointAt(i);

            // check if this is the opening token
            if (firstOpeningToken == codePointAt &&
                (i == 0 || value.codePointAt(i - 1) != escapeChar) &&
                value.startsWith(openingToken, i)) {
                if (i - lastNodeEnd > 0) {
                    String text = getEscapedText(value, lastNodeEnd, i);
                    nodes.add(new SubstitutionNode.TextNode(text));
                }

                // lets skip ahead the size of the opening token.
                i += openingTokenLength;

                ValidateOf<Pair<SubstitutionNode, Integer>> nestedNodes = buildInternal(path, value, i, depth + 1);

                errors.addAll(nestedNodes.getErrors());
                if (nestedNodes.hasResults()) {
                    nodes.add(nestedNodes.results().getFirst());
                }

                // forward the counter by the length of the closing token -1 (since the next loop will increment by 1)
                i = nestedNodes.results().getSecond() - 1;
                lastNodeEnd = nestedNodes.results().getSecond();
            } else if (firstClosingToken == codePointAt &&
                (i == 0 || value.codePointAt(i - 1) != escapeChar) &&
                value.startsWith(closingToken, i)) {

                if (depth == 0) {
                    errors.add(new ValidationError.UnexpectedClosingTokenTransform(path, value, closingToken, i));
                } else {
                    if (lastNodeEnd != i) {
                        String text = getEscapedText(value, lastNodeEnd, i);
                        // if there is some text from the end of the last node add a new text node.
                        nodes.add(new SubstitutionNode.TextNode(text));
                    }

                    i += closingTokenLength;

                    return ValidateOf.validateOf(new Pair<>(new SubstitutionNode.TransformNode(nodes), i), errors);
                }
            }
        }

        if (depth != 0) {
            errors.add(new ValidationError.UnclosedSubstitutionTransform(path, value));
        }

        // if there is some text from the end of the last node add a new text node.
        // of if this is an empty string. (to ensure, we have at least one text node)
        if (lastNodeEnd != length || length == 0) {
            String text = getEscapedText(value, lastNodeEnd, length);
            // if there is some text from the end of the last node add a new text node.
            nodes.add(new SubstitutionNode.TextNode(text));
        }

        return ValidateOf.validateOf(new Pair<>(new SubstitutionNode.TransformNode(nodes), length), errors);
    }

    private String getEscapedText(String value, int lastNodeEnd, int i) {
        String text = value.substring(lastNodeEnd, i);
        text = patternReplaceOpen.matcher(text).replaceAll(Matcher.quoteReplacement(openingToken));
        text = patternReplaceClose.matcher(text).replaceAll(Matcher.quoteReplacement(closingToken));
        return text;
    }

}

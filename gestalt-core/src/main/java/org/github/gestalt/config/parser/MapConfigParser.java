package org.github.gestalt.config.parser;

import org.github.gestalt.config.entity.ConfigValue;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ArrayNode;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.node.MapNode;
import org.github.gestalt.config.token.ArrayToken;
import org.github.gestalt.config.token.ObjectToken;
import org.github.gestalt.config.token.Token;
import org.github.gestalt.config.utils.CollectionUtils;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.Pair;
import org.github.gestalt.config.utils.PathUtil;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Takes in a tokenized config and returns a config node tree.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class MapConfigParser implements ConfigParser {
    private static final System.Logger logger = System.getLogger(MapConfigParser.class.getName());

    @Override
    public GResultOf<ConfigNode> parse(SentenceLexer lexer, List<Pair<List<Token>, ConfigValue>> configs, boolean failOnErrors) {
        return buildConfigTree(lexer, configs, 0, failOnErrors);
    }

    /**
     * Recursive call to group each section of config that have the same path to this paint until we hit the leaf.
     * Then return the leaf
     * Use the children leaf to build the config tree
     *
     * @param tokens       group of tokens that represent an object Array or Map config node
     * @param index        the depth of the currant group of config lists we are looking at.
     * @param failOnErrors Results can be unpredictable if it continues
     * @return the ConfigNode root for the configurations at this point.
     */
    GResultOf<ConfigNode> buildConfigTree(SentenceLexer lexer, List<Pair<List<Token>, ConfigValue>> tokens, int index,
                                          boolean failOnErrors) {

        if (tokens == null || tokens.isEmpty()) {
            return GResultOf.errors(new ValidationError.EmptyToken());
        }

        // build the current path, mostly for logging.
        String currentPath = PathUtil.toPath(lexer, tokens.get(0).getFirst().subList(0, index));
        List<ValidationError> errorList = new ArrayList<>();

        // if there is only 1 token and we are at the end of the path return a valid leaf.
        if (tokens.size() == 1 && tokens.get(0).getFirst().size() <= index) {
            ConfigValue configValue = tokens.get(0).getSecond();
            return GResultOf.result(new LeafNode(configValue.getValue()));
        }

        // result any mis-matched path's, this is most like when a path is both a duplicate, or leaf and an object and an array.
        List<ValidationError> mismatchedPathLengthErrors = getPathLengthErrors(tokens, index, currentPath);
        if (!mismatchedPathLengthErrors.isEmpty()) {
            return GResultOf.errors(mismatchedPathLengthErrors);
        }

        // group the tokens at the index, for example all object tokens with the same name will be grouped, or arrays with the same index.
        // these grouped objects represent a child object
        Map<Token, List<Pair<List<Token>, ConfigValue>>> tokensAtIndexGrouped =
            tokens.stream().collect(Collectors.groupingBy(tokenPair -> tokenPair.getFirst().get(index)));

        // a distinct list of all token types in this level of the config tree branch
        List<Token> tokenTypes = tokensAtIndexGrouped.keySet().stream()
            .filter(CollectionUtils.distinctBy(Token::getClass))
            .collect(Collectors.toList());

        // do some validation on the node.
        if (tokenTypes.isEmpty()) {
            errorList.add(new ValidationError.NoTokensInPath(currentPath));
        } else if (tokenTypes.size() > 1) {
            // if there is more than one token type we add the error
            errorList.add(new ValidationError.MultipleTokenTypes(currentPath, tokenTypes));
        } else {
            // if there is only 1 token type all tokens at this level of the config tree are the same.
            if (tokenTypes.get(0) instanceof ArrayToken) {
                // result possible array errors.
                errorList.addAll(validateArrayInvalidIndex(tokens, index, currentPath));
                errorList.addAll(validateArrayMissingIndex(tokens, index, currentPath));
                errorList.addAll(validateArrayDuplicateLeafIndex(tokensAtIndexGrouped, index, currentPath));
                errorList.addAll(validateArrayLeafAndNonLeaf(tokensAtIndexGrouped, index, currentPath));
            } else if (!(tokenTypes.get(0) instanceof ObjectToken)) {
                // if this is not a ArrayToken or a ObjectToken then it is an unknown token.
                errorList.add(new ValidationError.UnknownTokenWithPath(tokenTypes.get(0), currentPath));
            }
        }

        //if there are any Error level validation issues don't continue validating of the sub tree.
        if (errorList.stream().anyMatch(it -> it.level().equals(ValidationLevel.ERROR))) {
            return GResultOf.errors(errorList);
        }

        // group all similar tokens, then recursively call buildConfigTree with them.
        List<Pair<Token, GResultOf<ConfigNode>>> configsValidateOf =
            tokens.stream()
                .collect(Collectors.groupingBy(tokenPair -> tokenPair.getFirst().get(index)))
                .entrySet()
                .stream()
                .map(entry -> new Pair<>(entry.getKey(), buildConfigTree(lexer, entry.getValue(), index + 1, failOnErrors)))
                .collect(Collectors.toList());

        // Get any errors and split them by level.
        Map<ValidationLevel, List<ValidationError>> recursiveErrors =
            configsValidateOf.stream()
                .map(it -> it.getSecond().getErrors())
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(ValidationError::level));

        // add all warning errors to the current error list.
        if (recursiveErrors.containsKey(ValidationLevel.WARN)) {
            errorList.addAll(recursiveErrors.get(ValidationLevel.WARN));
        }

        // add all missing values errors to the current error list.
        if (recursiveErrors.containsKey(ValidationLevel.MISSING_VALUE)) {
            errorList.addAll(recursiveErrors.get(ValidationLevel.MISSING_VALUE));
        }

        // if there are any error level return immediately unless we have treatErrorsAsWarnings enabled.
        if (recursiveErrors.containsKey(ValidationLevel.ERROR)) {
            errorList.addAll(recursiveErrors.get(ValidationLevel.ERROR));
            if (failOnErrors) {
                return GResultOf.errors(errorList);
            }
        }

        // pull out the valid config nodes.
        List<Pair<Token, ConfigNode>> configs =
            configsValidateOf.stream()
                .filter(it -> (!failOnErrors || !it.getSecond().hasErrors(ValidationLevel.ERROR)) &&
                    it.getSecond().hasResults())
                .map(it -> new Pair<>(it.getFirst(), it.getSecond().results()))
                .collect(Collectors.toList());


        // There should only be one node type for this group of config as we have already validated it
        if (configsValidateOf.isEmpty()) {
            logger.log(System.Logger.Level.WARNING, "unable to parse tokens and create config node");
        } else if (configs.isEmpty()) {
            logger.log(System.Logger.Level.WARNING, "No configs found");
        } else {
            Token token = configs.get(0).getFirst();
            ConfigNode result = null;
            if (token instanceof ObjectToken) {
                result = new MapNode(configs.stream()
                    .collect(Collectors.toMap(it -> ((ObjectToken) it.getFirst()).getName(), Pair::getSecond)));
            } else if (token instanceof ArrayToken) {
                OptionalInt maxArrayInt = configs.stream()
                    .map(it -> ((ArrayToken) it.getFirst()).getIndex()).mapToInt(Integer::intValue).max();

                ConfigNode[] arrayNodes = new ConfigNode[maxArrayInt.orElse(0) + 1];

                configs.forEach(config -> arrayNodes[((ArrayToken) config.getFirst()).getIndex()] = config.getSecond());
                result = new ArrayNode(Arrays.asList(arrayNodes));
            }

            if (result != null) {
                return GResultOf.resultOf(result, errorList);
            }
        }

        return GResultOf.errors(new ValidationError.NoResultsFoundForPath(currentPath));
    }

    /**
     * Return a list of errors for any array tokens that have an index less than 0.
     *
     * @param tokens      array tokens to result
     * @param index       the index or depth in the tree we are analyzing.
     * @param currentPath the current path.
     * @return list of errors for any array tokens that have an index less than 0
     */
    private List<ValidationError> validateArrayInvalidIndex(List<Pair<List<Token>, ConfigValue>> tokens, int index, String currentPath) {

        return tokens.stream().map(token -> (ArrayToken) token.getFirst().get(index))
            .filter(arrayToken -> arrayToken.getIndex() < 0)
            .map(arrayToken -> new ValidationError.ArrayInvalidIndex(arrayToken.getIndex(), currentPath))
            .collect(Collectors.toList());
    }

    private List<ValidationError> validateArrayMissingIndex(List<Pair<List<Token>, ConfigValue>> tokens, int index, String currentPath) {
        // Counts for each array index.
        Map<Integer, Long> arrayIndexCounts = tokens.stream()
            .map(token -> (ArrayToken) token.getFirst().get(index))
            .filter(arrayToken -> arrayToken.getIndex() >= 0)
            .collect(Collectors.groupingBy(ArrayToken::getIndex, Collectors.counting()));

        // result that we are not missing any index's in the array
        long maxIndex = arrayIndexCounts.keySet().stream().max(Comparator.comparing(Long::valueOf)).orElse(-1);

        return IntStream.rangeClosed(0, Math.toIntExact(maxIndex)).filter(it -> !arrayIndexCounts.containsKey(it))
            .mapToObj(it -> new ValidationError.ArrayMissingIndex(it, currentPath))
            .collect(Collectors.toList());
    }

    private List<ValidationError> validateArrayLeafAndNonLeaf(Map<Token, List<Pair<List<Token>, ConfigValue>>> tokensAtIndexGrouped,
                                                              int index, String currentPath) {
        // Build a list of all path sizes in the current set of tokens.
        List<Integer> pathSizes = tokensAtIndexGrouped.values().stream()
            .flatMap(Collection::stream)
            .map(it -> it.getFirst().size())
            .filter(it -> it >= index + 1)
            .distinct()
            .collect(Collectors.toList());

        // if any paths are of current index length and we have more than 1, we are both at a leaf and sub-object
        if (pathSizes.contains(index + 1) && pathSizes.size() > 1) {
            return Collections.singletonList(new ValidationError.ArrayLeafAndNotLeaf(pathSizes, currentPath));
        }

        return Collections.emptyList();
    }

    private List<ValidationError> validateArrayDuplicateLeafIndex(Map<Token,
        List<Pair<List<Token>, ConfigValue>>> tokensAtIndexGrouped, int index, String currentPath) {
        // Is the current tokens all a leaf?
        boolean isLeaf = tokensAtIndexGrouped.values().stream()
            .flatMap(Collection::stream)
            .map(it -> it.getFirst().size()).allMatch(size -> size == index + 1);

        // If we are a leaf
        if (isLeaf) {

            // since the tokensAtIndexGrouped groups tokens that are the same, if there
            // are duplicate they wil be grouped together.
            // So we need to check if any tokens have more than one values.
            List<ArrayToken> arrayIndexCounts = tokensAtIndexGrouped.entrySet()
                .stream()
                .filter(tokens -> tokens.getValue().size() > 1)
                .map(tokens -> (ArrayToken) tokens.getKey())
                .collect(Collectors.toList());

            return arrayIndexCounts.stream()
                .map(arrayToken -> new ValidationError.ArrayDuplicateIndex(arrayToken.getIndex(), currentPath))
                .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    // We can not have paths with a different lengths, as this means we are at
    // both a leaf and object for the same group of tokens.
    private List<ValidationError> getPathLengthErrors(List<Pair<List<Token>, ConfigValue>> tokens,
                                                      int index, String currentPath) {
        List<Pair<List<Token>, ConfigValue>> nodesWithSamePathLengths =
            tokens.stream()
                .filter(tokenPair -> tokenPair.getFirst().size() < index + 1)
                .collect(Collectors.toList());

        List<ValidationError> errorList = new ArrayList<>();
        if (!nodesWithSamePathLengths.isEmpty()) {
            errorList.add(new ValidationError.PathLengthErrors(currentPath));
        }

        return errorList;
    }
}

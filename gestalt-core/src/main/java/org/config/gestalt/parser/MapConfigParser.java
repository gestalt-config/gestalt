package org.config.gestalt.parser;

import org.config.gestalt.entity.ConfigValue;
import org.config.gestalt.entity.ValidationError;
import org.config.gestalt.entity.ValidationLevel;
import org.config.gestalt.node.ArrayNode;
import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.node.LeafNode;
import org.config.gestalt.node.MapNode;
import org.config.gestalt.token.ArrayToken;
import org.config.gestalt.token.ObjectToken;
import org.config.gestalt.token.Token;
import org.config.gestalt.utils.CollectionUtils;
import org.config.gestalt.utils.Pair;
import org.config.gestalt.utils.PathUtil;
import org.config.gestalt.utils.ValidateOf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Takes in a tokenized config and returns a config node tree.
 *
 * @author Colin Redmond
 */
public class MapConfigParser implements ConfigParser {
    private static final Logger logger = LoggerFactory.getLogger(MapConfigParser.class.getName());

    private boolean treatErrorsAsWarnings = false;

    public MapConfigParser() {
    }

    /**
     * If we should treat errors as warnings, and continue processing even when we receive an error.
     * This may be useful for environment properties as you dont have absolute control over them and
     * may need to be more flexible.
     *
     * @param treatErrorsAsWarnings if we should treat warnings as errors.
     */
    public MapConfigParser(boolean treatErrorsAsWarnings) {
        this.treatErrorsAsWarnings = treatErrorsAsWarnings;
    }

    @Override
    public ValidateOf<ConfigNode> parse(List<Pair<List<Token>, ConfigValue>> configs) {
        return buildConfigTree(configs, 0);
    }

    /**
     * Recursive call to group each section of config that have the same path to this paint until we hit the leaf.
     * Then return the leaf
     * Use the children leaf to build the config tree
     *
     * @param tokens group of tokens that represent an object Array or Map config node
     * @param index  the depth of the currant group of config lists we are looking at.
     * @return the ConfigNode root for the configurations at this point.
     */
    protected ValidateOf<ConfigNode> buildConfigTree(List<Pair<List<Token>, ConfigValue>> tokens, int index) {

        if (tokens == null || tokens.isEmpty()) {
            return ValidateOf.inValid(new ValidationError.EmptyToken());
        }

        String currentPath = PathUtil.toPath(tokens.get(0).getFirst().subList(0, index));
        List<ValidationError> errorList = new ArrayList<>();

        if (tokens.size() == 1 && tokens.get(0).getFirst().size() <= index) {
            ConfigValue configValue = tokens.get(0).getSecond();
            return ValidateOf.valid(new LeafNode(configValue.getValue()));
        }

        List<ValidationError> mismatchedPathLengthErrors = getMismatchedPathLengthErrors(tokens, index, currentPath);
        if (!mismatchedPathLengthErrors.isEmpty()) {
            return ValidateOf.inValid(mismatchedPathLengthErrors);
        }

        Map<Token, List<Pair<List<Token>, ConfigValue>>> tokensAtIndexGrouped = tokens
            .stream()
            .collect(Collectors.groupingBy(tokenPair -> tokenPair.getFirst().get(index)));

        // a distinct list of all token types in this level of the config tree branch
        List<Token> tokenTypes = tokensAtIndexGrouped.keySet().stream()
            .filter(CollectionUtils.distinctBy(Token::getClass))
            .collect(Collectors.toList());

        if (tokenTypes.isEmpty()) {
            errorList.add(new ValidationError.NoTokensInPath(currentPath));
        } else if (tokenTypes.size() > 1) {
            // if there is more than one token type
            errorList.add(new ValidationError.MultipleTokenTypes(currentPath, tokenTypes));
        } else {
            // if there is only 1 token type all tokens at this level of the config tree are the same.
            if (tokenTypes.get(0) instanceof ArrayToken) {
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
            return ValidateOf.inValid(errorList);
        }

        // group all similar tokens, then recursively call buildConfigTree with them.
        List<Pair<Token, ValidateOf<ConfigNode>>> configsValidateOf = tokens.stream()
            .collect(Collectors.groupingBy(tokenPair -> tokenPair.getFirst().get(index)))
            .entrySet()
            .stream()
            .map(entry -> new Pair<>(entry.getKey(), buildConfigTree(entry.getValue(), index + 1)))
            .collect(Collectors.toList());

        // Get any errors and split them by level.
        Map<ValidationLevel, List<ValidationError>> recursiveErrors = configsValidateOf
            .stream()
            .map(it -> it.getSecond().getErrors())
            .flatMap(Collection::stream)
            .collect(Collectors.groupingBy(ValidationError::level));

        // add all warning errors to the current error list.
        if (recursiveErrors.containsKey(ValidationLevel.WARN)) {
            errorList.addAll(recursiveErrors.get(ValidationLevel.WARN));
        }

        // if there are any error level return immediately unless we have treatErrorsAsWarnings enabled.
        if (recursiveErrors.containsKey(ValidationLevel.ERROR)) {
            errorList.addAll(recursiveErrors.get(ValidationLevel.ERROR));
            if (!treatErrorsAsWarnings) {
                return ValidateOf.inValid(errorList);
            }
        }

        // pull out the valid config nodes.
        List<Pair<Token, ConfigNode>> configs = configsValidateOf
            .stream()
            .filter(it -> (treatErrorsAsWarnings || !it.getSecond().hasErrors(ValidationLevel.ERROR)) && it.getSecond().hasResults())
            .map(it -> new Pair<>(it.getFirst(), it.getSecond().results()))
            .collect(Collectors.toList());


        // There should only be one node type for this group of config as we have already validated it
        if (configsValidateOf.isEmpty()) {
            logger.warn("unable to parse tokens and create config node");
        } else if (configs.isEmpty()) {
            logger.warn("No configs found");
        } else {
            Token token = configs.get(0).getFirst();
            ConfigNode result = null;
            if (token instanceof ObjectToken) {
                result = new MapNode(configs.stream()
                    .collect(Collectors.toMap(it -> ((ObjectToken) it.getFirst()).getName(), Pair::getSecond)));
            } else if (token instanceof ArrayToken) {
                OptionalInt maxArrayInt = configs.stream()
                    .map(it -> ((ArrayToken) it.getFirst()).getIndex())
                    .mapToInt(Integer::intValue).max();

                ConfigNode[] arrayNodes = new ConfigNode[maxArrayInt.orElse(0) + 1];

                configs.forEach(config ->
                    arrayNodes[((ArrayToken) config.getFirst()).getIndex()] = config.getSecond());
                result = new ArrayNode(Arrays.asList(arrayNodes));
            }

            if (result != null) {
                return ValidateOf.validateOf(result, errorList);
            }
        }

        return ValidateOf.inValid(new ValidationError.NoResultsFoundForPath(currentPath));
    }

    private List<ValidationError> validateArrayInvalidIndex(List<Pair<List<Token>, ConfigValue>> tokens,
                                                            int index, String currentPath) {
        // Return a
        return tokens.stream()
            .map(token -> (ArrayToken) token.getFirst().get(index))
            .filter(arrayToken -> arrayToken.getIndex() < 0)
            .map(arrayToken -> new ValidationError.ArrayInvalidIndex(arrayToken.getIndex(), currentPath))
            .collect(Collectors.toList());
    }

    private List<ValidationError> validateArrayMissingIndex(List<Pair<List<Token>, ConfigValue>> tokens,
                                                            int index, String currentPath) {
        // Counts for each array index.
        Map<Integer, Long> arrayIndexCounts = tokens.stream()
            .map(token -> (ArrayToken) token.getFirst().get(index))
            .filter(arrayToken -> arrayToken.getIndex() >= 0)
            .collect(Collectors.groupingBy(ArrayToken::getIndex, Collectors.counting()));

        // Validate that we are not missing any index's in the array
        long maxIndex = arrayIndexCounts.keySet().stream().max(Comparator.comparing(Long::valueOf)).orElse(-1);

        return IntStream.rangeClosed(0, Math.toIntExact(maxIndex))
            .filter(it -> !arrayIndexCounts.containsKey(it))
            .mapToObj(it -> new ValidationError.ArrayMissingIndex(it, currentPath))
            .collect(Collectors.toList());
    }

    private List<ValidationError> validateArrayLeafAndNonLeaf(Map<Token, List<Pair<List<Token>, ConfigValue>>> tokensAtIndexGrouped,
                                                              int index, String currentPath) {
        // Build a list of all path sizes in the current set of tokens.
        List<Integer> pathSizes = tokensAtIndexGrouped.values()
            .stream()
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

    private List<ValidationError> validateArrayDuplicateLeafIndex(Map<Token, List<Pair<List<Token>, ConfigValue>>> tokensAtIndexGrouped,
                                                                  int index, String currentPath) {
        // Is the current tokens all a leaf?
        boolean isLeaf = tokensAtIndexGrouped.values()
            .stream()
            .flatMap(Collection::stream)
            .map(it -> it.getFirst().size())
            .allMatch(size -> size == index + 1);

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

            return arrayIndexCounts
                .stream()
                .map(arrayToken -> new ValidationError.ArrayDuplicateIndex(arrayToken.getIndex(), currentPath))
                .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    // We can not have paths with a different lengths, as this means we are at
    // both a leaf and object for the same group of tokens.
    private List<ValidationError> getMismatchedPathLengthErrors(List<Pair<List<Token>, ConfigValue>> tokens, int index,
                                                                String currentPath) {
        List<Pair<List<Token>, ConfigValue>> nodesWithMismatchedPathLengths = tokens
            .stream()
            .filter(tokenPair -> tokenPair.getFirst().size() < index + 1)
            .collect(Collectors.toList());

        List<ValidationError> errorList = new ArrayList<>();
        if (!nodesWithMismatchedPathLengths.isEmpty()) {
            errorList.add(new ValidationError.MismatchedPathLength(currentPath));
        }

        return errorList;
    }
}

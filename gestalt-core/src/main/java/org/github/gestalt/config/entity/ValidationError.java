package org.github.gestalt.config.entity;

import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.post.process.transform.substitution.SubstitutionNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.token.Token;

import java.util.List;

/**
 * Validation errors for every possible error.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public abstract class ValidationError {
    private ValidationLevel level;

    /**
     * Protected constructor so end users cant create a Validation error only inherit from it.
     *
     * @param level error level
     */
    protected ValidationError(ValidationLevel level) {
        this.level = level;
    }

    /**
     * Get the description of the error.
     *
     * @return description of the error
     */
    public abstract String description();

    /**
     * Get the ValidationLevel of this error.
     *
     * @return ValidationLevel of this error
     */
    public ValidationLevel level() {
        return level;
    }

    /**
     * Set the ValidationLevel for this error.
     *
     * @param level ValidationLevel
     */
    public void setLevel(ValidationLevel level) {
        this.level = level;
    }

    /**
     * Returns true if this error is for a missing results issue.
     *
     * @return true if this error is for a missing results issue
     */
    public boolean hasNoResults() {
        return level.equals(ValidationLevel.MISSING_VALUE);
    }

    /**
     * Empty path provided to tokenizer. Parsing error.
     */
    public static class EmptyPath extends ValidationError {

        public EmptyPath() {
            super(ValidationLevel.WARN);
        }

        @Override
        public String description() {
            return "empty path provided";
        }
    }

    /**
     * Empty element/word in a path. Parsing error.
     */
    public static class EmptyElement extends ValidationError {
        private final String path;

        public EmptyElement(String path) {
            super(ValidationLevel.WARN);
            this.path = path;
        }

        @Override
        public String description() {
            return "empty element for path: " + path;
        }
    }

    /**
     * Failed to tokenize a element/word.
     */
    public static class FailedToTokenizeElement extends ValidationError {
        private final String element;
        private final String path;

        public FailedToTokenizeElement(String element, String path) {
            super(ValidationLevel.ERROR);
            this.element = element;
            this.path = path;
        }

        @Override
        public String description() {
            return "Unable to tokenize element " + element + " for path: " + path;
        }
    }

    /**
     * A word pattern must have a name, an array and index is optional.
     */
    public static class UnableToParseName extends ValidationError {
        private final String path;

        public UnableToParseName(String path) {
            super(ValidationLevel.ERROR);
            this.path = path;
        }

        @Override
        public String description() {
            return "unable to parse the name for path: " + path;
        }
    }

    /**
     * A word is an array but doesn't have an index or match the pattern.
     */
    public static class InvalidArrayToken extends ValidationError {
        private final String path;
        private final String element;
        private final String arrayIndex;

        public InvalidArrayToken(String element, String arrayIndex, String path) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.element = element;
            this.arrayIndex = arrayIndex;
        }

        @Override
        public String description() {
            return "Array index provided: " + arrayIndex + " for element " + element +
                " but unable to parse as int for path: " + path;
        }
    }

    /**
     * A word is an array but doesn't have an index.
     */
    public static class InvalidArrayIndexToken extends ValidationError {
        private final String path;
        private final String element;

        public InvalidArrayIndexToken(String element, String path) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.element = element;
        }

        @Override
        public String description() {
            return "Array index not provided provided for element " + element + " for path: " + path;
        }
    }

    /**
     * A word is an array but the index is negative.
     */
    public static class InvalidArrayNegativeIndexToken extends ValidationError {
        private final String path;
        private final String element;
        private final int index;

        public InvalidArrayNegativeIndexToken(String element, int index, String path) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.element = element;
            this.index = index;
        }

        @Override
        public String description() {
            return "Array index can not be negative: " + index + " provided provided for element: " + element + " for path: " + path;
        }
    }

    /**
     * No tokens provided while building the config node.
     */
    public static class EmptyToken extends ValidationError {

        public EmptyToken() {
            super(ValidationLevel.ERROR);
        }

        @Override
        public String description() {
            return "Empty or null token provided while building the config node";
        }
    }

    /**
     * Unknown token type while building a config node.
     */
    public static class UnknownTokenWithPath extends ValidationError {
        private final Token token;
        private final String path;

        public UnknownTokenWithPath(Token token, String path) {
            super(ValidationLevel.ERROR);
            this.token = token;
            this.path = path;
        }

        @Override
        public String description() {
            return "Unknown token type " + token + " for path: " + path;
        }
    }

    /**
     * Mismatched path lengths received for path, this could be because a node is both a leaf and an object.
     */
    public static class MismatchedPathLength extends ValidationError {
        private final String paths;

        public MismatchedPathLength(String paths) {
            super(ValidationLevel.ERROR);
            this.paths = paths;
        }

        @Override
        public String description() {
            return "Mismatched path lengths received for path: " + paths + ", this could be because a node is both a leaf and an object";
        }
    }

    /**
     * No tokens provided while building a config node.
     */
    public static class NoTokensInPath extends ValidationError {
        private final String path;

        public NoTokensInPath(String path) {
            super(ValidationLevel.ERROR);
            this.path = path;
        }

        @Override
        public String description() {
            return "Unable to find a token for path: " + path + " while building a config node";
        }
    }

    /**
     * For a specific path there are multiple token types. This can happen when a node is an array and an object
     */
    public static class MultipleTokenTypes extends ValidationError {
        private final String path;
        private final List<Token> tokens;

        public MultipleTokenTypes(String path, List<Token> tokens) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.tokens = tokens;
        }

        @Override
        public String description() {
            return "Found multiple token types " + tokens + " for path " + path;
        }
    }

    /**
     * Array is missing an index.
     */
    public static class ArrayMissingIndex extends ValidationError {
        private final long index;
        private final String path;

        public ArrayMissingIndex(long index) {
            super(ValidationLevel.WARN);
            this.index = index;
            this.path = null;
        }

        public ArrayMissingIndex(long index, String path) {
            super(ValidationLevel.WARN);
            this.index = index;
            this.path = path;
        }

        @Override
        public String description() {
            if (path != null) {
                return "Missing array index: " + index + " for path: " + path;
            } else {
                return "Missing array index: " + index;
            }
        }
    }

    /**
     * Invalid array index. For negative array indexes while building a config node.
     */
    public static class ArrayInvalidIndex extends ValidationError {
        private final long index;
        private final String path;

        public ArrayInvalidIndex(long index, String path) {
            super(ValidationLevel.ERROR);
            this.index = index;
            this.path = path;
        }

        @Override
        public String description() {
            return "Invalid array index: " + index + " for path: " + path;
        }
    }

    /**
     * Array has duplicate index's while building config node.
     */
    public static class ArrayDuplicateIndex extends ValidationError {
        private final long index;
        private final String path;

        public ArrayDuplicateIndex(long index, String path) {
            super(ValidationLevel.ERROR);
            this.index = index;
            this.path = path;
        }

        @Override
        public String description() {
            return "Duplicate array index: " + index + " for path: " + path;
        }
    }

    /**
     * Array has both leaf and non leaf values while building config node.
     */
    public static class ArrayLeafAndNotLeaf extends ValidationError {
        private final String path;
        private final List<Integer> sizes;

        public ArrayLeafAndNotLeaf(List<Integer> sizes, String path) {
            super(ValidationLevel.ERROR);
            this.sizes = sizes;
            this.path = path;
        }

        @Override
        public String description() {
            return "Array is both a leaf and non leaf with sizes: " + sizes + " for path: " + path;
        }
    }

    /**
     * While decoding a leaf it is missing its value.
     */
    public static class DecodingArrayMissingValue extends ValidationError {
        private final String path;
        private final String decoderName;

        public DecodingArrayMissingValue(String path, String decoderName) {
            super(ValidationLevel.MISSING_VALUE);
            this.path = path;
            this.decoderName = decoderName;
        }

        @Override
        public String description() {
            return "Array on path: " + path + ", has no value attempting to decode " + decoderName;
        }

        @Override
        public boolean hasNoResults() {
            return true;
        }
    }

    /**
     * No results found for path while building config node.
     */
    public static class NoResultsFoundForPath extends ValidationError {
        private final String path;

        public NoResultsFoundForPath(String path) {
            super(ValidationLevel.MISSING_VALUE);
            this.path = path;
        }

        @Override
        public String description() {
            return "Unable to find node matching path: " + path;
        }

        @Override
        public boolean hasNoResults() {
            return true;
        }
    }

    /**
     * While decoding a leaf it is missing its value.
     */
    public static class DecodingLeafMissingValue extends ValidationError {
        private final String path;
        private final String decoderName;

        public DecodingLeafMissingValue(String path, String decoderName) {
            super(ValidationLevel.MISSING_VALUE);
            this.path = path;
            this.decoderName = decoderName;
        }

        @Override
        public String description() {
            return "Leaf on path: " + path + ", has no value attempting to decode " + decoderName;
        }

        @Override
        public boolean hasNoResults() {
            return true;
        }
    }

    /**
     * While decoding a leaf we received a non leaf node.
     */
    public static class DecodingExpectedLeafNodeType extends ValidationError {
        private final String path;
        private final ConfigNode node;
        private final String decoderName;

        public DecodingExpectedLeafNodeType(String path, ConfigNode node, String decoderName) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.node = node;
            this.decoderName = decoderName;
        }

        @Override
        public String description() {
            return "Expected a leaf on path: " + path + ", received node type: " + (node == null ? "null" : node.getNodeType().getType()) +
                ", attempting to decode " + decoderName;
        }
    }

    /**
     * While decoding an array we received a non array node.
     */
    public static class DecodingExpectedArrayNodeType extends ValidationError {
        private final String path;
        private final ConfigNode node;
        private final String decoderName;

        public DecodingExpectedArrayNodeType(String path, ConfigNode node, String decoderName) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.node = node;
            this.decoderName = decoderName;
        }

        @Override
        public String description() {
            return "Expected a Array on path: " + path + ", received node type: " + (node == null ? "null" : node.getNodeType()) +
                ", attempting to decode " + decoderName;
        }
    }

    /**
     * While decoding a map we received a non map node.
     */
    public static class DecodingExpectedMapNodeType extends ValidationError {
        private final String path;
        private final List<TypeCapture<?>> types;

        private final ConfigNode node;

        public DecodingExpectedMapNodeType(String path, List<TypeCapture<?>> types, ConfigNode node) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.types = types;
            this.node = node;
        }

        public DecodingExpectedMapNodeType(String path, ConfigNode node) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.types = null;
            this.node = node;
        }

        @Override
        public String description() {
            if (types == null) {
                return "Expected a map node on path: " + path + ", received node type : " +
                    (node == null ? "null" : node.getNodeType());
            } else {
                return "Expected a map on path: " + path + ", received node type : " +
                    (node == null ? "null" : node.getNodeType().getType()) + ", received invalid types: " + types;
            }
        }
    }

    /**
     * While decoding a number the value is not a number.
     */
    public static class DecodingNumberParsing extends ValidationError {
        private final String path;
        private final ConfigNode node;
        private final String decoderName;

        public DecodingNumberParsing(String path, ConfigNode node, String decoderName) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.node = node;
            this.decoderName = decoderName;
        }

        @Override
        public String description() {
            return "Unable to parse a number on Path: " + path + ", from node: " + node + " attempting to decode " + decoderName;
        }
    }

    /**
     * While decoding a number received a number format exception.
     */
    public static class DecodingNumberFormatException extends ValidationError {
        private final String path;
        private final ConfigNode node;
        private final String nodeType;

        public DecodingNumberFormatException(String path, ConfigNode node, String nodeType) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.node = node;
            this.nodeType = nodeType;
        }

        @Override
        public String description() {
            return "Unable to decode a number on path: " + path + ", from node: " + node + " attempting to decode " + nodeType;
        }
    }

    /**
     * While deciding a char, expected a single value character but received more.
     */
    public static class DecodingCharWrongSize extends ValidationError {
        private final String path;
        private final ConfigNode node;

        public DecodingCharWrongSize(String path, ConfigNode node) {
            super(ValidationLevel.WARN);
            this.path = path;
            this.node = node;
        }

        @Override
        public String description() {
            return "Expected a char on path: " + path + ", decoding node: " + node + " received the wrong size";
        }
    }

    /**
     * While deciding a byte, expected a single value but received more.
     */
    public static class DecodingByteTooLong extends ValidationError {
        private final String path;
        private final ConfigNode node;

        public DecodingByteTooLong(String path, ConfigNode node) {
            super(ValidationLevel.WARN);
            this.path = path;
            this.node = node;
        }

        @Override
        public String description() {
            return "Expected a Byte on path: " + path + ", decoding node: " + node + " received the wrong size";
        }
    }

    /**
     * While decoding a value received an exception.
     */
    public static class ErrorDecodingException extends ValidationError {
        private final String path;
        private final ConfigNode node;
        private final String decoder;

        public ErrorDecodingException(String path, ConfigNode node, String decoder) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.node = node;
            this.decoder = decoder;
        }

        @Override
        public String description() {
            return "Unable to decode a " + decoder + " on path: " + path + ", from node: " + node;
        }
    }

    /**
     * While decoding a maps key the key was null.
     */
    public static class DecodersMapKeyNull extends ValidationError {
        private final String path;

        public DecodersMapKeyNull(String path) {
            super(ValidationLevel.ERROR);
            this.path = path;
        }

        @Override
        public String description() {
            return "Map key was null on path: " + path;
        }
    }

    /**
     * While decoding a maps value, it was null.
     */
    public static class DecodersMapValueNull extends ValidationError {
        private final String path;

        public DecodersMapValueNull(String path) {
            super(ValidationLevel.WARN);
            this.path = path;
        }

        @Override
        public String description() {
            return "Map key was null on path: " + path;
        }
    }

    /**
     * No decoders found.
     */
    public static class NoDecodersFound extends ValidationError {
        private final String klass;

        public NoDecodersFound(String klass) {
            super(ValidationLevel.ERROR);
            this.klass = klass;
        }

        @Override
        public String description() {
            return "No decoders found for class: " + klass;
        }
    }

    /**
     * While trying to get a configuration, was unable to find a value.
     */
    public static class NoResultsFoundForNode extends ValidationError {
        private final String path;
        private final String area;
        private String klass;

        public NoResultsFoundForNode(String path, String area) {
            super(ValidationLevel.MISSING_VALUE);
            this.path = path;
            this.area = area;
        }

        public NoResultsFoundForNode(String path, String klass, String area) {
            super(ValidationLevel.MISSING_VALUE);
            this.path = path;
            this.klass = klass;
            this.area = area;
        }

        public NoResultsFoundForNode(String path, Class<?> klass, String area) {
            super(ValidationLevel.MISSING_VALUE);
            this.path = path;
            this.klass = klass.getSimpleName();
            this.area = area;
        }

        @Override
        public String description() {
            if (klass != null) {
                return "Unable to find node matching path: " + path + ", for class: " + klass + ", during " + area;
            } else {
                return "Unable to find node matching path: " + path + ", during " + area;
            }
        }

        @Override
        public boolean hasNoResults() {
            return true;
        }
    }

    /**
     * While building a config node an empty node name was provided.
     */
    public static class EmptyNodeNameProvided extends ValidationError {
        private final String path;

        public EmptyNodeNameProvided(String path) {
            super(ValidationLevel.WARN);
            this.path = path;
        }

        @Override
        public String description() {
            return "Empty node name provided for path: " + path;
        }
    }

    /**
     * While building a config node an empty value name was provided.
     */
    public static class EmptyNodeValueProvided extends ValidationError {
        private final String path;
        private final String key;

        public EmptyNodeValueProvided(String path, String key) {
            super(ValidationLevel.WARN);
            this.path = path;
            this.key = key;
        }

        @Override
        public String description() {
            return "Empty node value provided for path: " + path + "." + key;
        }
    }

    /**
     * Received the wrong node type while navigating to a node.
     */
    public static class MismatchedObjectNodeForPath extends ValidationError {
        private final String path;
        private final Class<?> expectedKlass;
        private final Class<?> actualKlass;

        public MismatchedObjectNodeForPath(String path, Class<?> expectedKlass, Class<?> actualKlass) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.expectedKlass = expectedKlass;
            this.actualKlass = actualKlass;
        }

        @Override
        public String description() {
            return "Mismatched Nodes on path: " + path + ", expected: " + expectedKlass.getSimpleName() +
                " received: " + actualKlass.getSimpleName();
        }
    }

    /**
     * Config node was null while navigating to a path.
     */
    public static class NullNodeForPath extends ValidationError {
        private final String path;

        public NullNodeForPath(String path) {
            super(ValidationLevel.WARN);
            this.path = path;
        }

        @Override
        public String description() {
            return "Null Nodes on path: " + path;
        }
    }

    /**
     * Token provided is null for path.
     */
    public static class NullTokenForPath extends ValidationError {
        private final String path;

        public NullTokenForPath(String path) {
            super(ValidationLevel.WARN);
            this.path = path;
        }

        @Override
        public String description() {
            return "Null or Empty Token on path: " + path;
        }
    }

    /**
     * Unknown token type found while navigating to a node.
     */
    public static class UnsupportedTokenType extends ValidationError {
        private final String path;
        private final Token token;

        public UnsupportedTokenType(String path, Token token) {
            super(ValidationLevel.WARN);
            this.path = path;
            this.token = token;
        }

        @Override
        public String description() {
            return "unsupported token: " + token.getClass().getSimpleName() + " for path: " + path;
        }
    }

    /**
     * Unknown node type while building config node.
     */
    public static class UnknownNodeTypeDuringLoad extends ValidationError {
        private final String nodeType;
        private final String path;

        public UnknownNodeTypeDuringLoad(String path, String nodeType) {
            super(ValidationLevel.ERROR);
            this.nodeType = nodeType;
            this.path = path;
        }

        @Override
        public String description() {
            return "Unknown node type: " + nodeType + " on Path: " + path;
        }
    }

    /**
     * Unable to merge nodes of different types. Can not merge an array with a object or an object with a leaf.
     */
    public static class UnableToMergeDifferentNodes extends ValidationError {
        private final Class<?> klass1;
        private final Class<?> klass2;

        public UnableToMergeDifferentNodes(Class<?> klass1, Class<?> klass2) {
            super(ValidationLevel.ERROR);
            this.klass1 = klass1;
            this.klass2 = klass2;
        }

        @Override
        public String description() {
            return "Unable to merge different nodes, of type: " + klass1.getSimpleName() + " and type: " + klass2.getSimpleName();
        }
    }

    /**
     * Unknown node type while building config node.
     */
    public static class UnknownNodeType extends ValidationError {
        private final String nodeType;
        private final String path;

        public UnknownNodeType(String path, String nodeType) {
            super(ValidationLevel.ERROR);
            this.nodeType = nodeType;
            this.path = path;
        }

        @Override
        public String description() {
            return "Unknown node type: " + nodeType + " on Path: " + path;
        }
    }

    /**
     * Failed to decode an Enum, as the value doesn't exist.
     */
    public static class EnumValueNotFound extends ValidationError {
        private final String path;
        private final String enumValue;
        private final Class<?> enumClass;

        public EnumValueNotFound(String path, String enumValue, Class<?> enumClass) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.enumValue = enumValue;
            this.enumClass = enumClass;
        }

        @Override
        public String description() {
            return "ENUM " + enumClass.getName() + " could not be created with value " + enumValue +
                " for Path: " + path;
        }
    }

    /**
     * Exception while decoding an enum.
     */
    public static class ExceptionDecodingEnum extends ValidationError {
        private final String path;
        private final String enumValue;
        private final Class<?> enumClass;
        private final Exception exception;

        public ExceptionDecodingEnum(String path, String enumValue, Class<?> enumClass, Exception exception) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.enumValue = enumValue;
            this.enumClass = enumClass;
            this.exception = exception;
        }

        @Override
        public String description() {
            return "Exception on Path: " + path + ", decoding enum: " + enumClass.getName() + " could not be created with value " +
                enumValue + " exception was: " + exception.getMessage();
        }
    }

    /**
     * Leaf node is null.
     */
    public static class LeafNodesIsNull extends ValidationError {
        private final String path;

        public LeafNodesIsNull(String path) {
            super(ValidationLevel.WARN);
            this.path = path;
        }

        @Override
        public String description() {
            return "Leaf nodes is null: " + path;
        }
    }

    /**
     * Leaf node has no values.
     */
    public static class LeafNodesHaveNoValues extends ValidationError {
        private final String path;

        public LeafNodesHaveNoValues(String path) {
            super(ValidationLevel.WARN);
            this.path = path;
        }

        @Override
        public String description() {
            return "Leaf nodes are empty for path: " + path;
        }
    }

    /**
     * While decoding a Object the constructor was not public. Unable to create the object.
     */
    public static class ConstructorNotPublic extends ValidationError {
        private final String path;
        private final String klassName;

        public ConstructorNotPublic(String path, String klassName) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.klassName = klassName;
        }

        @Override
        public String description() {
            return "Constructor for: " + klassName + " is not public on Path: " + path;
        }
    }

    /**
     * While decoding a Object no default constructor found.
     */
    public static class NoDefaultConstructor extends ValidationError {
        private final String path;
        private final String klassName;

        public NoDefaultConstructor(String path, String klassName) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.klassName = klassName;
        }

        @Override
        public String description() {
            return "No default Constructor for : " + klassName + " on Path: " + path;
        }
    }

    /**
     * While decoding a Object no value was found and the result will be null.
     */
    public static class NullValueDecodingObject extends ValidationError {
        private final String path;
        private final String field;
        private final String klassName;

        public NullValueDecodingObject(String path, String field, String klassName) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.field = field;
            this.klassName = klassName;
        }

        @Override
        public String description() {
            return "Decoding object : " + klassName + " on path: " + path + ", field " + field + " results in null value";
        }
    }

    /**
     * Unexpected closing token found when now substitution was open.
     */
    public static class UnexpectedClosingTokenTransform extends ValidationError {
        private final String path;
        private final String value;
        private final String closingToken;
        private final int location;

        public UnexpectedClosingTokenTransform(String path, String value, String closingToken, int location) {
            super(ValidationLevel.DEBUG);
            this.path = path;
            this.value = value;
            this.closingToken = closingToken;
            this.location = location;
        }

        @Override
        public String description() {
            return "Unexpected closing token: " + closingToken + " found in string: " + value +
                ", at location: " + location + " on path: " + path;
        }
    }

    /**
     * Reached the end of a string with an unclosed substitution.
     */
    public static class UnclosedSubstitutionTransform extends ValidationError {
        private final String path;
        private final String value;

        public UnclosedSubstitutionTransform(String path, String value) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.value = value;
        }

        @Override
        public String description() {
            return "Reached the end of a string " + value + " with an unclosed substitution on path: " + path;
        }
    }

    public static class NoMatchingTransformFound extends ValidationError {
        private final String path;
        private final String transformName;

        public NoMatchingTransformFound(String path, String transformName) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.transformName = transformName;
        }

        @Override
        public String description() {
            return "Unable to find matching transform for " + path + " with transform: " + transformName +
                ". make sure you registered all expected transforms";
        }
    }

    /**
     * No matching transform found for name.
     */
    public static class NoMatchingDefaultTransformFound extends ValidationError {
        private final String path;
        private final String key;


        public NoMatchingDefaultTransformFound(String path, String key) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.key = key;
        }

        @Override
        public String description() {
            return "Unable to find matching transform for " + path + " with the default transformers. " +
                "For key: " + key + ", make sure you registered all expected transforms";
        }
    }

    /**
     * No Key found for the transform.
     */
    public static class NoKeyFoundForTransform extends ValidationError {
        private final String path;
        private final String transformName;
        private final String key;

        public NoKeyFoundForTransform(String path, String transformName, String key) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.transformName = transformName;
            this.key = key;
        }

        @Override
        public String description() {
            return "Unable to find matching key for transform " + transformName + " with key " + key +
                " on path " + path;
        }
    }

    /**
     * Transform doesnt match the regex
     */
    public static class TransformDoesntMatchRegex extends ValidationError {
        private final String path;
        private final String value;

        public TransformDoesntMatchRegex(String path, String value) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.value = value;
        }

        @Override
        public String description() {
            return "Transform doesnt match the expected format with value " + value + " on path " + path;
        }
    }

    /**
     * Not a valid SubstitutionNode
     */
    public static class NotAValidSubstitutionNode extends ValidationError {
        private final String path;
        private final SubstitutionNode node;

        public NotAValidSubstitutionNode(String path, SubstitutionNode node) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.node = node;
        }

        @Override
        public String description() {
            return "Unknown SubstitutionNode " + node + " on path " + path;
        }
    }


    /**
     * Not a valid SubstitutionNode
     */
    public static class ExceededMaximumNestedSubstitutionDepth extends ValidationError {
        private final String path;
        private final ConfigNode node;
        private final int depth;

        public ExceededMaximumNestedSubstitutionDepth(String path, int depth, ConfigNode node) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.node = node;
            this.depth = depth;
        }

        @Override
        public String description() {
            return "Exceeded maximum nested substitution depth of " + depth + " on path " + path + " for node: " + node.toString();
        }
    }

    /**
     * Unknown node type while building config node.
     */
    public static class UnknownNodeTypePostProcess extends ValidationError {
        private final String nodeType;
        private final String path;

        public UnknownNodeTypePostProcess(String path, String nodeType) {
            super(ValidationLevel.ERROR);
            this.nodeType = nodeType;
            this.path = path;
        }

        @Override
        public String description() {
            return "Unknown node type: " + nodeType + " on Path: " + path + " while post processing";
        }
    }

    /**
     * While trying to get a configuration, was unable to find a value.
     */
    public static class NoSystemPropertyFoundPostProcess extends ValidationError {
        private final String path;
        private final String property;

        public NoSystemPropertyFoundPostProcess(String path, String property) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.property = property;
        }

        @Override
        public String description() {
            return "No System Property found for: " + property + ", on path: " + path + " during post process";
        }
    }

    /**
     * While trying to get a Custom Map Property during post processing.
     */
    public static class NoCustomPropertyFoundPostProcess extends ValidationError {
        private final String path;
        private final String property;

        public NoCustomPropertyFoundPostProcess(String path, String property) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.property = property;
        }

        @Override
        public String description() {
            return "No custom Property found for: " + property + ", on path: " + path + " during post process";
        }
    }

    /**
     * While trying to get a Environment Variable during post processing.
     */
    public static class NoEnvironmentVariableFoundPostProcess extends ValidationError {
        private final String path;
        private final String property;

        public NoEnvironmentVariableFoundPostProcess(String path, String property) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.property = property;
        }

        @Override
        public String description() {
            return "No Environment Variables found for: " + property + ", on path: " + path + " during post process";
        }
    }

    /**
     * Invalid number of parameters provided to random expression during post processing.
     */
    public static class InvalidNumberOfParametersForRandomExpression extends ValidationError {
        private final String path;
        private final String key;

        private final String type;
        private final int expected;

        public InvalidNumberOfParametersForRandomExpression(String path, String key, String type, int expected) {
            super(ValidationLevel.WARN);
            this.path = path;
            this.key = key;
            this.type = type;

            this.expected = expected;
        }

        @Override
        public String description() {
            return "Invalid number of parameters for type : " + type + ", from key: " + key + ", on path: " + path +
                " during post process. Expected " + expected + " parameters";
        }
    }

    /**
     * Invalid number of parameters provided to random expression during post processing.
     */
    public static class InvalidNumberOfParametersForRandomExpressionError extends ValidationError {
        private final String path;
        private final String key;

        private final String type;
        private final int expected;

        public InvalidNumberOfParametersForRandomExpressionError(String path, String key, String type, int expected) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.key = key;
            this.type = type;

            this.expected = expected;
        }

        @Override
        public String description() {
            return "Invalid number of parameters for type : " + type + ", from key: " + key + ", on path: " + path +
                " during post process. Must have " + expected + " parameters";
        }
    }

    /**
     * unable to parse random expression during post processing.
     */
    public static class UnableToParseRandomParameter extends ValidationError {
        private final String path;
        private final String key;
        private final String type;
        private final String p1;
        private final String p2;

        public UnableToParseRandomParameter(String path, String key, String type, String p1, String p2) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.key = key;
            this.type = type;
            this.p1 = p1;
            this.p2 = p2;
        }

        @Override
        public String description() {
            return "Unable to parse random parameter: " + key + ", on path: " + path + " during post process, " +
                "with parameters: " + p1 + ", " + p2 + " of type: " + type;
        }
    }

    /**
     * unable to parse random expression during post processing.
     */
    public static class UnableToParseRandomExpression extends ValidationError {
        private final String path;
        private final String key;

        public UnableToParseRandomExpression(String path, String key) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.key = key;
        }

        @Override
        public String description() {
            return "Unable to parse random expression: " + key + ", on path: " + path + " during post process";
        }
    }

    /**
     * Unsupported random expression during post process.
     */
    public static class UnsupportedRandomPostProcess extends ValidationError {
        private final String path;
        private final String key;

        public UnsupportedRandomPostProcess(String path, String key) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.key = key;
        }

        @Override
        public String description() {
            return "Unsupported random post processor: " + key + ", on path: " + path + " during post process";
        }
    }

    /**
     * No Configuration found for Node Post Processing.
     */
    public static class NodePostProcessingConfigMissing extends ValidationError {
        private final String path;
        private final String property;

        public NodePostProcessingConfigMissing(String path, String property) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.property = property;
        }

        @Override
        public String description() {
            return "node Transform PostProcessorConfig is null, unable to transform path: " + path + " with: " + property;
        }
    }

    /**
     * Node Post Processing scanned bad tokens.
     */
    public static class NodePostProcessingBadTokens extends ValidationError {
        private final String path;
        private final String property;

        public NodePostProcessingBadTokens(String path, String property) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.property = property;
        }

        @Override
        public String description() {
            return "Errors generating tokens while running node transform path: " + path + " with: " + property;
        }
    }

    /**
     * Node Post Processing scanned missing tokens.
     */
    public static class NodePostProcessingNoResultsForTokens extends ValidationError {
        private final String path;
        private final String property;

        public NodePostProcessingNoResultsForTokens(String path, String property) {
            super(ValidationLevel.MISSING_VALUE);
            this.path = path;
            this.property = property;
        }

        @Override
        public String description() {
            return "No results generating tokens while running node transform path: " + path + " with: " + property;
        }

        @Override
        public boolean hasNoResults() {
            return true;
        }
    }

    /**
     * Node Post Processing has not generated any results.
     */
    public static class NodePostProcessingNoResults extends ValidationError {

        /**
         * Protected constructor so end users cant create a Validation error only inherit from it.
         */
        public NodePostProcessingNoResults() {
            super(ValidationLevel.MISSING_VALUE);
        }

        @Override
        public String description() {
            return "No results generated from post processor ";
        }

        @Override
        public boolean hasNoResults() {
            return true;
        }
    }

    /**
     * Node Post Processing scanned missing tokens.
     */
    public static class NodePostProcessingErrorsNavigatingToNode extends ValidationError {
        private final String path;
        private final String property;

        public NodePostProcessingErrorsNavigatingToNode(String path, String property) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.property = property;
        }

        @Override
        public String description() {
            return "Errors navigating to node while running node transform path: " + path + " with: " + property;
        }
    }

    /**
     * Node Post Processing scanned missing tokens.
     */
    public static class NodePostProcessingNodeNotLeaf extends ValidationError {
        private final String path;
        private final String property;

        public NodePostProcessingNodeNotLeaf(String path, String property) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.property = property;
        }

        @Override
        public String description() {
            return "Non leaf node found while running node transform path: " + path + " with: " + property;
        }
    }

    /**
     * Node Post Processing scanned missing tokens.
     */
    public static class NodePostProcessingNodeLeafHasNoValue extends ValidationError {
        private final String path;
        private final String property;

        public NodePostProcessingNodeLeafHasNoValue(String path, String property) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.property = property;
        }

        @Override
        public String description() {
            return "leaf node has no value while running node transform path: " + path + " with: " + property;
        }
    }
}


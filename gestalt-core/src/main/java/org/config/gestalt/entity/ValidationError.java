package org.config.gestalt.entity;

import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.token.Token;

import java.util.List;

public abstract class ValidationError {
    private ValidationLevel level;

    protected ValidationError(ValidationLevel level) {
        this.level = level;
    }

    public abstract String description();

    public ValidationLevel level() {
        return level;
    }

    public void setLevel(ValidationLevel level) {
        this.level = level;
    }

    //parser errors
    public static class EmptyPath extends ValidationError {

        public EmptyPath() {
            super(ValidationLevel.WARN);
        }

        @Override
        public String description() {
            return "empty path provided";
        }
    }

    //parser errors
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

    //parsing errors
    public static class EmptyToken extends ValidationError {

        public EmptyToken() {
            super(ValidationLevel.ERROR);
        }

        @Override
        public String description() {
            return "Empty or null token provided";
        }
    }

    public static class UnknownToken extends ValidationError {
        private final Token token;

        public UnknownToken(Token token) {
            super(ValidationLevel.ERROR);
            this.token = token;
        }

        @Override
        public String description() {
            return "Unknown token type " + token;
        }
    }


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

    public static class NoTokensInPath extends ValidationError {
        private final String path;

        public NoTokensInPath(String path) {
            super(ValidationLevel.ERROR);
            this.path = path;
        }

        @Override
        public String description() {
            return "Unable to find a token for path: " + path;
        }
    }

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

    public static class NoResultsFoundForPath extends ValidationError {
        private final String path;

        public NoResultsFoundForPath(String path) {
            super(ValidationLevel.ERROR);
            this.path = path;
        }

        @Override
        public String description() {
            return "Unable to find node matching path: " + path;
        }
    }

    public static class DecodingLeafMissingValue extends ValidationError {
        private final String path;
        private final ConfigNode node;
        private final String nodeType;

        public DecodingLeafMissingValue(String path, ConfigNode node, String nodeType) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.node = node;
            this.nodeType = nodeType;
        }

        @Override
        public String description() {
            return "Leaf on path: " + path + ", missing value, " + node + " attempting to decode " + nodeType;
        }
    }

    public static class DecodingExpectedLeafNodeType extends ValidationError {
        private final String path;
        private final ConfigNode node;
        private final String nodeType;

        public DecodingExpectedLeafNodeType(String path, ConfigNode node, String nodeType) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.node = node;
            this.nodeType = nodeType;
        }

        @Override
        public String description() {
            return "Expected a leaf on path: " + path + ", received node type, received: " + node + " attempting to decode " + nodeType;
        }
    }

    public static class DecodingExpectedArrayNodeType extends ValidationError {
        private final String path;
        private final ConfigNode node;
        private final String nodeType;

        public DecodingExpectedArrayNodeType(String path, ConfigNode node, String nodeType) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.node = node;
            this.nodeType = nodeType;
        }

        @Override
        public String description() {
            return "Expected a Array  on path: " + path + ", received node type, received: " + node + " attempting to decode " + nodeType;
        }
    }

    public static class DecodingUnsupportedOperation extends ValidationError {
        private final ConfigNode node;
        private final String nodeType;

        public DecodingUnsupportedOperation(ConfigNode node, String nodeType) {
            super(ValidationLevel.ERROR);
            this.node = node;
            this.nodeType = nodeType;
        }

        @Override
        public String description() {
            return "Unsupported operation decoding node: " + node + " attempting to decode " + nodeType;
        }
    }

    public static class DecodingExpectedMap extends ValidationError {
        private final String path;
        private final List<TypeCapture<?>> types;

        public DecodingExpectedMap(String path, List<TypeCapture<?>> types) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.types = types;
        }

        @Override
        public String description() {
            return "Expected a map on path: " + path + ", received inavalid types: " + types.toString();
        }
    }

    public static class DecodingNumberParsing extends ValidationError {
        private final String path;
        private final ConfigNode node;
        private final String nodeType;

        public DecodingNumberParsing(String path, ConfigNode node, String nodeType) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.node = node;
            this.nodeType = nodeType;
        }

        @Override
        public String description() {
            return "Unable to parse a number on Path: " + path + ", from node: " + node + " attempting to decode " + nodeType;
        }
    }

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

    public static class NoResultsFoundForNode extends ValidationError {
        private final String path;
        private final String klass;

        public NoResultsFoundForNode(String path, String klass) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.klass = klass;
        }

        public NoResultsFoundForNode(String path, Class<?> klass) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.klass = klass.getSimpleName();
        }

        @Override
        public String description() {
            return "Unable to find node matching path: " + path + ", for class: " + klass;
        }
    }

    public static class NoResultsFoundForDecodingNode extends ValidationError {
        private final String path;
        private final String klass;

        public NoResultsFoundForDecodingNode(String path, String klass) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.klass = klass;
        }

        public NoResultsFoundForDecodingNode(String path, Class<?> klass) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.klass = klass.getSimpleName();
        }

        @Override
        public String description() {
            return "Unable to decode node matching path: " + path + ", for class: " + klass;
        }
    }

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

    public static class UnableToFindArrayNodeForPath extends ValidationError {
        private final String path;
        private final Token token;

        public UnableToFindArrayNodeForPath(String path, Token token) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.token = token;
        }

        @Override
        public String description() {
            return "Unable to find array node for path: " + path + ", at token: " + token.getClass().getSimpleName();
        }
    }

    public static class UnableToFindObjectNodeForPath extends ValidationError {
        private final String path;
        private final Token token;

        public UnableToFindObjectNodeForPath(String path, Token token) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.token = token;
        }

        @Override
        public String description() {
            return "Unable to find object node for path: " + path + ", at token: " + token.getClass().getSimpleName();
        }
    }

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

    public static class NullTokenForPath extends ValidationError {
        private final String path;

        public NullTokenForPath(String path) {
            super(ValidationLevel.WARN);
            this.path = path;
        }

        @Override
        public String description() {
            return "Null Token on path: " + path;
        }
    }

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

    public static class UnknownNodeType extends ValidationError {
        private final String nodeType;

        public UnknownNodeType(String nodeType) {
            super(ValidationLevel.ERROR);
            this.nodeType = nodeType;
        }

        @Override
        public String description() {
            return "Unknown node type: " + nodeType;
        }
    }

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
}

package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.ValidateOf;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/**
 * Decode a String.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class StringConstructorDecoder implements Decoder<Object> {

    public StringConstructorDecoder() {
    }

    @Override
    public Priority priority() {
        return Priority.LOW;
    }

    @Override
    public String name() {
        return "StringConstructor";
    }

    @Override
    public boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type) {
        Class<?> klass = type.getRawType();
        Constructor<?>[] stringConstructor = klass.getConstructors();

        if (!(node instanceof LeafNode)) {
            return false;
        }

        return Arrays.stream(stringConstructor)
            .anyMatch(it -> it.getParameterCount() == 1 && it.getParameters()[0].getType().equals(String.class));
    }

    /**
     * Decode the current node. If the current node is a class or list we may need to decode sub nodes.
     *
     * @param path           the current path
     * @param tags           the tags for the current request
     * @param node           the current node we are decoding.
     * @param type           the type of object we are decoding.
     * @param decoderContext The context of the current decoder.
     * @return ValidateOf the current node with details of either success or failures.
     */
    @Override
    public ValidateOf<Object> decode(String path, Tags tags, ConfigNode node, TypeCapture<?> type, DecoderContext decoderContext) {

        LeafNode leafNode = (LeafNode) node;
        if (leafNode.getValue().isEmpty()) {
            return ValidateOf.inValid(new ValidationError.LeafNodesIsNullDecoding(path, type));
        }

        Class<?> klass = type.getRawType();

        try {
            Constructor<?> stringConstructor = klass.getConstructor(String.class);
            return ValidateOf.valid(stringConstructor.newInstance(node.getValue().get()));
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            return ValidateOf.inValid(new ValidationError.StringConstructorNotFound(path, type));
        }
    }
}

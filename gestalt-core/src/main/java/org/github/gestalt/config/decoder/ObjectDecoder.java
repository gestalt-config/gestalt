package org.github.gestalt.config.decoder;

import org.github.gestalt.config.annotations.Config;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.node.MapNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.utils.PathUtil;
import org.github.gestalt.config.utils.ValidateOf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.util.*;

/**
 * Decode a class. This decoder is best suited for pojo style classes.
 * Will fail if the constructor is private.
 * Will construct the class even if there are missing values, the values will be null or the default. Then it will return errors.
 * Decodes member classes and lists as well.
 *
 * @author Colin Redmond
 */
public class ObjectDecoder implements Decoder<Object> {
    private static final Logger logger = LoggerFactory.getLogger(ObjectDecoder.class.getName());

    private final Set<Class<?>> ignoreTypes;

    /**
     * constructor for the ObjectDecoder.
     */
    public ObjectDecoder() {
        ignoreTypes = getIgnoreTypes();
    }

    @Override
    public Priority priority() {
        return Priority.VERY_LOW;
    }

    @Override
    public String name() {
        return "Object";
    }

    @Override
    public boolean matches(TypeCapture<?> klass) {
        return !klass.getRawType().isPrimitive() && !klass.isArray() && !klass.isEnum() &&
            !klass.hasParameter() && !klass.isInterface() && !ignoreTypes.contains(klass.getRawType());
    }

    private Set<Class<?>> getIgnoreTypes() {
        return new HashSet<>(Arrays.asList(
            Boolean.class, Byte.class, Character.class, Double.class, Float.class, Integer.class, Long.class,
            Short.class, String.class, Void.class));
    }

    @Override
    public ValidateOf<Object> decode(String path, ConfigNode node, TypeCapture<?> type, DecoderService decoderService) {
        if (!(node instanceof MapNode)) {
            List<TypeCapture<?>> genericInterfaces = type.getParameterTypes();
            return ValidateOf.inValid(new ValidationError.DecodingExpectedMapNodeType(path, genericInterfaces, node.getNodeType()));
        }

        Class<?> klass = type.getRawType();

        try {
            Constructor<?> constructor = klass.getDeclaredConstructor();
            if (Modifier.isPrivate(constructor.getModifiers())) {
                return ValidateOf.inValid(new ValidationError.ConstructorNotPublic(path, klass.getName()));
            }

            List<ValidationError> errors = new ArrayList<>();

            Object obj = klass.getDeclaredConstructor().newInstance();

            List<Field> classFields = getClassFields(klass);
            for (Field field : classFields) {
                int modifiers = field.getModifiers();
                String name = field.getName();
                Type fieldClass = field.getGenericType();

                // if we have an annotation, use that for the path instead of the name.
                Config configAnnotation = field.getAnnotation(Config.class);
                if (configAnnotation != null && configAnnotation.path() != null && !configAnnotation.path().isEmpty()) {
                    name = configAnnotation.path();
                }

                String nextPath = PathUtil.pathForKey(path, name);

                if (!Modifier.isStatic(modifiers)) {
                    field.setAccessible(true);

                    ValidateOf<ConfigNode> configNode = decoderService.getNextNode(nextPath, name, node);

                    errors.addAll(configNode.getErrors());
                    if (!configNode.hasResults()) {
                        // if we have no value, check the config annotation for a default.
                        if (configAnnotation != null && configAnnotation.defaultVal() != null &&
                            !configAnnotation.defaultVal().isEmpty()) {
                            ValidateOf<?> defaultValidateOf = decoderService.decodeNode(nextPath, new LeafNode(configAnnotation.defaultVal()),
                                TypeCapture.of(fieldClass));

                            if (defaultValidateOf.hasErrors()) {
                                errors.addAll(defaultValidateOf.getErrors());
                            }

                            if (defaultValidateOf.hasResults()) {
                                field.set(obj, defaultValidateOf.results());
                            }
                        }
                    } else {
                        ValidateOf<?> fieldValidateOf = decoderService.decodeNode(nextPath, configNode.results(),
                            TypeCapture.of(fieldClass));
                        if (fieldValidateOf.hasErrors()) {
                            errors.addAll(fieldValidateOf.getErrors());
                        }

                        if (fieldValidateOf.hasResults()) {
                            field.set(obj, fieldValidateOf.results());
                        } else {
                            errors.add(new ValidationError.NoResultsFoundForNode(nextPath, field.getType(), "object decoding"));
                        }
                    }
                } else {
                    logger.info("Ignoring static field for class: " + klass.getName() + " field " + name);
                }
            }

            return ValidateOf.validateOf(obj, errors);
        } catch (NoSuchMethodException e) {
            return ValidateOf.inValid(new ValidationError.NoDefaultConstructor(path, klass.getName()));
        } catch (SecurityException | InstantiationException | IllegalAccessException |
                 IllegalArgumentException | InvocationTargetException e) {
            return ValidateOf.inValid(new ValidationError.ConstructorNotPublic(path, klass.getName()));
        }
    }

    private List<Field> getClassFields(Class<?> klass) {
        List<Field> classFields = new ArrayList<>();
        Class<?> currentClass = klass;
        while (currentClass != null) {
            final Field[] fields = currentClass.getDeclaredFields();
            classFields.addAll(Arrays.asList(fields));
            currentClass = currentClass.getSuperclass();
        }
        return classFields;
    }
}

package org.config.gestalt.decoder;

import org.config.gestalt.entity.ValidationError;
import org.config.gestalt.entity.ValidationLevel;
import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.node.MapNode;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.utils.ValidateOf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ObjectDecoder implements Decoder {
    private static final Logger logger = LoggerFactory.getLogger(ObjectDecoder.class.getName());

    @Override
    public String name() {
        return "Object";
    }

    @Override
    public boolean matches(TypeCapture<?> klass) {
        return klass.getRawType().isMemberClass();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ValidateOf<T> decode(String path, ConfigNode node, TypeCapture<T> type, DecoderService decoderService) {
        ValidateOf<T> results;
        if (node instanceof MapNode) {
            Class<?> klass = type.getRawType();

            try {
                Constructor<?> constructor = klass.getDeclaredConstructor();
                if (Modifier.isPrivate(constructor.getModifiers())) {
                    return ValidateOf.inValid(new ValidationError.ConstructorNotPublic(path, klass.getName()));
                }

                List<ValidationError> errors = new ArrayList<>();

                T obj = (T) klass.getDeclaredConstructor().newInstance();

                List<Field> classFields = getClassFields(klass);
                for (Field field : classFields) {
                    int modifiers = field.getModifiers();
                    String name = field.getName();
                    Type fieldClass = field.getGenericType();

                    String nextPath = path != null && !path.isEmpty() ? path + "." + name : name;

                    if (!Modifier.isStatic(modifiers)) {
                        field.setAccessible(true);

                        ValidateOf<ConfigNode> configNode = decoderService.getNextNode(nextPath, name, node);

                        errors.addAll(configNode.getErrors(ValidationLevel.WARN));
                        if (configNode.hasErrors(ValidationLevel.ERROR)) {
                            errors.addAll(configNode.getErrors(ValidationLevel.ERROR));
                        } else if (!configNode.hasResults()) {
                            errors.add(new ValidationError.NoResultsFoundForNode(nextPath, field.getType()));
                        } else {
                            ValidateOf<?> fieldValidateOf = decoderService.decodeNode(nextPath, configNode.results(),
                                TypeCapture.of(fieldClass));
                            if (fieldValidateOf.hasErrors()) {
                                errors.addAll(fieldValidateOf.getErrors());
                            }

                            if (fieldValidateOf.hasResults()) {
                                field.set(obj, fieldValidateOf.results());
                            } else {
                                errors.add(new ValidationError.NoResultsFoundForNode(nextPath, field.getType()));
                            }
                        }
                    } else {
                        logger.warn("Ignoring static field for class: " + klass.getName() + " field " + name);
                    }
                }

                return ValidateOf.validateOf(obj, errors);
            } catch (NoSuchMethodException e) {
                return ValidateOf.inValid(new ValidationError.NoDefaultConstructor(path, klass.getName()));
            } catch (SecurityException | InstantiationException | IllegalAccessException |
                IllegalArgumentException | InvocationTargetException e) {
                return ValidateOf.inValid(new ValidationError.ConstructorNotPublic(path, klass.getName()));
            }
        } else {
            results = ValidateOf.inValid(new ValidationError.DecodingExpectedLeafNodeType(path, node, name()));
        }
        return results;
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

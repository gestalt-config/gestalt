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
import java.util.function.Function;

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
                String fieldName = field.getName();

                if (Modifier.isStatic(modifiers)) {
                    logger.info("Ignoring static field for class: " + klass.getName() + " field " + fieldName);
                    continue;
                }

                Type fieldClass = field.getGenericType();

                String name = getAnnotationValue(klass, field, fieldName, fieldClass, Config::path,
                    config -> config.path() != null && !config.path().isEmpty());

                name = name.isEmpty() ? fieldName : name;

                String nextPath = PathUtil.pathForKey(path, name);
                ValidateOf<ConfigNode> configNode = decoderService.getNextNode(nextPath, name, node);

                errors.addAll(configNode.getErrors());
                if (!configNode.hasResults()) {
                    // if we have no value, check the config annotation for a default.
                    // if we have an annotation, use that for the path instead of the name.
                    String defaultValue = getAnnotationValue(klass, field, fieldName, fieldClass, Config::defaultVal,
                        config -> config.defaultVal() != null && !config.defaultVal().isEmpty());

                    if (!defaultValue.isEmpty()) {
                        ValidateOf<?> defaultValidateOf =
                            decoderService.decodeNode(nextPath, new LeafNode(defaultValue), TypeCapture.of(fieldClass));

                        if (defaultValidateOf.hasErrors()) {
                            errors.addAll(defaultValidateOf.getErrors());
                        }

                        if (defaultValidateOf.hasResults()) {
                            field.setAccessible(true);
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
                        field.setAccessible(true);
                        field.set(obj, fieldValidateOf.results());
                    } else {
                        errors.add(new ValidationError.NoResultsFoundForNode(nextPath, field.getType(), "object decoding"));
                    }
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

    private String getAnnotationValue(Class<?> klass, Field field, String fieldName, Type fieldClass,
                                      Function<Config, String> get, Function<Config, Boolean> find) {
        String value = "";
        // if we have an annotation, use that for the path instead of the name.
        Optional<Config> configAnnotation = Optional.ofNullable(field.getAnnotation(Config.class));
        if (configAnnotation.isPresent() && find.apply(configAnnotation.get())) {
            value = get.apply(configAnnotation.get());
        } else {

            // If there is no field annotation, check if there is a method field.
            configAnnotation = findMethodConfig(klass, fieldName, fieldClass, find);

            if (configAnnotation.isPresent() && find.apply(configAnnotation.get())) {
                value = get.apply(configAnnotation.get());
            }
        }
        return value;
    }

    private Optional<Config> findMethodConfig(Class<?> klass, String fieldName, Type fieldClass, Function<Config, Boolean> find) {
        Optional<Config> configAnnotation;
        String methodName;
        if (fieldClass.equals(boolean.class) || fieldClass.equals(Boolean.TYPE)) {
            methodName = "is" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        } else {
            methodName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        }
        // first look for the method with the name and annotations.
        // This would be either isfeildName or getFieldName. If we dont find that try just the field name
        configAnnotation = getMethodAnnotation(klass, methodName, find).or(() -> getMethodAnnotation(klass, fieldName, find));
        return configAnnotation;
    }

    private Optional<Config> getMethodAnnotation(Class<?> klass, String methodName, Function<Config, Boolean> find) {
        Config result = null;

        var method = Arrays.stream(klass.getMethods())
                           .filter(it -> it.getName().equals(methodName))
                           .filter(it -> {
                               Config methodConfigAnnotation = it.getAnnotation(Config.class);
                               return methodConfigAnnotation != null && find.apply(methodConfigAnnotation);
                           })
                           .findFirst();

        if (method.isPresent()) {
            result = method.get().getAnnotation(Config.class);

        }
        return Optional.ofNullable(result);
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

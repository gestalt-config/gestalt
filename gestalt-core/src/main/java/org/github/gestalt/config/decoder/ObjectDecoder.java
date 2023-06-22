package org.github.gestalt.config.decoder;

import org.github.gestalt.config.annotations.Config;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.node.MapNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.utils.ClassUtils;
import org.github.gestalt.config.utils.PathUtil;
import org.github.gestalt.config.utils.ValidateOf;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;

import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.WARNING;

/**
 * Decode a class. This decoder is best suited for pojo style classes.
 * Will fail if the constructor is private.
 * Will construct the class even if there are missing values, the values will be null or the default. Then it will return errors.
 * Decodes member classes and lists as well.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public class ObjectDecoder implements Decoder<Object> {
    private static final System.Logger logger = System.getLogger(ObjectDecoder.class.getName());

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
        return new HashSet<>(List.of(
            Boolean.class, Byte.class, Character.class, Double.class, Float.class, Integer.class, Long.class,
            Short.class, String.class, Void.class));
    }

    @Override
    public ValidateOf<Object> decode(String path, ConfigNode node, TypeCapture<?> type, DecoderService decoderService) {
        if (!(node instanceof MapNode)) {
            List<TypeCapture<?>> genericInterfaces = type.getParameterTypes();
            return ValidateOf.inValid(new ValidationError.DecodingExpectedMapNodeType(path, genericInterfaces, node));
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
                    logger.log(INFO, "Ignoring static field for class: " + klass.getName() + " field " + fieldName);
                    continue;
                }

                Type fieldClass = field.getGenericType();
                TypeCapture fieldType = TypeCapture.of(fieldClass);

                // check if the field has an annotation, which will override the field name.
                String name = getFieldAnnotationValue(klass, field, fieldName, fieldClass, Config::path);

                name = name.isEmpty() ? fieldName : name;

                // check if there is a node in the map for this field.
                String nextPath = PathUtil.pathForKey(path, name);
                ValidateOf<ConfigNode> configNode = decoderService.getNextNode(nextPath, name, node);

                errors.addAll(configNode.getErrors());
                if (configNode.hasResults()) {
                    // if the map node has a value for the field.
                    //decode the field node.
                    ValidateOf<?> fieldValidateOf = decoderService.decodeNode(nextPath, configNode.results(), fieldType);

                    errors.addAll(fieldValidateOf.getErrors());
                    if (fieldValidateOf.hasResults()) {
                        // set the field to the decoded value for the map node.
                        setField(obj, field, klass, fieldValidateOf.results());
                    } else {
                        // if we have a node for this field but the decoding failed,
                        // check to see if the field value result will be null. If so add a null value error
                        Object value = getObject(obj, field, klass);
                        if (value == null) {
                            errors.add(new ValidationError.NullValueDecodingObject(nextPath, fieldName, klass.getSimpleName()));
                        } else {
                            errors.add(new ValidationError.NoResultsFoundForNode(nextPath, field.getType(), "object decoding"));
                        }
                    }
                } else {
                    // if we have no value for this field, check the config annotation for a default.
                    // if we have an annotation, use that for the path instead of the name.
                    String defaultValue = getFieldAnnotationValue(klass, field, fieldName, fieldClass, Config::defaultVal);

                    if (!defaultValue.isEmpty()) {
                        // if we have a default value in the annotation attempt to decode it as a leaf of the field type.
                        ValidateOf<?> defaultValidateOf = decoderService.decodeNode(nextPath, new LeafNode(defaultValue), fieldType);

                        errors.addAll(defaultValidateOf.getErrors());
                        // if the default value decoded to the expected field type set the field to the default value.
                        if (defaultValidateOf.hasResults()) {
                            setField(obj, field, klass, defaultValidateOf.results());
                        } else {

                            // if we failed to decode the default annotation value, check to see if the field value result will be null.
                            // If so add a null value error
                            Object value = getObject(obj, field, klass);
                            if (value == null) {
                                errors.add(new ValidationError.NullValueDecodingObject(nextPath, fieldName, klass.getSimpleName()));
                            }
                        }
                    } else {

                        // even though we have default value in the annotation lets try to decode the field,
                        // as it may be an optional that can support null values.
                        ValidateOf<?> decodedResults = decoderService.decodeNode(nextPath, configNode.results(), fieldType);

                        // if the decoder supported nullable types (such as optional) set the field to the value.
                        if (decodedResults.hasResults()) {
                            setField(obj, field, klass, decodedResults.results());
                        } else {
                            // if we have no default and the type doesn't support null values,
                            // check to see if the field value result will be null. If so add a null value error
                            Object value = getObject(obj, field, klass);
                            if (value == null) {
                                errors.add(new ValidationError.NullValueDecodingObject(nextPath, fieldName, klass.getSimpleName()));
                            }
                        }
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

    private Object getObject(Object obj, Field field, Class<?> klass) throws IllegalAccessException {

        String methodName;
        if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.TYPE)) {
            methodName = "is" + field.getName();
        } else {
            methodName = "get" + field.getName();
        }

        var method = getMethod(klass, methodName);
        if (method.isPresent()) {
            try {
                return method.get().invoke(obj);
            } catch (InvocationTargetException e) {
                logger.log(WARNING, "Failed to get value calling method " + methodName + ", on class " + klass.getSimpleName() +
                    ", for field " + field.getName());
            }
        }

        field.setAccessible(true);
        return field.get(obj);

    }

    private String getFieldAnnotationValue(Class<?> klass, Field field, String fieldName, Type fieldClass,
                                           Function<Config, String> get) {
        String value = "";
        // if we have an annotation, use that for the path instead of the name.
        Optional<Config> configAnnotation = Optional.ofNullable(field.getAnnotation(Config.class));
        if (configAnnotation.isPresent() &&
            get.apply(configAnnotation.get()) != null &&
            !get.apply(configAnnotation.get()).isEmpty()
        ) {
            value = get.apply(configAnnotation.get());
        } else {

            // If there is no field annotation, check if there is a method field.
            configAnnotation = findMethodConfig(klass, fieldName, fieldClass, get);

            if (configAnnotation.isPresent() &&
                get.apply(configAnnotation.get()) != null &&
                !get.apply(configAnnotation.get()).isEmpty()
            ) {
                value = get.apply(configAnnotation.get());
            }
        }
        return value;
    }

    private Optional<Config> findMethodConfig(Class<?> klass, String fieldName, Type fieldClass, Function<Config, String> get) {
        Optional<Config> configAnnotation;
        String methodName;
        if (fieldClass.equals(boolean.class) || fieldClass.equals(Boolean.TYPE)) {
            methodName = "is" + fieldName;
        } else {
            methodName = "get" + fieldName;
        }
        // first look for the method with the name and annotations.
        // This would be either isfeildName or getFieldName. If we dont find that try just the field name
        configAnnotation = getMethodAnnotation(klass, methodName, get).or(() -> getMethodAnnotation(klass, fieldName, get));
        return configAnnotation;
    }

    private Optional<Config> getMethodAnnotation(Class<?> klass, String methodName, Function<Config, String> get) {
        Config result = null;

        var method = getMethod(klass, methodName);

        if (method.isPresent()) {
            Config methodConfigAnnotation = method.get().getAnnotation(Config.class);
            if (methodConfigAnnotation != null &&
                get.apply(methodConfigAnnotation) != null &&
                !get.apply(methodConfigAnnotation).isEmpty()) {
                result = method.get().getAnnotation(Config.class);
            }
        }
        return Optional.ofNullable(result);
    }

    private Optional<Method> getMethod(Class<?> klass, String methodName) {
        return Arrays.stream(klass.getMethods())
                     .filter(it -> it.getName().equalsIgnoreCase(methodName))
                     .findFirst();
    }

    private void setField(Object obj, Field field, Class<?> klass, Object value) throws IllegalAccessException {

        String methodName = "set" + field.getName();
        var setMethod = getMethod(klass, methodName);
        if (setMethod.isPresent() &&
            setMethod.get().getParameterCount() == 1 &&
            ClassUtils.isAssignable(setMethod.get().getParameters()[0].getType(), value.getClass())) {
            try {
                setMethod.get().invoke(obj, value);
            } catch (InvocationTargetException e) {
                logger.log(WARNING, "unable to set field " + field.getName() + " using method " + methodName +
                    ", on class " + klass.getSimpleName() + ", for val: " + value + ", setting field directly");

                field.setAccessible(true);
                field.set(obj, value);
            }
        } else {
            field.setAccessible(true);
            field.set(obj, value);
        }
    }

    private List<Field> getClassFields(Class<?> klass) {
        List<Field> classFields = new ArrayList<>();
        Class<?> currentClass = klass;
        while (currentClass != null) {
            final Field[] fields = currentClass.getDeclaredFields();
            classFields.addAll(List.of(fields));
            currentClass = currentClass.getSuperclass();
        }
        return classFields;
    }
}

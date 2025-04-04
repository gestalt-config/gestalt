package org.github.gestalt.config.decoder;

import org.github.gestalt.config.annotations.Config;
import org.github.gestalt.config.annotations.ConfigParameter;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.entity.ValidationError.OptionalMissingValueDecoding;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.node.MapNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.ClassUtils;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.PathUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.WARNING;

/**
 * Decode a class. This decoder is best suited for pojo style classes.
 * Will fail if the constructor is private.
 * Will construct the class even if there are missing values, the values will be null or the default. Then it will return errors.
 * Decodes member classes and lists as well.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class ObjectDecoder implements Decoder<Object> {
    private static final System.Logger logger = System.getLogger(ObjectDecoder.class.getName());

    private final Set<Class<?>> ignoreTypes;

    private final Pattern argPattern = Pattern.compile("^arg\\d$");

    /**
     * constructor for the ObjectDecoder.
     */
    public ObjectDecoder() {
        ignoreTypes = getIgnoreTypes();
    }


    private String getMethodName(Field field) {
        String methodName;
        if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {
            methodName = "is" + field.getName();
        } else {
            methodName = "get" + field.getName();
        }
        return methodName;
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
    public boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type) {
        return !type.getRawType().isPrimitive() && !type.isArray() && !type.isEnum() &&
            !type.hasParameter() && !type.isInterface() && !ignoreTypes.contains(type.getRawType());
    }

    private Set<Class<?>> getIgnoreTypes() {
        return new HashSet<>(List.of(
            Boolean.class, Byte.class, Character.class, Double.class, Float.class, Integer.class, Long.class,
            Short.class, String.class, Void.class));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public GResultOf<Object> decode(String path, Tags tags, ConfigNode node, TypeCapture<?> type, DecoderContext decoderContext) {
        if (!(node instanceof MapNode)) {
            List<TypeCapture<?>> genericInterfaces = type.getParameterTypes();
            return GResultOf.errors(new ValidationError.DecodingExpectedMapNodeType(path, genericInterfaces, node));
        }
        Class<?> klass = type.getRawType();

        DecoderService decoderSrv = decoderContext.getDecoderService();

        try {

            // Try and get the object by the constructor first
            Optional<Object> constructorObject = getByConstructor(path, tags, node, decoderContext, klass, decoderSrv);
            if (constructorObject.isPresent()) return GResultOf.result(constructorObject.get());

            Constructor<?> constructor = klass.getDeclaredConstructor();
            if (Modifier.isPrivate(constructor.getModifiers())) {
                return GResultOf.errors(new ValidationError.ConstructorNotPublic(path, klass.getName()));
            }

            List<ValidationError> errors = new ArrayList<>();

            Object obj = klass.getDeclaredConstructor().newInstance();

            List<Field> classFields = getClassFields(klass);
            for (Field field : classFields) {
                int modifiers = field.getModifiers();
                String fieldName = field.getName();
                boolean foundValue = false;

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
                String nextPath = PathUtil.pathForKey(decoderContext.getDefaultLexer(), path, name);
                GResultOf<ConfigNode> configNode = decoderSrv.getNextNode(nextPath, name, node);

                // Add any errors that are not missing value ones.
                errors.addAll(configNode.getErrorsNotLevel(ValidationLevel.MISSING_VALUE));

                if (configNode.hasResults()) {
                    // if the map node has a value for the field.
                    //decode the field node.
                    GResultOf<?> decodeResultOf = decoderSrv.decodeNode(nextPath, tags, configNode.results(), fieldType, decoderContext);

                    errors.addAll(decodeResultOf.getErrors());
                    if (decodeResultOf.hasResults()) {
                        foundValue = true;
                        // set the field to the decoded value for the map node.
                        setField(obj, field, klass, decodeResultOf.results());
                    }

                } else {
                    // if we have no value for this field, check the config annotation for a default.
                    // if we have an annotation, use that for the path instead of the name.
                    String defaultValue = getFieldAnnotationValue(klass, field, fieldName, fieldClass, Config::defaultVal);

                    if (!defaultValue.isEmpty()) {
                        // if we have a default value in the annotation attempt to decode it as a leaf of the field type.
                        GResultOf<?> defaultGResultOf = decoderSrv
                            .decodeNode(nextPath, tags, new LeafNode(defaultValue), fieldType, decoderContext);

                        errors.addAll(defaultGResultOf.getErrors());
                        // if the default value decoded to the expected field type set the field to the default value.
                        if (defaultGResultOf.hasResults()) {
                            foundValue = true;
                            setField(obj, field, klass, defaultGResultOf.results());
                            errors.add(new OptionalMissingValueDecoding(nextPath, node, name(), decoderContext));
                        }
                    } else {
                        // even though we have default value in the annotation lets try to decode the field,
                        // as it may be an optional that can support null values.
                        GResultOf<?> decodedResults = decoderSrv
                            .decodeNode(nextPath, tags, configNode.results(), fieldType, decoderContext);

                        // if the decoder supported nullable types (such as optional) set the field to the value.
                        if (decodedResults.hasResults()) {
                            //only add the errors if we actually found a result, otherwise we dont care.
                            errors.addAll(decodedResults.getErrorsNotLevel(ValidationLevel.MISSING_OPTIONAL_VALUE));
                            errors.add(new OptionalMissingValueDecoding(nextPath, node, name(), klass.getSimpleName(), decoderContext));
                            foundValue = true;
                            setField(obj, field, klass, decodedResults.results());
                        }
                    }
                }

                if (!foundValue) {
                    // if we have not set the field
                    // check to see if the field value result will be null. If so add a null value error
                    boolean initialized = fieldHasInitializedValue(obj, field, klass);
                    if (initialized) {
                        errors.add(new OptionalMissingValueDecoding(nextPath, node, name(), klass.getSimpleName(), decoderContext));
                    } else {
                        // first check the field to see if it is annotated with nullable.
                        var fieldAnnotations = field.getAnnotations();
                        boolean isNullable = isNullableAnnotation(fieldAnnotations);

                        if (!isNullable) {
                            // if the field isnt annotated with nullable, check if the get method is annotated with nullable.
                            String methodName = getMethodName(field);
                            var method = getMethod(klass, methodName).or(() -> getMethod(klass, fieldName));
                            if (method.isPresent()) {
                                var methodAnnotations = method.get().getAnnotations();
                                isNullable = isNullableAnnotation(methodAnnotations);
                            }
                        }

                        if (!isNullable) {
                            errors.add(new ValidationError.NoResultsFoundForNode(nextPath, klass.getSimpleName(), "object decoding"));
                        } else {
                            errors.add(new OptionalMissingValueDecoding(nextPath, node, name(), klass.getSimpleName(), decoderContext));
                        }
                    }
                }
            }

            return GResultOf.resultOf(obj, errors);
        } catch (NoSuchMethodException e) {
            return GResultOf.errors(new ValidationError.NoDefaultConstructor(path, klass.getName()));
        } catch (SecurityException | InstantiationException | IllegalAccessException |
                 IllegalArgumentException | InvocationTargetException e) {
            return GResultOf.errors(new ValidationError.ConstructorNotPublic(path, klass.getName()));
        }
    }

    // try and build the object using a constructor.
    private Optional<Object> getByConstructor(String path, Tags tags, ConfigNode node, DecoderContext decoderContext, Class<?> klass, DecoderService decoderSrv) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        Constructor<?>[] constructors = klass.getDeclaredConstructors();
        List<Constructor<?>> sortedConstructors = Arrays.stream(constructors)
            .sorted((p1, p2) -> p2.getParameterCount() - p1.getParameterCount())
            .collect(Collectors.toList());

        for (Constructor<?> constructor : sortedConstructors) {
            if (Modifier.isPrivate(constructor.getModifiers())) {
                continue;
            }
            Parameter[] constParams = constructor.getParameters();
            Object[] parameters = new Object[constParams.length];

            // for now ignore the default constructor.
            if (constParams.length == 0) {
                continue;
            }

            boolean suitable = true;
            for (int i = 0; i < constParams.length; i++) {
                String paramName = getParameterName(constParams[i]);

                String nextPath = PathUtil.pathForKey(decoderContext.getDefaultLexer(), path, paramName);
                GResultOf<ConfigNode> configNode = decoderSrv.getNextNode(nextPath, paramName, node);
                ConfigNode paramNode;
                if (configNode.hasResults()) {
                    paramNode = configNode.results();
                } else {
                    // if we have not found a node for the parameter try and see if it has a @Config default
                    Optional<String> defaultValue = getParameterDefault(constParams[i]);
                    if (defaultValue.isPresent() && !defaultValue.get().isEmpty()) {
                        // if we have a default value in the annotation attempt to decode it as a leaf of the field type.
                       paramNode = new LeafNode(defaultValue.get());
                    } else {
                        suitable = false;
                        break;
                    }
                }

                GResultOf<?> decodeResult = decoderSrv.decodeNode(nextPath, tags, paramNode,
                    TypeCapture.of(constParams[i].getType()), decoderContext);

                if (decodeResult.hasResults()) {
                    parameters[i] = decodeResult.results();
                } else {
                    suitable = false;
                    break;
                }
            }

            if (suitable) {
                constructor.setAccessible(true);
                return Optional.of(constructor.newInstance(parameters));
            }
        }
        return Optional.empty();
    }

    private static boolean isNullableAnnotation(Annotation[] fieldAnnotations) {
        return Arrays.stream(fieldAnnotations)
            .anyMatch(it -> it.annotationType().getName().toLowerCase(Locale.getDefault()).contains("nullable"));
    }

    private boolean fieldHasInitializedValue(Object obj, Field field, Class<?> klass) throws IllegalAccessException {
        String methodName = getMethodName(field);

        Object fieldValue = null;
        var method = getMethod(klass, methodName);
        if (method.isPresent()) {
            try {
                fieldValue = method.get().invoke(obj);
            } catch (InvocationTargetException e) {
                logger.log(WARNING, "Failed to get value calling method " + methodName + ", on class " + klass.getSimpleName() +
                    ", for field " + field.getName());
            }
        }

        if (fieldValue == null) {
            field.setAccessible(true);
            fieldValue = field.get(obj);
        }

        // Unfortunantly, there is no easy way to tell if primitives have been initialized.
        // we check for 0, if it is 0 we assume that it has not been initialized.
        // but what if the intended default was 0? We have no way of knowing. So we default to say it is not initialized.
        if (field.getType().isPrimitive()) {
            if (byte.class.isAssignableFrom(fieldValue.getClass()) || Byte.class.isAssignableFrom(fieldValue.getClass())) {
                return ((byte) fieldValue) != 0;
            } else if (short.class.isAssignableFrom(fieldValue.getClass()) || Short.class.isAssignableFrom(fieldValue.getClass())) {
                return ((short) fieldValue) != 0;
            } else if (int.class.isAssignableFrom(fieldValue.getClass()) || Integer.class.isAssignableFrom(fieldValue.getClass())) {
                return ((int) fieldValue) != 0;
            } else if (long.class.isAssignableFrom(fieldValue.getClass()) || Long.class.isAssignableFrom(fieldValue.getClass())) {
                return ((long) fieldValue) != 0;
            } else if (float.class.isAssignableFrom(fieldValue.getClass()) || Float.class.isAssignableFrom(fieldValue.getClass())) {
                return ((float) fieldValue) != 0;
            } else if (double.class.isAssignableFrom(fieldValue.getClass()) || Double.class.isAssignableFrom(fieldValue.getClass())) {
                return ((double) fieldValue) != 0;
            } else if (boolean.class.isAssignableFrom(fieldValue.getClass()) || Boolean.class.isAssignableFrom(fieldValue.getClass())) {
                return (boolean) fieldValue;
            } else if (char.class.isAssignableFrom(fieldValue.getClass()) || Character.class.isAssignableFrom(fieldValue.getClass())) {
                return ((char) fieldValue) != 0;
            }
            return false;
        } else {
            return fieldValue != null;
        }

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

    /**
     * Gets the name of the parameter as either the @Config path or if compiled with -parameters the parameters name.
     *
     * @param parameter parameter we are trying to find a name for.
     * @return parameter name or "" if none
     */
    private String getParameterName(Parameter parameter) {
        // if we have an annotation, use that for the path instead of the name.
        Optional<ConfigParameter> configAnnotation = Optional.ofNullable(parameter.getAnnotation(ConfigParameter.class));
        if (configAnnotation.isPresent() &&
            configAnnotation.get().path() != null &&
            !configAnnotation.get().path().isEmpty()
        ) {
            return configAnnotation.get().path();
        }

        if (!argPattern.matcher(parameter.getName()).find()) {
            return parameter.getName();
        }

        return "";
    }

    private Optional<String> getParameterDefault(Parameter parameter) {
        // if we have an annotation, use that for the path instead of the name.
        Optional<ConfigParameter> configAnnotation = Optional.ofNullable(parameter.getAnnotation(ConfigParameter.class));
        if (configAnnotation.isPresent() &&
            configAnnotation.get().defaultVal() != null &&
            !configAnnotation.get().defaultVal().isEmpty()
        ) {
            return Optional.of(configAnnotation.get().defaultVal());
        }

        return Optional.empty();
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

package org.github.gestalt.config.cdi;


import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.AnnotatedMember;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Provider;
import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

/**
 * Actual implementations for producer method in CDI producer {@link GestaltConfigProducer}.
 *
 * <p>Based on <a href="https://github.com/smallrye/smallrye-config/tree/3.1.1/cdi">...</a>
 *
 * @author <a href="https://github.com/guhilling">Gunnar Hilling</a>
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025. (c) 2024.
 */
@SuppressWarnings({"StringSplitter", "TypeParameterUnusedInFormals"})
public final class GestaltConfigProducerUtil {

    private GestaltConfigProducerUtil() {
        throw new UnsupportedOperationException();
    }

    /**
     * Retrieves a converted configuration value from {@link InjectConfig}.
     *
     * @param injectionPoint the {@link InjectionPoint} where the configuration value will be injected
     * @param config         the current {@link InjectConfig} instance.
     * @param <T>            type of class to get.
     * @return the converted configuration value.
     */
    public static <T> T getValue(InjectionPoint injectionPoint, Gestalt config) {
        return getValue(getName(injectionPoint), getType(injectionPoint), getDefaultValue(injectionPoint), config);
    }

    /**
     * Retrieves a converted configuration value from {@link Gestalt}.
     *
     * @param name         the name of the configuration property.
     * @param type         the {@link Type} of the configuration value to convert.
     * @param defaultValue the default value to use if no configuration value is found.
     * @param config       the current {@link Gestalt} instance.
     * @param <T>          type of class to get.
     * @return the converted configuration value.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getValue(String name, Type type, String defaultValue, Gestalt config) {
        try {
            if (defaultValue == null || defaultValue.isEmpty()) {
                return config.getConfig(name, TypeCapture.of(type));
            } else {
                Optional<T> optionalValue = config.getConfigOptional(name, TypeCapture.of(type));

                if (optionalValue.isPresent()) {
                    return optionalValue.get();
                }

                GResultOf<T> result = config.getDecoderService()
                    .decodeNode(name, Tags.of(), defaultValue, TypeCapture.of(type), config.getDecoderContext());

                if (result.hasResults()) {
                    return result.results();
                } else {
                    throw new GestaltConfigException("Unable to find value for " + name +
                        ", failed to get default value " + defaultValue, name);
                }
            }

        } catch (GestaltException e) {
            throw new GestaltConfigException("Exception getting configuration for " + name + " type " + type, e);
        }
    }

    /**
     * Retrieves a converted configuration value from {@link InjectConfig}.
     *
     * @param injectionPoint the {@link InjectionPoint} where the configuration value will be injected
     * @param config         the current {@link InjectConfig} instance.
     * @param <T>            type of class to get.
     * @return the converted configuration value.
     */
    public static <T> Optional<T> getOptionalValue(InjectionPoint injectionPoint, Gestalt config) {
        return getOptionalValue(getName(injectionPoint), getType(injectionPoint), getDefaultValue(injectionPoint), config);
    }

    /**
     * Retrieves a converted configuration value from {@link Gestalt}.
     *
     * @param name         the name of the configuration property.
     * @param type         the {@link Type} of the configuration value to convert.
     * @param defaultValue the default value to use if no configuration value is found.
     * @param config       the current {@link Gestalt} instance.
     * @param <T>          type of class to get.
     * @return the converted configuration value.
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> getOptionalValue(String name, Type type, String defaultValue, Gestalt config) {
        TypeCapture<Optional<T>> typeCapture = TypeCapture.of(type);
        return config.getConfigOptional(name, (TypeCapture<T>) typeCapture.getFirstParameterType());
    }

    /**
     * Retrieves a converted configuration value from {@link InjectConfig}.
     *
     * @param injectionPoint the {@link InjectionPoint} where the configuration value will be injected
     * @param config         the current {@link Gestalt} instance.
     * @param <T>            type of class to get.
     * @return the converted configuration value.
     */
    public static <T> T getSupplierValue(InjectionPoint injectionPoint, Gestalt config) {
        return getSupplierValue(getName(injectionPoint), getType(injectionPoint), getDefaultValue(injectionPoint), config);
    }

    /**
     * Retrieves a converted configuration value from {@link Gestalt}.
     *
     * @param name         the name of the configuration property.
     * @param type         the {@link Type} of the configuration value to convert.
     * @param defaultValue the default value to use if no configuration value is found.
     * @param config       the current {@link Gestalt} instance.
     * @param <T>          type of class to get.
     * @return the converted configuration value.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getSupplierValue(String name, Type type, String defaultValue, Gestalt config) {
        TypeCapture<T> typeCapture = TypeCapture.of(type);
        try {
            return config.getConfig(name, (TypeCapture<T>) typeCapture.getFirstParameterType());
        } catch (GestaltException e) {
            throw new GestaltConfigException("Exception getting supplier value for " + name + " type " + type, e);
        }
    }

    private static Type getType(InjectionPoint injectionPoint) {
        Type type = injectionPoint.getType();
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            if (parameterizedType.getRawType().equals(Provider.class) || parameterizedType.getRawType().equals(Instance.class)) {
                return parameterizedType.getActualTypeArguments()[0];
            }
        }
        return type;
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> rawTypeOf(final Type type) {
        if (type instanceof Class<?>) {
            return (Class<T>) type;
        } else if (type instanceof ParameterizedType) {
            return rawTypeOf(((ParameterizedType) type).getRawType());
        } else if (type instanceof GenericArrayType) {
            return (Class<T>) Array.newInstance(rawTypeOf(((GenericArrayType) type).getGenericComponentType()), 0).getClass();
        } else {
            throw new GestaltConfigException("Not supported raw type", type.getTypeName());
        }
    }

    private static String getName(InjectionPoint injectionPoint) {
        for (Annotation qualifier : injectionPoint.getQualifiers()) {
            if (qualifier.annotationType().equals(InjectConfig.class)) {
                InjectConfig configProperty = (InjectConfig) qualifier;
                return getConfigKey(injectionPoint, configProperty);
            }
        }
        return null;
    }

    private static String getDefaultValue(InjectionPoint injectionPoint) {
        for (Annotation qualifier : injectionPoint.getQualifiers()) {
            if (qualifier.annotationType().equals(InjectConfig.class)) {
                String str = ((InjectConfig) qualifier).defaultValue();
                if (!str.isBlank()) {
                    return str;
                }
                Class<?> rawType = rawTypeOf(injectionPoint.getType());
                if (rawType.isPrimitive()) {
                    if (rawType == char.class) {
                        return "";
                    } else if (rawType == boolean.class) {
                        return "false";
                    } else {
                        return "0";
                    }
                }
                return null;
            }
        }
        return null;
    }

    static String getConfigKey(InjectionPoint ip, InjectConfig configProperty) {
        String key = configProperty.path();
        if (!key.isBlank()) {
            return key;
        }
        if (ip.getAnnotated() instanceof AnnotatedMember) {
            AnnotatedMember<?> member = (AnnotatedMember<?>) ip.getAnnotated();
            AnnotatedType<?> declaringType = member.getDeclaringType();
            if (declaringType != null) {
                String[] parts = declaringType.getJavaClass().getCanonicalName().split("\\.");
                StringBuilder sb = new StringBuilder(parts[0]);
                for (int i = 1; i < parts.length; i++) {
                    sb.append('.').append(parts[i]);
                }
                sb.append('.').append(member.getJavaMember().getName());
                return sb.toString();
            }
        }
        throw new GestaltConfigException("No Configuration Property Name");
    }

}

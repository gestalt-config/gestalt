package org.github.gestalt.config.decoder;

import org.github.gestalt.config.annotations.Config;
import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.entity.ValidationError.OptionalMissingValueDecoding;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.node.MapNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.reload.CoreReloadListener;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.ClassUtils;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.Pair;
import org.github.gestalt.config.utils.PathUtil;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Allows a user to create an object from an interface and retrieve configuration values.
 * Gestalt expects a standard java bean where the member value would be carModel then the method would be String getCarModel()
 * If the interface has a default method and is missing a config, it will return the default value.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class ProxyDecoder implements Decoder<Object> {

    // For the proxy decoder, if we should use a cached value or call gestalt for the most recent value.
    private ProxyDecoderMode proxyDecoderMode = ProxyDecoderMode.CACHE;

    private GestaltConfig config;


    private static String getConfigNameFromMethod(String methodName, Type returnType) {
        String name = methodName;
        if (methodName.startsWith("get")) {
            name = methodName.substring(3);
        } else if (methodName.startsWith("is") &&
            (returnType.equals(boolean.class) || returnType.equals(Boolean.TYPE))) {
            name = methodName.substring(2);
        }

        // since the first characters are likely upper case, lowercase only the first character.
        // The method is getCar and we remove get, we have Car.
        return name.substring(0, 1).toLowerCase(Locale.ROOT) + name.substring(1);
    }

    @Override
    public void applyConfig(GestaltConfig config) {
        proxyDecoderMode = config.getProxyDecoderMode();
        this.config = config;
    }

    /**
     * must have a higher priority than the object decoder.
     *
     * @return VERY_LOW
     */
    @Override
    public Priority priority() {
        return Priority.LOW;
    }

    @Override
    public String name() {
        return "proxy";
    }

    @Override
    public boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type) {
        return type.isInterface() &&
            !Collection.class.isAssignableFrom(type.getRawType()) &&
            !Map.class.isAssignableFrom(type.getRawType());
    }

    @Override
    public GResultOf<Object> decode(String path, Tags tags, ConfigNode node, TypeCapture<?> type, DecoderContext decoderContext) {
        if (!(node instanceof MapNode)) {
            return GResultOf.errors(new ValidationError.DecodingExpectedMapNodeType(path, node));
        }

        Class<?> klass = type.getRawType();

        List<ValidationError> errors = new ArrayList<>();

        Method[] classMethods = klass.getMethods();

        DecoderService decoderService = decoderContext.getDecoderService();

        Map<String, Object> methodResults = new HashMap<>();
        // for each method, we want to get the corresponding bean value. ie if it is getCar, the bean value would be car.
        // Then get the configuration for the bean value and decode it.
        // Save it into a cache for use with the proxy.
        for (Method method : classMethods) {
            String methodName = method.getName();
            Type returnType = method.getGenericReturnType();
            boolean foundValue = false;

            String name;

            // if we have an annotation, use that for the path instead of the name.
            Config configAnnotation = method.getAnnotation(Config.class);
            if (configAnnotation != null && configAnnotation.path() != null && !configAnnotation.path().isEmpty()) {
                name = configAnnotation.path();
            } else {
                name = getConfigNameFromMethod(methodName, returnType);
            }

            String nextPath = PathUtil.pathForKey(decoderContext.getDefaultLexer(), path, name);

            GResultOf<ConfigNode> configNode = decoderService.getNextNode(nextPath, name, node);

            // Add any errors that are not missing value ones.
            errors.addAll(configNode.getErrorsNotLevel(ValidationLevel.MISSING_VALUE));

            if (configNode.hasResults()) {
                GResultOf<?> fieldGResultOf = decoderService.decodeNode(nextPath, tags, configNode.results(),
                    TypeCapture.of(returnType), decoderContext);

                errors.addAll(fieldGResultOf.getErrors());
                if (fieldGResultOf.hasResults()) {
                    methodResults.put(methodName, fieldGResultOf.results());
                    foundValue = true;
                }
            } else {

                // if we have no value, check the config annotation for a default.
                if (configAnnotation != null && configAnnotation.defaultVal() != null && !configAnnotation.defaultVal().isEmpty()) {
                    GResultOf<?> defaultGResultOf = decoderService.decodeNode(nextPath, tags, new LeafNode(configAnnotation.defaultVal()),
                        TypeCapture.of(returnType), decoderContext);

                    errors.addAll(defaultGResultOf.getErrors());
                    if (defaultGResultOf.hasResults()) {
                        methodResults.put(methodName, defaultGResultOf.results());
                        foundValue = true;
                        errors.add(new OptionalMissingValueDecoding(nextPath, node, name(), klass.getSimpleName(), decoderContext));
                    }
                } else {
                    // even though we have default value in the annotation lets try to decode the field,
                    // as it may be an optional that can support null values.
                    GResultOf<?> decodedResults = decoderService
                        .decodeNode(nextPath, tags, configNode.results(), TypeCapture.of(returnType), decoderContext);

                    // if the decoder supported nullable types (such as optional) set the field to the value.
                    if (decodedResults.hasResults()) {
                        //only add the errors if we actually found a result, otherwise we dont care.
                        errors.addAll(decodedResults.getErrorsNotLevel(ValidationLevel.MISSING_OPTIONAL_VALUE));
                        errors.add(new OptionalMissingValueDecoding(nextPath, node, name(), klass.getSimpleName(), decoderContext));
                        foundValue = true;
                        methodResults.put(methodName, decodedResults.results());
                    }
                }
            }

            if (!foundValue && !method.isDefault()) {
                errors.add(new ValidationError.NoResultsFoundForNode(nextPath, type.getRawType(), "proxy decoding"));
            } else if (!foundValue && method.isDefault()) {
                errors.add(new OptionalMissingValueDecoding(nextPath, node, name(), klass.getSimpleName(), decoderContext));
            }
        }

        InvocationHandler proxyHandler;
        switch (proxyDecoderMode) {

            case PASSTHROUGH: {
                proxyHandler = new ProxyPassThroughInvocationHandler(path, tags, decoderContext, config);
                break;
            }

            case CACHE:
            default: {
                proxyHandler = new ProxyCacheInvocationHandler(path, tags, decoderContext, config, methodResults);
                if (decoderContext.getGestalt() != null) {
                    decoderContext.getGestalt().registerListener((ProxyCacheInvocationHandler) proxyHandler);
                }
                break;
            }
        }

        Object myProxy = Proxy.newProxyInstance(type.getRawType().getClassLoader(), new Class<?>[]{type.getRawType()}, proxyHandler);
        return GResultOf.resultOf(myProxy, errors);
    }


    static class ProxyPassThroughInvocationHandler implements InvocationHandler {
        protected final String path;
        protected final Tags tags;
        protected final DecoderContext decoderContext;
        protected final GestaltConfig config;


        private ProxyPassThroughInvocationHandler(String path, Tags tags, DecoderContext decoderContext, GestaltConfig config) {
            this.path = path;
            this.tags = tags;
            this.decoderContext = decoderContext;
            this.config = config;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            Class<?> returnType = method.getReturnType();

            Optional<Object> result = retrieveConfig(proxy, method, args);

            Object gestaltResult = null;

            if (result.isPresent()) {
                gestaltResult = result.get();
            } else {
                Pair<Boolean, ?> optionalInfo = ClassUtils.isOptionalAndDefault(returnType);
                if (optionalInfo.getFirst() && !config.isTreatMissingDiscretionaryValuesAsErrors()) {
                    gestaltResult = optionalInfo.getSecond();
                } else if (!config.isTreatMissingValuesAsErrors()) {
                    if (returnType.isPrimitive()) {
                        gestaltResult = ClassUtils.getDefaultValue(returnType);
                    } else {
                        //noinspection DataFlowIssue assigning null for ease of reading here
                        gestaltResult = null;
                    }
                } else {
                    throw new GestaltException("Failed to get pass through object from proxy config while calling method: " + methodName +
                        " with type: " + returnType + " in path: " + path);
                }
            }

            return gestaltResult;
        }

        protected Optional<Object> retrieveConfig(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            boolean isDefault = method.isDefault();

            Type genericType = method.getGenericReturnType();
            Class<?> returnType = method.getReturnType();

            String name;

            // if we have an annotation, use that for the path instead of the name.
            Config configAnnotation = method.getAnnotation(Config.class);
            if (configAnnotation != null && configAnnotation.path() != null && !configAnnotation.path().isEmpty()) {
                name = configAnnotation.path();
            } else {
                name = getConfigNameFromMethod(methodName, returnType);
            }

            String nextPath = PathUtil.pathForKey(decoderContext.getDefaultLexer(), path, name);

            Optional<Object> result = Optional.empty();
            if (decoderContext.getGestalt() != null) {
                result = decoderContext.getGestalt().getConfigOptional(nextPath, TypeCapture.of(genericType), tags);
            }

            if (result.isPresent()) {
                return result;
            } else {

                // if we have no value, check the config annotation for a default.
                if (configAnnotation != null && configAnnotation.defaultVal() != null &&
                    !configAnnotation.defaultVal().isEmpty()) {
                    GResultOf<?> defaultGResultOf = decoderContext.getDecoderService()
                        .decodeNode(nextPath, tags, new LeafNode(configAnnotation.defaultVal()), TypeCapture.of(returnType),
                            decoderContext);

                    if (defaultGResultOf.hasResults()) {
                        return Optional.of(defaultGResultOf.results());
                    }
                }

                if (isDefault) {
                    var defaultResult = MethodHandles.lookup()
                        .findSpecial(
                            method.getDeclaringClass(),
                            methodName,
                            MethodType.methodType(returnType, new Class[0]),
                            method.getDeclaringClass())
                        .bindTo(proxy)
                        .invokeWithArguments(args);

                    return Optional.of(defaultResult);
                }
            }
            return Optional.empty();
        }
    }


    static final class ProxyCacheInvocationHandler extends ProxyPassThroughInvocationHandler
        implements InvocationHandler, CoreReloadListener {

        private static final System.Logger logger = System.getLogger(ProxyCacheInvocationHandler.class.getName());
        private final Map<String, Object> methodResults;


        private ProxyCacheInvocationHandler(String path, Tags tags, DecoderContext decoderContext, GestaltConfig config,
                                            Map<String, Object> methodResults) {
            super(path, tags, decoderContext, config);
            this.methodResults = methodResults;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            Class<?> returnType = method.getReturnType();

            Object result = methodResults.get(methodName);
            if (result != null) {
                return result;
            } else {
                Optional<Object> resultOptional = retrieveConfig(proxy, method, args);

                Object gestaltResult = null;
                if (resultOptional.isPresent()) {
                    gestaltResult = resultOptional.get();
                } else {
                    Pair<Boolean, ?> optionalInfo = ClassUtils.isOptionalAndDefault(returnType);
                    if (optionalInfo.getFirst() && !config.isTreatMissingDiscretionaryValuesAsErrors()) {
                        gestaltResult = optionalInfo.getSecond();
                    } else if (!config.isTreatMissingValuesAsErrors()) {
                        if (returnType.isPrimitive()) {
                            gestaltResult = ClassUtils.getDefaultValue(returnType);
                        } else {
                            //noinspection DataFlowIssue assigning null for ease of reading here
                            gestaltResult = null;
                        }
                    } else {
                        throw new GestaltException("Failed to get cached object from proxy config while calling method: " + methodName +
                            " with type: " + returnType + " in path: " + path);
                    }
                }

                methodResults.put(methodName, gestaltResult);

                return gestaltResult;
            }
        }

        @Override
        public void reload() {
            logger.log(System.Logger.Level.DEBUG, "Reloading received on Proxy Cache Listener. Clearing Cache");
            methodResults.clear();
        }
    }
}


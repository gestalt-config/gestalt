package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.MapNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.utils.PathUtil;
import org.github.gestalt.config.utils.ValidateOf;

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
 */
public class ProxyDecoder implements Decoder<Object> {

    private String getConfigName(String methodName, Type returnType) {
        String name = methodName;
        if (methodName.startsWith("get")) {
            name = methodName.substring(3);
        } else if (methodName.startsWith("is") &&
            (returnType.equals(boolean.class) || returnType.equals(Boolean.TYPE))) {
            name = methodName.substring(2);
        }

        // since the first characters are likely upper case, lowercase only the first character.
        // The method is getCar and we remove get, we have Car.
        char[] nameArray = name.toCharArray();
        nameArray[0] = Character.toLowerCase(nameArray[0]);
        return new String(nameArray);
    }

    /**
     * must have a lower priority than the list and map decoders as they will also match a interface
     *
     * @return VERY_LOW
     */
    @Override
    public Priority priority() {
        return Priority.VERY_LOW;
    }

    @Override
    public String name() {
        return "proxy";
    }

    @Override
    public boolean matches(TypeCapture<?> klass) {
        return klass.isInterface() && !Collection.class.isAssignableFrom(klass.getRawType()) && !Map.class.isAssignableFrom(klass.getRawType());
    }

    @Override
    public ValidateOf<Object> decode(String path, ConfigNode node, TypeCapture<?> type, DecoderService decoderService) {
        if (!(node instanceof MapNode)) {
            return ValidateOf.inValid(new ValidationError.DecodingExpectedMapNodeType(path, node.getNodeType()));
        }

        Map<String, Object> methodResults = new HashMap<>();

        Class<?> klass = type.getRawType();

        List<ValidationError> errors = new ArrayList<>();

        Method[] classMethods = klass.getMethods();

        // for each method, we want to get the corresponding bean value. ie if it is getCar, the bean value would be car.
        // Then get the configuration for the bean value and decode it.
        // Save it into a cache for use with the proxy.
        for (Method method : classMethods) {
            String methodName = method.getName();
            Type returnType = method.getReturnType();

            String name = getConfigName(methodName, returnType);

            String nextPath = PathUtil.pathForKey(path, name);

            ValidateOf<ConfigNode> configNode = decoderService.getNextNode(nextPath, name, node);

            errors.addAll(configNode.getErrors(ValidationLevel.WARN));
            if (configNode.hasErrors(ValidationLevel.ERROR)) {
                errors.addAll(configNode.getErrors(ValidationLevel.ERROR));
            } else if (!configNode.hasResults()) {
                errors.add(new ValidationError.NoResultsFoundForNode(nextPath, returnType.getTypeName(), "proxy decoding"));
            } else {
                ValidateOf<?> fieldValidateOf = decoderService.decodeNode(nextPath, configNode.results(),
                    TypeCapture.of(returnType));
                if (fieldValidateOf.hasErrors()) {
                    errors.addAll(fieldValidateOf.getErrors());
                }

                if (!fieldValidateOf.hasResults()) {
                    errors.add(new ValidationError.NoResultsFoundForNode(nextPath, returnType.getTypeName(), "proxy decoding"));
                } else {
                    methodResults.put(methodName, fieldValidateOf.results());
                }
            }
        }

        InvocationHandler proxyHandler = new ProxyInvocationHandler(path, methodResults);
        Object myProxy = Proxy.newProxyInstance(type.getRawType().getClassLoader(),
            new Class<?>[]{type.getRawType()},
            proxyHandler);
        return ValidateOf.validateOf(myProxy, errors);
    }

    private static class ProxyInvocationHandler implements InvocationHandler {
        private final String path;
        private final Map<String, Object> methodResults;

        private ProxyInvocationHandler(String path, Map<String, Object> methodResults) {
            this.path = path;
            this.methodResults = methodResults;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable, GestaltException {
            String methodName = method.getName();
            boolean isDefault = method.isDefault();
            Class<?> type = method.getReturnType();

            Object result = methodResults.get(methodName);

            if (result != null) {
                return result;
            } else if (isDefault) {
                return MethodHandles.lookup()
                             .findSpecial(
                                 method.getDeclaringClass(),
                                 methodName,
                                 MethodType.methodType(type, new Class[0]),
                                 method.getDeclaringClass())
                             .bindTo(proxy)
                             .invokeWithArguments(args);
            } else {
                throw new GestaltException("Failed to get proxy config while calling method: " + methodName +
                    " in path: " + path + ".");
            }
        }
    }
}

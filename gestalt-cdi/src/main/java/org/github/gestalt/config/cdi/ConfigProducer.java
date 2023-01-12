/*
 * Copyright 2017 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.github.gestalt.config.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import org.github.gestalt.config.Gestalt;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Supplier;

/**
 * CDI producer for {@link Gestalt} bean.
 *
 * @author <a href="http://jmesnil.net/">Jeff Mesnil</a> (c) 2017 Red Hat inc.
 */
@ApplicationScoped
public class ConfigProducer {
    public static boolean isClassHandledByConfigProducer(Type requiredType) {
        return requiredType == String.class
            || requiredType == Boolean.class
            || requiredType == Boolean.TYPE
            || requiredType == Integer.class
            || requiredType == Integer.TYPE
            || requiredType == Long.class
            || requiredType == Long.TYPE
            || requiredType == Float.class
            || requiredType == Float.TYPE
            || requiredType == Double.class
            || requiredType == Double.TYPE
            || requiredType == Short.class
            || requiredType == Short.TYPE
            || requiredType == Byte.class
            || requiredType == Byte.TYPE
            || requiredType == Character.class
            || requiredType == Character.TYPE;
    }

    @Produces
    protected Gestalt getConfig() {
        return ConfigProvider.getGestaltConfig();
    }

    @Dependent
    @Produces
    @GestaltConfig
    protected String produceStringConfigProperty(InjectionPoint ip) {
        return ConfigProducerUtil.getValue(ip, getConfig());
    }

    @Dependent
    @Produces
    @GestaltConfig
    protected Long getLongValue(InjectionPoint ip) {
        return ConfigProducerUtil.getValue(ip, getConfig());
    }

    @Dependent
    @Produces
    @GestaltConfig
    protected Integer getIntegerValue(InjectionPoint ip) {
        return ConfigProducerUtil.getValue(ip, getConfig());
    }

    @Dependent
    @Produces
    @GestaltConfig
    protected Float produceFloatConfigProperty(InjectionPoint ip) {
        return ConfigProducerUtil.getValue(ip, getConfig());
    }

    @Dependent
    @Produces
    @GestaltConfig
    protected Double produceDoubleConfigProperty(InjectionPoint ip) {
        return ConfigProducerUtil.getValue(ip, getConfig());
    }

    @Dependent
    @Produces
    @GestaltConfig
    protected Boolean produceBooleanConfigProperty(InjectionPoint ip) {
        return ConfigProducerUtil.getValue(ip, getConfig());
    }

    @Dependent
    @Produces
    @GestaltConfig
    protected Short produceShortConfigProperty(InjectionPoint ip) {
        return ConfigProducerUtil.getValue(ip, getConfig());
    }

    @Dependent
    @Produces
    @GestaltConfig
    protected Byte produceByteConfigProperty(InjectionPoint ip) {
        return ConfigProducerUtil.getValue(ip, getConfig());
    }

    @Dependent
    @Produces
    @GestaltConfig
    protected Character produceCharacterConfigProperty(InjectionPoint ip) {
        return ConfigProducerUtil.getValue(ip, getConfig());
    }

    @Dependent
    @Produces
    @GestaltConfig
    protected <T> Optional<T> produceOptionalConfigProperty(InjectionPoint ip) {
        return ConfigProducerUtil.getOptionalValue(ip, getConfig());
    }

    @Dependent
    @Produces
    @GestaltConfig
    protected <T> Supplier<T> produceSupplierConfigProperty(InjectionPoint ip) {
        return () -> ConfigProducerUtil.getSupplierValue(ip, getConfig());
    }

    @Dependent
    @Produces
    @GestaltConfig
    protected <T> Set<T> producesSetConfigProperty(InjectionPoint ip) {
        return ConfigProducerUtil.getValue(ip, getConfig());
    }

    @Dependent
    @Produces
    @GestaltConfig
    protected <T> List<T> producesListConfigProperty(InjectionPoint ip) {
        return ConfigProducerUtil.getValue(ip, getConfig());
    }

    @Dependent
    @Produces
    @GestaltConfig
    protected <K, V> Map<K, V> producesMapConfigProperty(InjectionPoint ip) {
        return ConfigProducerUtil.getValue(ip, getConfig());
    }
}

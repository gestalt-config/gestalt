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

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.*;
import jakarta.enterprise.inject.spi.configurator.AnnotatedTypeConfigurator;
import jakarta.enterprise.util.Nonbinding;
import jakarta.inject.Provider;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import static org.github.gestalt.config.cdi.GestaltConfigProducer.isClassHandledByConfigProducer;

/**
 * CDI Extension to produces Config bean.
 *
 * <p>Based on <a href="https://github.com/smallrye/smallrye-config/tree/3.1.1/cdi">...</a>
 *
 * @author <a href="http://jmesnil.net/">Jeff Mesnil</a> (c) 2017 Red Hat inc.
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public class GestaltConfigExtension implements Extension {
    private final Set<InjectionPoint> configPropertyInjectionPoints = new HashSet<>();

    /**
     * ConfigProperties for SmallRye Config.
     */
    private final Set<ConfigClassWithPrefix> configProperties = new HashSet<>();

    /**
     * ConfigProperties for CDI.
     */
    private final Set<ConfigClassWithPrefix> configPropertiesBeans = new HashSet<>();


    protected void beforeBeanDiscovery(@Observes BeforeBeanDiscovery bbd, BeanManager bm) {
        AnnotatedType<GestaltConfigProducer> configBean = bm.createAnnotatedType(GestaltConfigProducer.class);
        bbd.addAnnotatedType(configBean, GestaltConfigProducer.class.getName());

        // Remove NonBinding annotation. OWB is not able to look up CDI beans programmatically with NonBinding in the
        // case the look-up changed the non-binding parameters (in this case the prefix)
        AnnotatedTypeConfigurator<InjectConfigs> configPropertiesConfigurator = bbd
            .configureQualifier(InjectConfigs.class);
        configPropertiesConfigurator.methods().forEach(methodConfigurator -> methodConfigurator
            .remove(annotation -> annotation.annotationType().equals(Nonbinding.class)));
    }

    protected void processConfigProperties(
        @Observes @WithAnnotations(InjectConfigs.class) ProcessAnnotatedType<?> processAnnotatedType) {
        // Even if we filter in the CDI event, beans containing injection points of ConfigProperties are also fired.
        if (processAnnotatedType.getAnnotatedType().isAnnotationPresent(InjectConfigs.class)) {
            // We are going to veto, because it may be a managed bean, and we will use a configurator bean
            processAnnotatedType.veto();

            // Each config class is both in SmallRyeConfig and managed by a configurator bean.
            // CDI requires more beans for injection points due to binding prefix.
            ConfigClassWithPrefix properties = configClassWithPrefix(processAnnotatedType.getAnnotatedType().getJavaClass(),
                processAnnotatedType.getAnnotatedType().getAnnotation(InjectConfigs.class).prefix());
            // Unconfigured is represented as an empty String in SmallRye Config
            if (!properties.getPrefix().isEmpty()) {
                configProperties.add(properties);
            } else {
                configProperties.add(ConfigClassWithPrefix.configClassWithPrefix(properties.getKlass(), ""));
            }
            configPropertiesBeans.add(properties);
        }
    }

    protected void processConfigInjectionPoints(@Observes ProcessInjectionPoint<?, ?> pip) {
        var annotated = pip.getInjectionPoint().getAnnotated();
        if (annotated.isAnnotationPresent(InjectConfig.class)) {
            configPropertyInjectionPoints.add(pip.getInjectionPoint());
        }

        if (annotated.isAnnotationPresent(InjectConfigs.class)) {
            ConfigClassWithPrefix properties = configClassWithPrefix((Class<?>) pip.getInjectionPoint().getType(),
                pip.getInjectionPoint().getAnnotated().getAnnotation(InjectConfigs.class).prefix());

            // If the prefix is empty at the injection point, fallbacks to the class prefix (already registered)
            if (!properties.getPrefix().isEmpty()) {
                configProperties.add(properties);
            }
            // Cover all combinations of the configurator bean for ConfigProperties because prefix is binding
            configPropertiesBeans.add(properties);
        }
    }

    protected void registerCustomBeans(@Observes AfterBeanDiscovery abd, BeanManager bm) {
        Set<Class<?>> customTypes = new HashSet<>();
        for (InjectionPoint ip : configPropertyInjectionPoints) {
            Type requiredType = ip.getType();
            if (requiredType instanceof ParameterizedType) {
                ParameterizedType type = (ParameterizedType) requiredType;
                // TODO We should probably handle all parameterized types correctly
                if (type.getRawType().equals(Provider.class) || type.getRawType().equals(Instance.class)) {
                    // These injection points are satisfied by the built-in Instance bean
                    Type typeArgument = type.getActualTypeArguments()[0];
                    if (typeArgument instanceof Class && !isClassHandledByConfigProducer(typeArgument)) {
                        customTypes.add((Class<?>) typeArgument);
                    }
                }
            } else if (requiredType instanceof Class && !isClassHandledByConfigProducer(requiredType)) {
                // type is not produced by ConfigProducer
                customTypes.add((Class<?>) requiredType);
            }
        }

        customTypes.forEach(customType -> abd.addBean(new GestaltConfigInjectionBean<>(bm, customType)));
        configPropertiesBeans.forEach(properties -> abd.addBean(new GestaltConfigsInjectionBean<>(properties)));
    }

    protected void result(@Observes AfterDeploymentValidation adv) {

    }

    protected Set<InjectionPoint> getConfigPropertyInjectionPoints() {
        return configPropertyInjectionPoints;
    }

    public ConfigClassWithPrefix configClassWithPrefix(Class<?> klass, String prefix) {
        return new ConfigClassWithPrefix(klass, prefix);
    }
}

/*
 * Copyright 2017 Inscope Metrics Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arpnetworking.commons.jackson.databind.deser;

import com.arpnetworking.commons.builder.ThreadLocalBuilder;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBuilder;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.impl.BeanPropertyMap;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdValueProperty;

import java.util.Collection;

/**
 * Bean deserializer builder for Jackson which leverages
 * {@link ThreadLocalBuilder} instances.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
public final class ThreadLocalBuilderBeanDeserializerBuilder extends BeanDeserializerBuilder {

    private final Class<ThreadLocalBuilder<?>> _threadLocalBuilderClass;

    /**
     * Public constructor.
     *
     * @param threadLocalBuilderClass The thread local builder class.
     * @param wrappedBeanDeserializerBuilder The instance of {@code BeanDeserializerBuilder} to wrap.
     */
    public ThreadLocalBuilderBeanDeserializerBuilder(
            final Class<ThreadLocalBuilder<?>> threadLocalBuilderClass,
            final BeanDeserializerBuilder wrappedBeanDeserializerBuilder) {
        super(wrappedBeanDeserializerBuilder);
        _threadLocalBuilderClass = threadLocalBuilderClass;
    }

    /**
     * Copied from {@link com.fasterxml.jackson.databind.deser.BeanDeserializerBuilder}
     * and modified to create {@link ThreadLocalBuilderBasedDeserializer} instead.
     *
     * Instead the {@link com.fasterxml.jackson.databind.deser.BeanDeserializerBuilder}
     * implementation should be be extensible to allow a different compatible builder
     * deserializer to be instantiated instead of reproducing this entire method.
     *
     * See:
     * https://github.com/FasterXML/jackson-databind/issues/2487
     */
    @Override
    public JsonDeserializer<?> buildBuilderBased(
            final JavaType valueType,
            final String expBuildMethodName)
            throws JsonMappingException {
        // First: validation; must have build method that returns compatible type
        if (_buildMethod == null) {
            // as per [databind#777], allow empty name
            if (!expBuildMethodName.isEmpty()) {
                _context.reportBadDefinition(_beanDesc.getType(),
                        String.format("Builder class %s does not have build method (name: '%s')",
                                _beanDesc.getBeanClass().getName(),
                                expBuildMethodName));
            }
        } else {
            // also: type of the method must be compatible
            final Class<?> rawBuildType = _buildMethod.getRawReturnType();
            final Class<?> rawValueType = valueType.getRawClass();
            if (rawBuildType != rawValueType
                    && !rawBuildType.isAssignableFrom(rawValueType)
                    && !rawValueType.isAssignableFrom(rawBuildType)) {
                _context.reportBadDefinition(_beanDesc.getType(),
                        String.format("Build method '%s' has wrong return type (%s), not compatible with POJO type (%s)",
                                _buildMethod.getFullName(),
                                rawBuildType.getName(),
                                valueType.getRawClass().getName()));
            }
        }
        // And if so, we can try building the deserializer
        final Collection<SettableBeanProperty> props = _properties.values();
        _fixAccess(props);
        BeanPropertyMap propertyMap = BeanPropertyMap.construct(
                _config,
                props,
                _collectAliases(props),
                _config.isEnabled(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES));
        propertyMap.assignIndexes();

        boolean anyViews = !_config.isEnabled(MapperFeature.DEFAULT_VIEW_INCLUSION);

        if (!anyViews) {
            for (SettableBeanProperty prop : props) {
                if (prop.hasViews()) {
                    anyViews = true;
                    break;
                }
            }
        }

        if (_objectIdReader != null) {
            // May or may not have annotations for id property; but no easy access.
            // But hard to see id property being optional, so let's consider required at this point.
            final ObjectIdValueProperty prop = new ObjectIdValueProperty(_objectIdReader,
                    PropertyMetadata.STD_REQUIRED);
            propertyMap = propertyMap.withProperty(prop);
        }

        return new ThreadLocalBuilderBasedDeserializer(
                _threadLocalBuilderClass,
                this,
                _beanDesc,
                valueType,
                propertyMap,
                _backRefProperties,
                _ignorableProps,
                _ignoreAllUnknown,
                anyViews);
    }
}

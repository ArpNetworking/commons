/**
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
import com.fasterxml.jackson.databind.deser.BeanDeserializerBuilder;
import com.fasterxml.jackson.databind.deser.BuilderBasedDeserializer;
import com.fasterxml.jackson.databind.deser.ThreadLocalBuilderBasedDeserializer;

/**
 * Bean deserializer builder for Jackson which leverages
 * {@link ThreadLocalBuilder} instances.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
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

    @Override
    public JsonDeserializer<?> buildBuilderBased(
            final JavaType valueType,
            final String expBuildMethodName)
            throws JsonMappingException {
        final BuilderBasedDeserializer underlyingBuilderBasedDeserializer =
                (BuilderBasedDeserializer) super.buildBuilderBased(valueType, expBuildMethodName);

        return new ThreadLocalBuilderBasedDeserializer(
                _threadLocalBuilderClass,
                underlyingBuilderBasedDeserializer);
    }
}

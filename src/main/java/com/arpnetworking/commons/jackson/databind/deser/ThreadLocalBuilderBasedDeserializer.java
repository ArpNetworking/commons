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
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBuilder;
import com.fasterxml.jackson.databind.deser.BuilderBasedDeserializer;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.impl.BeanPropertyMap;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Supports {@link ThreadLocalBuilder} use during deserialization.
 *
 * Consumers of Arpnetworking Commons should _not_ depend directly on this class.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
public class ThreadLocalBuilderBasedDeserializer extends BuilderBasedDeserializer {

    private static final long serialVersionUID = 576412638264270116L;

    private final Class<ThreadLocalBuilder<?>> _threadLocalBuilderClass;

    // CHECKSTYLE.OFF: ParameterNumber - Constructor based on BuilderBasedDeserializer
    /**
     * Public constructor.
     *
     * @param threadLocalBuilderClass The {@link ThreadLocalBuilder} subclass.
     * @param builder only passed to {@link BuilderBasedDeserializer}
     * @param beanDesc only passed to {@link BuilderBasedDeserializer}
     * @param valueType only passed to {@link BuilderBasedDeserializer}
     * @param propertyMap only passed to {@link BuilderBasedDeserializer}
     * @param backRefProperties only passed to {@link BuilderBasedDeserializer}
     * @param ignorableProps only passed to {@link BuilderBasedDeserializer}
     * @param ignoreAllUnknown only passed to {@link BuilderBasedDeserializer}
     * @param anyViews only passed to {@link BuilderBasedDeserializer}
     */
    public ThreadLocalBuilderBasedDeserializer(
            final Class<ThreadLocalBuilder<?>> threadLocalBuilderClass,
            final BeanDeserializerBuilder builder,
            final BeanDescription beanDesc,
            final JavaType valueType,
            final BeanPropertyMap propertyMap,
            final HashMap<String, SettableBeanProperty> backRefProperties,
            final HashSet<String> ignorableProps,
            final boolean ignoreAllUnknown,
            final boolean anyViews) {
        super(builder,
                beanDesc,
                valueType,
                propertyMap,
                backRefProperties,
                ignorableProps,
                ignoreAllUnknown,
                anyViews);
        _threadLocalBuilderClass = threadLocalBuilderClass;
    }
    // CHECKSTYLE.ON: ParameterNumber

    /**
     * General structure copied from {@link com.fasterxml.jackson.databind.deser.BuilderBasedDeserializer}
     * and modified to redirect vanilla processing case to custom {@link ThreadLocalBuilder}
     * code path.
     */
    @Override
    public final Object deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
        if (p.isExpectedStartObjectToken()) {
            if (isVanillaFromObject()) {
                p.nextToken();
                return vanillaDeserializeAndFinishBuild(p, ctxt);
            }
        }
        return super.deserialize(p, ctxt);
    }

    /**
     * General structure copied from {@link com.fasterxml.jackson.databind.deser.BuilderBasedDeserializer}
     * and modified to use {@link ThreadLocalBuilder} instantiation and build.
     */
    private Object vanillaDeserializeAndFinishBuild(
            final JsonParser p,
            final DeserializationContext ctxt)
            throws IOException {

        try {
            return ThreadLocalBuilder.buildGeneric(
                    _threadLocalBuilderClass,
                    b -> {
                        Object bean = b;
                        try {
                            while (p.getCurrentToken() != JsonToken.END_OBJECT) {
                                final String propName = p.currentName();
                                // Skip field name:
                                p.nextToken();
                                final SettableBeanProperty prop = _beanProperties.find(propName);
                                if (prop != null) { // normal case
                                    try {
                                        bean = prop.deserializeSetAndReturn(p, ctxt, bean);
                                        // CHECKSTYLE.OFF: IllegalCatch - Retain existing behavior
                                    } catch (final IOException | RuntimeException e) {
                                        // CHECKSTYLE.ON: IllegalCatch
                                        // TODO(ville): Convert to throwing wrapAndThrow result. This improves coverage.
                                        // See: https://github.com/FasterXML/jackson-databind/pull/1871
                                        // throw wrapAndThrow(e, bean, propName, ctxt);
                                        wrapAndThrow(e, bean, propName, ctxt);
                                    }
                                } else {
                                    handleUnknownVanilla(p, ctxt, bean, propName);
                                }
                                p.nextToken();
                            }
                        } catch (final IOException e) {
                            throw new WrappedIOExceptionException(e);
                        }
                    });
        } catch (final WrappedIOExceptionException e) {
            throw e.getCause();
            // CHECKSTYLE.OFF: IllegalCatch - Match behavior in BuilderBasedDeserializer
        } catch (final Exception e) {
            // CHECKSTYLE.ON: IllegalCatch
            return wrapInstantiationProblem(e, ctxt);
        }
    }

    private boolean isVanillaFromObject() {
        // Do not consider whether there is a value instantiator from an array
        // by eliminating the call to canCreateUsingArrayDelegate from the
        // definition of nonStandardCreation.
        //
        // Otherwise the deserializability for a primitive delegate and an
        // array delegate differ in that the pojo with the primitive delegate
        // can also be deserialized from an object while the pojo with the
        // array delegate cannot.
        //
        // See:
        // https://github.com/FasterXML/jackson-databind/issues/2486

        final boolean nonStandardCreation = _unwrappedPropertyHandler != null
                || _valueInstantiator.canCreateUsingDelegate()
                || _valueInstantiator.canCreateFromObjectWith()
                || !_valueInstantiator.canCreateUsingDefault();

        return !nonStandardCreation
                && _injectables == null
                && !_needViewProcesing
                // also, may need to reorder stuff if we expect Object Id:
                && _objectIdReader == null;
    }

    @SuppressFBWarnings("BC_UNCONFIRMED_CAST_OF_RETURN_VALUE")
    private static class WrappedIOExceptionException extends RuntimeException {

        private static final long serialVersionUID = -5669726981322339644L;

        WrappedIOExceptionException(final IOException cause) {
            super(cause);
        }

        @Override
        public IOException getCause() {
            return (IOException) super.getCause();
        }
    }
}

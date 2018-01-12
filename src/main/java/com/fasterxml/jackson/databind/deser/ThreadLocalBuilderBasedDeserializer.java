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
package com.fasterxml.jackson.databind.deser;

import com.arpnetworking.commons.builder.ThreadLocalBuilder;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.util.NameTransformer;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.util.Set;

/**
 * Supports {@link ThreadLocalBuilder} use during deserialization.
 *
 * IMPORTANT: This class should be in the {@code com.arpnetworking.commons.jackson.databind.deser}
 * package; however, because we cannot extend {@link BuilderBasedDeserializer}
 * due to {@code final} and {@code private} methods we must use delegation and
 * in order to delegate {@code protected} methods must be in the same package.
 *
 * See:
 * https://github.com/FasterXML/jackson-databind/issues/1869
 *
 * Consumers of Arpnetworking Commons should _not_ depend directly on this class.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 */
public class ThreadLocalBuilderBasedDeserializer extends BeanDeserializerBase {

    private static final long serialVersionUID = 576412638264270116L;

    private final Class<ThreadLocalBuilder<?>> _threadLocalBuilderClass;
    private final BuilderBasedDeserializer _underlyingBuilderBasedDeserializer;

    /**
     * Public constructor.
     *
     * @param threadLocalBuilderClass The {@link ThreadLocalBuilder} subclass.
     * @param underlyingBuilderBasedDeserializer The Jackson created {@code BuilderBasedDeserializer}.
     */
    public ThreadLocalBuilderBasedDeserializer(
            final Class<ThreadLocalBuilder<?>> threadLocalBuilderClass,
            final BuilderBasedDeserializer underlyingBuilderBasedDeserializer) {
        super(underlyingBuilderBasedDeserializer);
        _threadLocalBuilderClass = threadLocalBuilderClass;
        _underlyingBuilderBasedDeserializer = underlyingBuilderBasedDeserializer;
    }

    /**
     * Protected constructor.
     *
     * @param threadLocalBuilderClass The {@link ThreadLocalBuilder} subclass.
     * @param underlyingBuilderBasedDeserializer The Jackson created {@code BuilderBasedDeserializer}.
     * @param unwrapper The {@code NameTransformer} to apply.
     */
    protected ThreadLocalBuilderBasedDeserializer(
            final Class<ThreadLocalBuilder<?>> threadLocalBuilderClass,
            final BuilderBasedDeserializer underlyingBuilderBasedDeserializer,
            final NameTransformer unwrapper) {
        super(underlyingBuilderBasedDeserializer, unwrapper);
        _threadLocalBuilderClass = threadLocalBuilderClass;
        _underlyingBuilderBasedDeserializer = underlyingBuilderBasedDeserializer;
    }

    /**
     * Protected constructor.
     *
     * @param threadLocalBuilderClass The {@link ThreadLocalBuilder} subclass.
     * @param underlyingBuilderBasedDeserializer The Jackson created {@code BuilderBasedDeserializer}.
     * @param oir The {@code ObjectIdReader} to apply.
     */
    protected ThreadLocalBuilderBasedDeserializer(
            final Class<ThreadLocalBuilder<?>> threadLocalBuilderClass,
            final BuilderBasedDeserializer underlyingBuilderBasedDeserializer,
            final ObjectIdReader oir) {
        super(underlyingBuilderBasedDeserializer, oir);
        _threadLocalBuilderClass = threadLocalBuilderClass;
        _underlyingBuilderBasedDeserializer = underlyingBuilderBasedDeserializer;
    }

    /**
     * Protected constructor.
     *
     * @param threadLocalBuilderClass The {@link ThreadLocalBuilder} subclass.
     * @param underlyingBuilderBasedDeserializer The Jackson created {@code BuilderBasedDeserializer}.
     * @param ignorableProps The {@code Set} of properties to ignore.
     */
    public ThreadLocalBuilderBasedDeserializer(
            final Class<ThreadLocalBuilder<?>> threadLocalBuilderClass,
            final BuilderBasedDeserializer underlyingBuilderBasedDeserializer,
            final Set<String> ignorableProps) {
        super(underlyingBuilderBasedDeserializer, ignorableProps);
        _threadLocalBuilderClass = threadLocalBuilderClass;
        _underlyingBuilderBasedDeserializer = underlyingBuilderBasedDeserializer;
    }

    @Override
    public Boolean supportsUpdate(final DeserializationConfig config) {
        return _underlyingBuilderBasedDeserializer.supportsUpdate(config);
    }

    @Override
    public JsonDeserializer<Object> unwrappingDeserializer(final NameTransformer unwrapper) {
        return new ThreadLocalBuilderBasedDeserializer(
                _threadLocalBuilderClass,
                (BuilderBasedDeserializer) _underlyingBuilderBasedDeserializer.unwrappingDeserializer(unwrapper),
                unwrapper);
    }

    @Override
    public BeanDeserializerBase withObjectIdReader(final ObjectIdReader oir) {
        return new ThreadLocalBuilderBasedDeserializer(
                _threadLocalBuilderClass,
                (BuilderBasedDeserializer) _underlyingBuilderBasedDeserializer.withObjectIdReader(oir),
                oir);
    }

    @Override
    public BeanDeserializerBase withIgnorableProperties(final Set<String> ignorableProps) {
        return new ThreadLocalBuilderBasedDeserializer(
                _threadLocalBuilderClass,
                (BuilderBasedDeserializer) _underlyingBuilderBasedDeserializer.withIgnorableProperties(ignorableProps),
                ignorableProps);
    }

    @Override
    protected BeanDeserializerBase asArrayDeserializer() {
        return _underlyingBuilderBasedDeserializer.asArrayDeserializer();
    }

    @Override
    protected Object _deserializeUsingPropertyBased(
            final JsonParser p,
            final DeserializationContext ctxt)
            throws IOException {
        return _underlyingBuilderBasedDeserializer._deserializeUsingPropertyBased(p, ctxt);
    }

    @Override
    public Object deserializeFromObject(final JsonParser p, final DeserializationContext ctxt) throws IOException {
        return _underlyingBuilderBasedDeserializer.deserializeFromObject(p, ctxt);
    }

    @Override
    public Object deserialize(
            final JsonParser p,
            final DeserializationContext ctxt,
            final Object value) throws IOException {
        return _underlyingBuilderBasedDeserializer.deserialize(p, ctxt, value);
    }

    @Override
    public final Object deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
        if (p.isExpectedStartObjectToken()) {
            final JsonToken t = p.nextToken();
            if (_vanillaProcessing) {
                return vanillaDeserializeAndFinishBuild(p, ctxt, t);
            }
        }
        return _underlyingBuilderBasedDeserializer.deserialize(p, ctxt);
    }

    /**
     * Copied from {@code com.fasterxml.jackson.databind.deser.BuilderBasedDeserializer}
     * and modified to use {@link ThreadLocalBuilder} instantiation and build.
     */
    private Object vanillaDeserializeAndFinishBuild(
            final JsonParser p,
            final DeserializationContext ctxt,
            final JsonToken t)
            throws IOException {

        try {
            return ThreadLocalBuilder.buildGeneric(
                    _threadLocalBuilderClass,
                    b -> {
                        Object bean = b;
                        try {
                            while (p.getCurrentToken() != JsonToken.END_OBJECT) {
                                final String propName = p.getCurrentName();
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
            return _underlyingBuilderBasedDeserializer.wrapInstantiationProblem(e, ctxt);
        }
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

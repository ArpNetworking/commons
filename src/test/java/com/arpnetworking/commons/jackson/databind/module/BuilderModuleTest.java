/*
 * Copyright 2016 Groupon.com
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
package com.arpnetworking.commons.jackson.databind.module;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Objects;

/**
 * Test for the BuilderModule class.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
public class BuilderModuleTest {

    private AutoCloseable _mockCloser;

    @Before
    public void setUp() {
        _mockCloser = MockitoAnnotations.openMocks(this);
    }

    @After
    public void close() {
        try {
            _mockCloser.close();
            // CHECKSTYLE.OFF: IllegalCatch - Required for testing
        } catch (final Exception e) {
            // CHECKSTYLE.ON: IllegalCatch
            // Expected exception
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test() {
        final BuilderModule module = new BuilderModule();
        module.setupModule(_context);
        Mockito.verify(_context).insertAnnotationIntrospector(Mockito.any(AnnotationIntrospectorPair.class));
    }

    @Test
    public void testSimpleRoundTrip() throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new BuilderModule());

        final PojoWithBuilder pojo = new PojoWithBuilder.Builder()
                .setInt(1)
                .build();
        final String pojoAsJson = objectMapper.writeValueAsString(pojo);
        final PojoWithBuilder actualPojo = objectMapper.readValue(pojoAsJson, PojoWithBuilder.class);

        Assert.assertEquals(pojo, actualPojo);
    }

    @Test
    @Ignore
    public void testGenericRoundTrip() throws IOException {
        // TODO(ville): Enable this test.
        //
        // Depends on release and upgrade to a version of Jackson that supports
        // basic generic builder deserialization.
        //
        // See:
        // https://github.com/FasterXML/jackson-databind/issues/921
        // https://github.com/FasterXML/jackson-databind/pull/1796
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new BuilderModule());

        final GenericPojoWithBuilder<PojoWithBuilder> pojo = new GenericPojoWithBuilder.Builder<PojoWithBuilder>()
                .setVal(
                        new PojoWithBuilder.Builder()
                                .setInt(1)
                                .build())
                .build();
        final String pojoAsJson = objectMapper.writeValueAsString(pojo);
        final GenericPojoWithBuilder<PojoWithBuilder> actualPojo = objectMapper.readValue(
                pojoAsJson,
                TYPE_REFERENCE_GENERIC_POJO_WITH_BUILDER);
        Assert.assertEquals(pojo, actualPojo);
    }

    @Mock
    private Module.SetupContext _context;

    private static final TypeReference<GenericPojoWithBuilder<PojoWithBuilder>> TYPE_REFERENCE_GENERIC_POJO_WITH_BUILDER = 
            new TypeReference<GenericPojoWithBuilder<PojoWithBuilder>>(){};

    /**
     * Test class.
     */
    public static class PojoWithBuilder {

        protected PojoWithBuilder(final Builder builder) {
            _int = builder._int;
        }

        public int getInt() {
            return _int;
        }

        @Override
        public boolean equals(final Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof PojoWithBuilder)) {
                return false;
            }
            final PojoWithBuilder otherPojo = (PojoWithBuilder) other;
            return otherPojo._int == this._int;
        }

        @Override
        public int hashCode() {
            return _int;
        }

        private final int _int;

        /**
         * Builder for PojoWithBuilder.
         */
        public static class Builder implements com.arpnetworking.commons.builder.Builder<PojoWithBuilder> {

            public Builder setInt(final int value) {
                _int = value;
                return this;
            }

            @Override
            public PojoWithBuilder build() {
                return new PojoWithBuilder(this);
            }

            private Integer _int;
        }
    }

    /**
     * Parameterized test class.
     *
     * @param <T> The value type.
     */
    public static class GenericPojoWithBuilder<T> {

        protected GenericPojoWithBuilder(final Builder<T> builder) {
            _val = builder._val;
        }

        public T getVal() {
            return _val;
        }

        @Override
        public boolean equals(final Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof GenericPojoWithBuilder)) {
                return false;
            }
            @SuppressWarnings("unchecked")
            final GenericPojoWithBuilder<T> otherPojo = (GenericPojoWithBuilder<T>) other;
            return Objects.equals(otherPojo._val, this._val);
        }

        @Override
        public int hashCode() {
            return _val == null ? 0 : _val.hashCode();
        }

        private final T _val;

        /**
         * Builder for GenericPojoWithBuilder.
         *
         * @param <T> The value type.
         */
        public static class Builder<T>
                implements com.arpnetworking.commons.builder.Builder<GenericPojoWithBuilder<T>> {

            public Builder<T> setVal(final T value) {
                _val = value;
                return this;
            }

            @Override
            public GenericPojoWithBuilder<T> build() {
                return new GenericPojoWithBuilder<T>(this);
            }

            private T _val;
        }
    }
}

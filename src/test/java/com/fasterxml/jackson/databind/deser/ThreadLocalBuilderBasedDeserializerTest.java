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
import com.arpnetworking.commons.jackson.databind.ObjectMapperFactory;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import net.sf.oval.constraint.NotNull;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

/**
 * Tests for the ThreadLocalBuilderBasedDeserializer class.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 */
public final class ThreadLocalBuilderBasedDeserializerTest {

    @Test
    public void testSupportsUpdate() {
        @SuppressWarnings("unchecked")
        final Class<ThreadLocalBuilder<?>> threadLocalBuilder = (Class) TestBeanWithCreator.Builder.class;
        Assert.assertFalse(
                new ThreadLocalBuilderBasedDeserializer(threadLocalBuilder, Mockito.mock(BuilderBasedDeserializer.class))
                        .supportsUpdate(OBJECT_MAPPER.getDeserializationConfig()));
    }

    @Test
    public void testNotObjectDeserializationFromObject() throws IOException {
        final TestBeanWithCreator bean = OBJECT_MAPPER.readValue("{\"i\":123}", TestBeanWithCreator.class);
        Assert.assertEquals(123, bean.getI());
    }

    @Test
    public void testNotObjectDeserializationFromValue() throws IOException {
        final TestBeanWithCreator bean = OBJECT_MAPPER.readValue("123", TestBeanWithCreator.class);
        Assert.assertEquals(123, bean.getI());
    }

    @Test(expected = IOException.class)
    public void testNotObjectDeserializationFailure() throws IOException {
        OBJECT_MAPPER.readValue("[]", TestBeanWithCreator.class);
    }

    @Test
    public void testPropertyDeserializationFailure() throws IOException {
        try {
            OBJECT_MAPPER.readValue("{\"i\":\"foo\"}", TestBean.class);
            Assert.fail("Expected exception not thrown");
        } catch (final IOException e) {
            Assert.assertTrue(e instanceof InvalidFormatException);
            Assert.assertEquals(
                    "Cannot deserialize value of type `java.lang.Integer` from String \"foo\": not a valid Integer "
                    + "value\n at [Source: (String)\"{\"i\":\"foo\"}\"; line: 1, column: 6] (through reference chain: "
                    + "com.fasterxml.jackson.databind.deser.ThreadLocalBuilderBasedDeserializerTest$TestBean$Builder["
                    + "\"i\"])",
                    e.getMessage());
        }
    }

    @Test
    public void testUnknownPropertyIgnored() throws IOException {
        final TestBean bean = OBJECT_MAPPER.readValue("{\"i\":3,\"j\":\"foo\"}", TestBean.class);
        Assert.assertEquals(3, bean.getI());
    }

    @Test
    public void testUnknownPropertyFailure() throws IOException {
        try {
            ObjectMapperFactory.createInstance()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
                    .readValue("{\"i\":3,\"j\":\"foo\"}", TestBean.class);
            Assert.fail("Expected exception not thrown");
        } catch (final IOException e) {
            Assert.assertTrue(e instanceof UnrecognizedPropertyException);
            Assert.assertEquals(
                    "Unrecognized field \"j\" (class com.fasterxml.jackson.databind.deser."
                    + "ThreadLocalBuilderBasedDeserializerTest$TestBean$Builder), not marked as ignorable (one known "
                    + "property: \"i\"])\n at [Source: (String)\"{\"i\":3,\"j\":\"foo\"}\"; line: 1, column: 13] "
                    + "(through reference chain: com.fasterxml.jackson.databind.deser."
                    + "ThreadLocalBuilderBasedDeserializerTest$TestBean$Builder[\"j\"])",
                    e.getMessage());
        }
    }

    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.getInstance();

    private static final class TestBeanWithCreator {

        public int getI() {
            return _i;
        }

        private TestBeanWithCreator(final Builder builder) {
            _i = builder._i;
        }

        private final int _i;

        // CHECKSTYLE.OFF: RedundantModifierCheck - Invoked reflectively
        public static final class Builder extends ThreadLocalBuilder<TestBeanWithCreator> {
            // CHECKSTYLE.ON: RedundantModifierCheck

            // CHECKSTYLE.OFF: RedundantModifierCheck - Invoked reflectively
            public Builder() {
                // CHECKSTYLE.ON: RedundantModifierCheck
                super(TestBeanWithCreator::new);
            }

            // CHECKSTYLE.OFF: RedundantModifierCheck - Invoked reflectively
            @JsonCreator
            public Builder(final Integer value) {
                // CHECKSTYLE.ON: RedundantModifierCheck
                super(TestBeanWithCreator::new);
                _i = value;
            }

            public Builder setI(final Integer value) {
                _i = value;
                return this;
            }

            @Override
            protected void reset() {
                _i = null;
            }

            @NotNull
            private Integer _i;
        }
    }

    private static final class TestBean {

        public int getI() {
            return _i;
        }

        private TestBean(final Builder builder) {
            _i = builder._i;
        }

        private final int _i;

        // CHECKSTYLE.OFF: RedundantModifierCheck - Invoked reflectively
        public static final class Builder extends ThreadLocalBuilder<TestBean> {
            // CHECKSTYLE.ON: RedundantModifierCheck

            // CHECKSTYLE.OFF: RedundantModifierCheck - Invoked reflectively
            public Builder() {
                // CHECKSTYLE.ON: RedundantModifierCheck
                super(TestBean::new);
            }

            public Builder setI(final Integer value) {
                _i = value;
                return this;
            }

            @Override
            protected void reset() {
                _i = null;
            }

            @NotNull
            private Integer _i;
        }
    }
}

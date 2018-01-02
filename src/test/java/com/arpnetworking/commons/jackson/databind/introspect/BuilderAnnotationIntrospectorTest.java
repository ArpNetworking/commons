/**
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
package com.arpnetworking.commons.jackson.databind.introspect;

import com.arpnetworking.commons.builder.ThreadLocalBuilder;
import com.arpnetworking.commons.jackson.databind.ObjectMapperFactory;
import com.arpnetworking.commons.jackson.databind.annotation.JsonIgnoreBuilder;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedClassResolver;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Test for the BuilderAnnotationIntrospector class.
 *
 * @author Gil Markham (gil at groupon dot com)
 */
public class BuilderAnnotationIntrospectorTest {

    @Before
    public void setUp() {
        _introspector = new BuilderAnnotationIntrospector();
    }

    @Test
    public void testReturnsNullWhenNoBuilder() {
        final AnnotatedClass annotatedClass = AnnotatedClassResolver.resolve(
                OBJECT_MAPPER.getDeserializationConfig(),
                OBJECT_MAPPER.constructType(PojoWithoutBuilder.class),
                OBJECT_MAPPER.getDeserializationConfig());
        final Class<?> pojoBuilder = _introspector.findPOJOBuilder(annotatedClass);
        Assert.assertNull(pojoBuilder);
    }

    @Test
    public void testReturnsBuilder() {
        final AnnotatedClass annotatedClass = AnnotatedClassResolver.resolve(
                OBJECT_MAPPER.getDeserializationConfig(),
                OBJECT_MAPPER.constructType(PojoWithBuilder.class),
                OBJECT_MAPPER.getDeserializationConfig());
        final Class<?> pojoBuilder = _introspector.findPOJOBuilder(annotatedClass);
        Assert.assertNotNull(pojoBuilder);
        Assert.assertEquals(PojoWithBuilder.Builder.class, pojoBuilder);
    }

    @Test
    public void testAbstractBuilder() {
        final AnnotatedClass annotatedClass = AnnotatedClassResolver.resolve(
                OBJECT_MAPPER.getDeserializationConfig(),
                OBJECT_MAPPER.constructType(PojoWithAbstractBuilder.class),
                OBJECT_MAPPER.getDeserializationConfig());
        final Class<?> pojoBuilder = _introspector.findPOJOBuilder(annotatedClass);
        Assert.assertNull(pojoBuilder);
    }

    @Test
    public void testIgnoresBuilder() {
        final AnnotatedClass annotatedClass = AnnotatedClassResolver.resolve(
                OBJECT_MAPPER.getDeserializationConfig(),
                OBJECT_MAPPER.constructType(PojoWithIgnoredBuilder.class),
                OBJECT_MAPPER.getDeserializationConfig());
        final Class<?> pojoBuilder = _introspector.findPOJOBuilder(annotatedClass);
        Assert.assertNull(pojoBuilder);
    }

    @Test
    public void testReturnsOverloadedBuilderConfig() {
        final AnnotatedClass annotatedClass = AnnotatedClassResolver.resolve(
                OBJECT_MAPPER.getDeserializationConfig(),
                OBJECT_MAPPER.constructType(PojoWithoutBuilder.class),
                OBJECT_MAPPER.getDeserializationConfig());
        final JsonPOJOBuilder.Value pojoBuilderConfig = _introspector.findPOJOBuilderConfig(annotatedClass);
        Assert.assertNotNull(pojoBuilderConfig);
        Assert.assertEquals("build", pojoBuilderConfig.buildMethodName);
        Assert.assertEquals("set", pojoBuilderConfig.withPrefix);
    }

    @Test
    public void testMyPojoBuilderAnnotationType() {
        final BuilderAnnotationIntrospector.MyPojoBuilder myPojoBuilder =
                new BuilderAnnotationIntrospector.MyPojoBuilder();
        Assert.assertEquals(JsonPOJOBuilder.class, myPojoBuilder.annotationType());
    }

    @Test
    public void testMyPojoBuilderToString() {
        final BuilderAnnotationIntrospector.MyPojoBuilder myPojoBuilder =
                new BuilderAnnotationIntrospector.MyPojoBuilder();
        Assert.assertEquals("build set", myPojoBuilder.toString());
    }

    @Test
    public void testEquals() {
        final BuilderAnnotationIntrospector.MyPojoBuilder myPojoBuilder =
                new BuilderAnnotationIntrospector.MyPojoBuilder();
        final BuilderAnnotationIntrospector.MyPojoBuilder otherBuildMethodPojoBuilder =
                new BuilderAnnotationIntrospector.MyPojoBuilder("create", "set");
        final BuilderAnnotationIntrospector.MyPojoBuilder otherPrefixPojoBuilder =
                new BuilderAnnotationIntrospector.MyPojoBuilder("build", "with");
        Assert.assertTrue(myPojoBuilder.equals(myPojoBuilder));
        Assert.assertFalse(myPojoBuilder.equals(null));
        Assert.assertFalse(myPojoBuilder.equals("ABC"));
        Assert.assertTrue(myPojoBuilder.equals(new BuilderAnnotationIntrospector.MyPojoBuilder()));
        Assert.assertFalse(myPojoBuilder.equals(otherBuildMethodPojoBuilder));
        Assert.assertFalse(myPojoBuilder.equals(otherPrefixPojoBuilder));
        Assert.assertFalse(otherBuildMethodPojoBuilder.equals(otherPrefixPojoBuilder));
    }

    @Test
    public void testHashCode() {
        final BuilderAnnotationIntrospector.MyPojoBuilder myPojoBuilder =
                new BuilderAnnotationIntrospector.MyPojoBuilder();
        final BuilderAnnotationIntrospector.MyPojoBuilder otherPojoBuilder =
                new BuilderAnnotationIntrospector.MyPojoBuilder("create", "set");
        final BuilderAnnotationIntrospector.MyPojoBuilder otherPrefixPojoBuilder =
                new BuilderAnnotationIntrospector.MyPojoBuilder("build", "with");
        Assert.assertEquals(myPojoBuilder.hashCode(), myPojoBuilder.hashCode());
        Assert.assertNotEquals(myPojoBuilder.hashCode(), otherPojoBuilder.hashCode());
        Assert.assertNotEquals(myPojoBuilder.hashCode(), otherPrefixPojoBuilder.hashCode());
        Assert.assertNotEquals(otherPojoBuilder.hashCode(), otherPrefixPojoBuilder.hashCode());
    }

    @Test
    public void testThreadLocalBuilderToPojo() throws IOException {
        final AtomicReference<PojoWithThreadLocalBuilder.Builder> pojoBuilder = new AtomicReference<>();
        ThreadLocalBuilder.build(
                PojoWithThreadLocalBuilder.Builder.class,
                b -> {
                    pojoBuilder.set(b);
                    b.setStrVal("bar");
        });

        final int countBefore = pojoBuilder.get().getResetCount();

        final PojoWithThreadLocalBuilder pojo = OBJECT_MAPPER.readValue(
                "{\"strVal\":\"foo\"}",
                PojoWithThreadLocalBuilder.class);

        final int countAfter = pojoBuilder.get().getResetCount();

        Assert.assertNotNull(pojo);
        Assert.assertEquals("foo", pojo.getStrVal());
        Assert.assertEquals(countBefore + 1, countAfter);
    }

    @Test
    public void testThreadLocalBuilderToBuilder() throws IOException {
        final AtomicReference<PojoWithThreadLocalBuilder.Builder> pojoBuilder = new AtomicReference<>();
        ThreadLocalBuilder.build(
                PojoWithThreadLocalBuilder.Builder.class,
                b -> {
                    pojoBuilder.set(b);
                    b.setStrVal("bar");
                });

        final int countBefore = pojoBuilder.get().getResetCount();

        final PojoWithThreadLocalBuilder.Builder builder = OBJECT_MAPPER.readValue(
                "{\"strVal\":\"foo\"}",
                PojoWithThreadLocalBuilder.Builder.class);
        final PojoWithThreadLocalBuilder pojo = builder.build();

        final int countAfter = pojoBuilder.get().getResetCount();

        Assert.assertNotNull(builder);
        Assert.assertNotNull(pojo);
        Assert.assertEquals("foo", pojo.getStrVal());
        Assert.assertEquals(countBefore, countAfter);
    }

    private BuilderAnnotationIntrospector _introspector;

    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.getInstance();

    /**
     * Test class.
     */
    public static class PojoWithoutBuilder {

        public String getStrVal() {
            return _strVal;
        }

        public Integer getIntVal() {
            return _intVal;
        }

        private String _strVal;
        private Integer _intVal;

        /**
         * Random nested class that should be ignored by introspector.
         */
        public static final class NonBuilderNestedClass {}
    }

    /**
     * Test class.
     */
    public static class PojoWithAbstractBuilder {

        public String getStrVal() {
            return _strVal;
        }

        public Integer getIntVal() {
            return _intVal;
        }

        private String _strVal;
        private Integer _intVal;

        /**
         * Builder for PojoWithAbstractBuilder.
         */
        public abstract static class Builder implements com.arpnetworking.commons.builder.Builder<PojoWithIgnoredBuilder> {}
    }

    /**
     * Test class.
     */
    public static class PojoWithIgnoredBuilder {

        private PojoWithIgnoredBuilder() {}

        protected PojoWithIgnoredBuilder(final Builder builder) {
            _strVal = builder._strVal;
            _source = builder._source;
            _intValue = builder._integerValue;
        }

        public String getStrVal() {
            return _strVal;
        }

        public String getSource() {
            return _source;
        }

        public Integer getIntValue() {
            return _intValue;
        }

        private String _strVal;
        private Integer _intValue;
        private String _source;

        /**
         * Builder for PojoWithIgnoredBuilder.
         */
        @JsonIgnoreBuilder
        public static class Builder implements com.arpnetworking.commons.builder.Builder<PojoWithIgnoredBuilder> {

            public void setStrVal(final String value) {
                _strVal = value;
            }

            @JsonProperty("integerVal")
            public void setIntegerValue(final Integer value) {
                _integerValue = value;
            }

            @Override
            public PojoWithIgnoredBuilder build() {
                return new PojoWithIgnoredBuilder(this);
            }

            private Integer _integerValue;
            private String _strVal;
            private String _source = "builder";
        }
    }

    /**
     * Test class.
     */
    public static class PojoWithThreadLocalBuilder {

        protected PojoWithThreadLocalBuilder(final Builder builder) {
            _strVal = builder._strVal;
        }

        public String getStrVal() {
            return _strVal;
        }

        private final String _strVal;

        /**
         * Builder for PojoWithBuilder.
         */
        public static class Builder extends ThreadLocalBuilder<PojoWithThreadLocalBuilder> {

            public Builder() {
                super(PojoWithThreadLocalBuilder::new);
            }

            public Builder setStrVal(final String value) {
                _strVal = value;
                return this;
            }

            public int getResetCount() {
                return _resetCount.get();
            }

            @Override
            protected void reset() {
                _strVal = null;
                _resetCount.incrementAndGet();
            }

            private String _strVal;
            private final AtomicInteger _resetCount = new AtomicInteger(0);
        }
    }

    /**
     * Test class.
     */
    public static class PojoWithBuilder {

        protected PojoWithBuilder(final Builder builder) {
            _strVal = builder._strVal;
            _source = builder._source;
            _intValue = builder._integerValue;
        }

        public String getStrVal() {
            return _strVal;
        }

        public String getSource() {
            return _source;
        }

        public Integer getIntValue() {
            return _intValue;
        }

        private final String _strVal;
        private final Integer _intValue;
        private final String _source;

        /**
         * Builder for PojoWithBuilder.
         */
        public static class Builder implements com.arpnetworking.commons.builder.Builder<PojoWithBuilder> {

            public void setStrVal(final String value) {
                _strVal = value;
            }

            @JsonProperty("integerVal")
            public void setIntegerValue(final Integer value) {
                _integerValue = value;
            }

            @Override
            public PojoWithBuilder build() {
                return new PojoWithBuilder(this);
            }

            private Integer _integerValue;
            private String _strVal;
            private String _source = "builder";
        }
    }
}

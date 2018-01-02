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
package com.arpnetworking.commons.builder;

import net.sf.oval.constraint.NotNull;
import net.sf.oval.exception.ConstraintsViolatedException;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Tests for the <code>ThreadLocalBuilder</code> class.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 */
public final class ThreadLocalBuilderTest {

    @Test
    public void testSuccess() {
        final MyThreadLocalPojo pojo = MyThreadLocalPojo.Builder.build(
                MyThreadLocalPojo.Builder.class,
                builder -> {
                    builder.setValue("foo");
                });
        Assert.assertNotNull(pojo);
        Assert.assertEquals("foo", pojo.getValue());
    }

    @Test(expected = ConstraintsViolatedException.class)
    public void testBuildFailure() {
        MyThreadLocalPojo.Builder.build(
                MyThreadLocalPojo.Builder.class,
                builder -> {
                        // Nothing to do
                });
        Assert.fail("build should have thrown");
    }

    @Test(expected = IllegalStateException.class)
    public void testConsumeFailure() {
        MyThreadLocalPojo.Builder.build(
                MyThreadLocalPojo.Builder.class,
                builder -> {
                    throw new IllegalStateException("Test exception");
                });
        Assert.fail("build should have thrown");
    }

    @Test
    public void testReuse() {
        final AtomicReference<ThreadLocalBuilder<?>> builderA = new AtomicReference<>();
        final AtomicInteger countA = new AtomicInteger();

        final MyCountingThreadLocalPojo pojoA = MyCountingThreadLocalPojo.Builder.build(
                MyCountingThreadLocalPojo.Builder.class,
                builder -> {
                    builder.setValue("foo");

                    builderA.set(builder); // NEVER DO THIS!
                    countA.set(builder.getResetCount());
                });
        Assert.assertNotNull(pojoA);
        Assert.assertEquals("foo", pojoA.getValue());

        final MyCountingThreadLocalPojo pojoB = MyCountingThreadLocalPojo.Builder.build(
                MyCountingThreadLocalPojo.Builder.class,
                builder -> {
                    builder.setValue("bar");

                    Assert.assertSame(builder, builderA.get());
                    Assert.assertTrue(builder.getResetCount() > countA.get());
                });
        Assert.assertNotNull(pojoB);
        Assert.assertEquals("bar", pojoB.getValue());
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidBuilder() {
        InvalidThreadLocalPojo.Builder.build(
                InvalidThreadLocalPojo.Builder.class,
                builder -> { });
        Assert.fail("Builder construction should have failed");
    }

    @Test
    public void testGeneric() {
        final List<String> list = Arrays.asList("foo", "bar");
        final GenericThreadLocalPojo<String> pojo =
                ThreadLocalBuilder.<GenericThreadLocalPojo<String>, GenericThreadLocalPojo.Builder<String>>buildGeneric(
                        GenericThreadLocalPojo.Builder.class,
                        b -> b.setValue(list)
                );
        Assert.assertEquals(list, pojo.getValue());
    }

    @Test
    public void testClone() {
        final MyThreadLocalPojo beanA = new MyThreadLocalPojo.Builder()
                .setValue("foo")
                .build();
        Assert.assertEquals("foo", beanA.getValue());

        final MyThreadLocalPojo beanB = ThreadLocalBuilder.clone(beanA, MyThreadLocalPojo.Builder.class);
        Assert.assertNotSame(beanA, beanB);
        Assert.assertEquals("foo", beanB.getValue());

        final MyThreadLocalPojo beanC = ThreadLocalBuilder.clone(
                beanA,
                MyThreadLocalPojo.Builder.class,
                b -> b.setValue("bar"));
        Assert.assertNotSame(beanA, beanC);
        Assert.assertNotSame(beanB, beanC);
        Assert.assertEquals("bar", beanC.getValue());
    }

    private static final class MyThreadLocalPojo {

        // IMPORTANT: Because of the reset invocation counting you cannot use
        // this builder class for any tests other than

        public String getValue() {
            return _value;
        }

        private MyThreadLocalPojo(final Builder builder) {
            _value = builder._value;
        }

        private final String _value;

        private static final class Builder extends ThreadLocalBuilder<MyThreadLocalPojo> {

            /* package private */ Builder() {
                super(MyThreadLocalPojo::new);
            }

            public Builder setValue(final String value) {
                _value = value;
                return this;
            }

            @Override
            protected void reset() {
                _value = null;
            }

            @NotNull
            private String _value;
        }
    }

    private static final class MyCountingThreadLocalPojo {

        // IMPORTANT: Because of the reset invocation counting you cannot use
        // this builder class for any tests other than testReuse

        public String getValue() {
            return _value;
        }

        private MyCountingThreadLocalPojo(final Builder builder) {
            _value = builder._value;
        }

        private final String _value;

        private static final class Builder extends ThreadLocalBuilder<MyCountingThreadLocalPojo> {

            /* package private */ Builder() {
                super(MyCountingThreadLocalPojo::new);
            }

            public int getResetCount() {
                return _resetCount.get();
            }

            public Builder setValue(final String value) {
                _value = value;
                return this;
            }

            @Override
            protected void reset() {
                _resetCount.incrementAndGet();
                _value = null;
            }

            @NotNull
            private String _value;
            private final AtomicInteger _resetCount = new AtomicInteger(0);
        }
    }

    private static final class InvalidThreadLocalPojo {

        private InvalidThreadLocalPojo(final Builder builder) { }

        private static final class Builder extends ThreadLocalBuilder<InvalidThreadLocalPojo> {

            private Builder() {
                super(InvalidThreadLocalPojo::new);
            }

            @Override
            protected void reset() { }
        }
    }

    private static final class GenericThreadLocalPojo<T> {

        public List<T> getValue() {
            return _value;
        }

        private GenericThreadLocalPojo(final Builder<T> builder) {
            _value = builder._value;
        }

        private final List<T> _value;

        private static final class Builder<T> extends ThreadLocalBuilder<GenericThreadLocalPojo<T>> {

            /* package private */ Builder() {
                super((java.util.function.Function<Builder<T>, GenericThreadLocalPojo<T>>) GenericThreadLocalPojo::new);
            }

            public Builder<T> setValue(final List<T> value) {
                _value = value;
                return this;
            }

            @Override
            protected void reset() {
                _value = null;
            }

            @NotNull
            private List<T> _value;
        }
    }
}

/*
 * Copyright 2018 Inscope Metrics, Inc.
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
package com.arpnetworking.commons.test;

import com.arpnetworking.commons.builder.OvalBuilder;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import javax.annotation.Nullable;

/**
 * Tests for the {@link BuildableTestHelper} class.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 */
public final class BuildableTestHelperTest {

    @Test
    public void testWorkingBuilder() throws InvocationTargetException, IllegalAccessException {
        BuildableTestHelper.testBuild(
                new PojoWorkingBuilder.Builder()
                        .setValue("foo")
                        .setValueWithoutGetter("bar")
                        .setOptional("123"),
                PojoWorkingBuilder.class);
    }

    @Test(expected = AssertionError.class)
    public void testBrokenBuilder() throws InvocationTargetException, IllegalAccessException {
        BuildableTestHelper.testBuild(
                new PojoBrokenBuilder.Builder()
                        .setValue("foo")
                        .setValueWithoutGetter("bar")
                        .setOptional("123"),
                PojoBrokenBuilder.class);
    }

    @Test(expected = AssertionError.class)
    public void testValueMustBeNonNull() throws InvocationTargetException, IllegalAccessException {
        BuildableTestHelper.testBuild(
                new PojoWorkingBuilder.Builder()
                        .setValue("foo")
                        .setValueWithoutGetter("bar")
                        .setOptional(null),
                PojoWorkingBuilder.class);
    }

    @Test(expected = IllegalStateException.class)
    public void testUnmappedInstanceField() throws InvocationTargetException, IllegalAccessException {
        BuildableTestHelper.testBuild(
                new PojoDisappearingFieldBuilder.Builder()
                        .setValue("foo"),
                PojoDisappearingFieldBuilder.class);
    }

    @Test(expected = IllegalStateException.class)
    public void testMissingBuilderField() throws InvocationTargetException, IllegalAccessException {
        BuildableTestHelper.testBuild(
                new PojoMissingFieldBuilder.Builder()
                        .setValue("foo"),
                PojoMissingFieldBuilder.class);
    }

    private static final class PojoWorkingBuilder {

        public String getValue() {
            return _value;
        }

        public Optional<String> getOptional() {
            return _optional;
        }

        private PojoWorkingBuilder(final PojoWorkingBuilder.Builder builder) {
            _value = builder._value;
            _valueWithoutGetter = builder._valueWithoutGetter;
            _optional = Optional.ofNullable(builder._optional);
        }

        private final String _value;
        private final String _valueWithoutGetter;
        private final Optional<String> _optional;

        private static final class Builder extends OvalBuilder<PojoWorkingBuilder> {

            protected Builder() {
                super(PojoWorkingBuilder::new);
            }

            public Builder setValue(final String value) {
                _value = value;
                return this;
            }

            public Builder setValueWithoutGetter(final String value) {
                _valueWithoutGetter = value;
                return this;
            }

            public Builder setOptional(@Nullable final String value) {
                _optional = value;
                return this;
            }

            public void setIgnoredForReturnType(final String value) {
                return;
            }

            public Builder setIgnoredForParameterCount() {
                return this;
            }

            public Builder setIgnoredForVarArgs(final String... values) {
                return this;
            }

            private String _value;
            private String _valueWithoutGetter;
            private String _optional;
        }
    }

    private static final class PojoBrokenBuilder {

        public String getValue() {
            return _value;
        }

        public Optional<String> getOptional() {
            return _optional;
        }

        private PojoBrokenBuilder(final Builder builder) {
            // Assignment intentionally flipped!
            _value = builder._valueWithoutGetter;
            _valueWithoutGetter = builder._value;
            _optional = Optional.ofNullable(builder._optional);
        }

        private final String _value;
        private final String _valueWithoutGetter;
        private final Optional<String> _optional;

        private static final class Builder extends OvalBuilder<PojoBrokenBuilder> {

            protected Builder() {
                super(PojoBrokenBuilder::new);
            }

            public Builder setValue(final String value) {
                _value = value;
                return this;
            }

            public Builder setValueWithoutGetter(final String value) {
                _valueWithoutGetter = value;
                return this;
            }

            public Builder setOptional(@Nullable final String value) {
                _optional = value;
                return this;
            }

            public void setIgnoredForReturnType(final String value) {
                return;
            }

            public Builder setIgnoredForParameterCount() {
                return this;
            }

            public Builder setIgnoredForVarArgs(final String... values) {
                return this;
            }

            private String _value;
            private String _valueWithoutGetter;
            private String _optional;
        }
    }

    private static final class PojoDisappearingFieldBuilder {

        private PojoDisappearingFieldBuilder(final PojoDisappearingFieldBuilder.Builder builder) {
        }

        private static final class Builder extends OvalBuilder<PojoDisappearingFieldBuilder> {

            protected Builder() {
                super(PojoDisappearingFieldBuilder::new);
            }

            public Builder setValue(final String value) {
                _value = value;
                return this;
            }

            private String _value;
        }
    }

    private static final class PojoMissingFieldBuilder {

        private PojoMissingFieldBuilder(final PojoMissingFieldBuilder.Builder builder) {
            _value = builder._misnamed;
        }

        private final String _value;

        private static final class Builder extends OvalBuilder<PojoMissingFieldBuilder> {

            protected Builder() {
                super(PojoMissingFieldBuilder::new);
            }

            public Builder setValue(final String value) {
                _misnamed = value;
                return this;
            }

            private String _misnamed;
        }
    }
}

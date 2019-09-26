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

import com.arpnetworking.commons.builder.Builder;
import com.arpnetworking.commons.builder.OvalBuilder;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tests for the {@link EqualityTestHelper} class.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
public class EqualityTestHelperTest {

    @Test(expected = AssertionError.class)
    public void testMismatchedBuilders() throws InvocationTargetException, IllegalAccessException {
        final Builder<?> builderA = new PojoA.Builder();
        final Builder<?> builderB = new PojoB.Builder();
        EqualityTestHelper.testEquality(builderA, builderB, Object.class);
    }

    @Test(expected = AssertionError.class)
    public void testSelfEqualityFails() throws InvocationTargetException, IllegalAccessException {
        EqualityTestHelper.testEquality(
                new PojoNotSelfEqual.Builder(),
                new PojoNotSelfEqual.Builder(),
                PojoNotSelfEqual.class);
    }

    @Test(expected = AssertionError.class)
    public void testInstanceEqualityFails() throws InvocationTargetException, IllegalAccessException {
        EqualityTestHelper.testEquality(
                new PojoNotInstanceEqual.Builder(),
                new PojoNotInstanceEqual.Builder(),
                PojoNotInstanceEqual.class);
    }

    @Test(expected = AssertionError.class)
    public void testNullInequalityFails() throws InvocationTargetException, IllegalAccessException {
        EqualityTestHelper.testEquality(
                new PojoNullEqual.Builder(),
                new PojoNullEqual.Builder(),
                PojoNullEqual.class);
    }

    @Test(expected = AssertionError.class)
    public void testOtherTypeInequalityFails() throws InvocationTargetException, IllegalAccessException {
        EqualityTestHelper.testEquality(
                new PojoOtherTypeEqual.Builder(),
                new PojoOtherTypeEqual.Builder(),
                PojoOtherTypeEqual.class);
    }

    @Test(expected = AssertionError.class)
    public void testInstanceHashcodesFails() throws InvocationTargetException, IllegalAccessException {
        EqualityTestHelper.testEquality(
                new PojoRandomHashcode.Builder(),
                new PojoRandomHashcode.Builder(),
                PojoRandomHashcode.class);
    }

    @Test
    public void testWorkingBuilder() throws InvocationTargetException, IllegalAccessException {
        EqualityTestHelper.testEquality(
                new PojoWorkingBuilder.Builder()
                    .setValue("foo")
                    .setOptional("123"),
                new PojoWorkingBuilder.Builder()
                    .setValue("bar")
                    .setOptional("456"),
                PojoWorkingBuilder.class);
    }

    @Test(expected = AssertionError.class)
    public void testBrokenBuilder() throws InvocationTargetException, IllegalAccessException {
        EqualityTestHelper.testEquality(
                new PojoBrokenBuilder.Builder()
                        .setValue("foo"),
                new PojoBrokenBuilder.Builder()
                        .setValue("bar"),
                PojoBrokenBuilder.class);
    }

    @Test(expected = AssertionError.class)
    public void testMissingGetterBuilder() throws InvocationTargetException, IllegalAccessException {
        EqualityTestHelper.testEquality(
                new PojoMissingGetterBuilder.Builder()
                        .setValue("foo"),
                new PojoMissingGetterBuilder.Builder()
                        .setValue("bar"),
                PojoMissingGetterBuilder.class);
    }

    private static final class PojoA {

        private PojoA(final Builder builder) { }

        private static final class Builder extends OvalBuilder<PojoA> {

            protected Builder() {
                super(PojoA::new);
            }
        }
    }

    private static final class PojoB {

        private PojoB(final Builder builder) { }

        private static final class Builder extends OvalBuilder<PojoB> {

            protected Builder() {
                super(PojoB::new);
            }
        }
    }

    private static final class PojoNotSelfEqual {

        private PojoNotSelfEqual(final Builder builder) { }

        @Override
        public boolean equals(final Object other) {
            if (other == this) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        private static final class Builder extends OvalBuilder<PojoNotSelfEqual> {

            protected Builder() {
                super(PojoNotSelfEqual::new);
            }
        }
    }

    private static final class PojoNotInstanceEqual {

        private PojoNotInstanceEqual(final Builder builder) { }

        @Override
        public boolean equals(final Object other) {
            if (other == this) {
                return true;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        private static final class Builder extends OvalBuilder<PojoNotInstanceEqual> {

            protected Builder() {
                super(PojoNotInstanceEqual::new);
            }
        }
    }

    private static final class PojoNullEqual {

        private PojoNullEqual(final Builder builder) { }

        @Override
        @SuppressFBWarnings(value = {"EQ_UNUSUAL"})
        public boolean equals(final Object other) {
            if (other == this) {
                return true;
            }
            if (other == null) {
                return true;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        private static final class Builder extends OvalBuilder<PojoNullEqual> {

            protected Builder() {
                super(PojoNullEqual::new);
            }
        }
    }

    private static final class PojoOtherTypeEqual {

        private PojoOtherTypeEqual(final Builder builder) { }

        @Override
        public boolean equals(final Object other) {
            if (other == this) {
                return true;
            }
            if (other == null) {
                return false;
            }
            if (!(other instanceof PojoOtherTypeEqual)) {
                return true;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        private static final class Builder extends OvalBuilder<PojoOtherTypeEqual> {

            protected Builder() {
                super(PojoOtherTypeEqual::new);
            }
        }
    }

    private static final class PojoRandomHashcode {

        @Override
        public boolean equals(final Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof PojoRandomHashcode)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return _code;
        }

        private PojoRandomHashcode(final Builder builder) {
            _code = builder._code;
        }

        private final int _code;

        private static final class Builder extends OvalBuilder<PojoRandomHashcode> {

            protected Builder() {
                super(PojoRandomHashcode::new);
            }

            @Override
            public PojoRandomHashcode build() {
                _code = NEXT_CODE.incrementAndGet();
                return super.build();
            }

            private int _code;

            private static final AtomicInteger NEXT_CODE = new AtomicInteger(0);
        }
    }

    private static final class PojoWorkingBuilder {

        public String getValue() {
            return _value;
        }

        public Optional<String> getOptional() {
            return _optional;
        }

        @Override
        public boolean equals(final Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof PojoWorkingBuilder)) {
                return false;
            }
            final PojoWorkingBuilder otherPojo = (PojoWorkingBuilder) other;
            return Objects.equals(_value, otherPojo._value)
                    && Objects.equals(_optional, otherPojo._optional);
        }

        @Override
        public int hashCode() {
            return Objects.hash(_value, _optional);
        }

        private PojoWorkingBuilder(final Builder builder) {
            _value = builder._value;
            _optional = Optional.ofNullable(builder._optional);
        }

        private final String _value;
        private final Optional<String> _optional;

        private static final class Builder extends OvalBuilder<PojoWorkingBuilder> {

            protected Builder() {
                super(PojoWorkingBuilder::new);
            }

            public Builder setValue(final String value) {
                _value = value;
                return this;
            }

            public Builder setOptional(final String value) {
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
            private String _optional;
        }
    }

    private static final class PojoBrokenBuilder {

        public String getValue() {
            return "foobar";
        }

        @Override
        @SuppressFBWarnings(value = {"EQ_CHECK_FOR_OPERAND_NOT_COMPATIBLE_WITH_THIS", "EQ_UNUSUAL"})
        public boolean equals(final Object other) {
            if (other == this) {
                return true;
            }
            if (other == null) {
                return false;
            }
            if (!(other instanceof PojoWorkingBuilder)) {
                return true;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return "foobar".hashCode();
        }

        private PojoBrokenBuilder(final Builder builder) { }

        private static final class Builder extends OvalBuilder<PojoBrokenBuilder> {

            protected Builder() {
                super(PojoBrokenBuilder::new);
            }

            public Builder setValue(final String value) {
                _value = value;
                return this;
            }

            private String _value;
        }
    }

    private static final class PojoMissingGetterBuilder {

        @Override
        public boolean equals(final Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof PojoMissingGetterBuilder)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        private PojoMissingGetterBuilder(final Builder builder) { }

        private static final class Builder extends OvalBuilder<PojoMissingGetterBuilder> {

            protected Builder() {
                super(PojoMissingGetterBuilder::new);
            }

            public Builder setValue(final String value) {
                _value = value;
                return this;
            }

            private String _value;
        }
    }
}

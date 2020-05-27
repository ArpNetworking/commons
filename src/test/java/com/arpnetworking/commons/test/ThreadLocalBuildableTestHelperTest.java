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

import com.arpnetworking.commons.builder.ThreadLocalBuilder;
import org.junit.Test;

/**
 * Tests for the {@link BuildableTestHelper} class.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
public final class ThreadLocalBuildableTestHelperTest {

    @Test
    public void testWorkingReset() throws Exception {
        ThreadLocalBuildableTestHelper.testReset(
                new PojoWorkingBuilder.Builder()
                        .setValue("foo"));
    }

    @Test
    public void testWorkingNonNullDefaultReset() throws Exception  {
        ThreadLocalBuildableTestHelper.testReset(
                new PojoWorkingBuilder.Builder()
                        .setValue("foo"));
    }

    @Test(expected = AssertionError.class)
    public void testBrokenBuilder() throws Exception  {
        ThreadLocalBuildableTestHelper.testReset(
                new PojoBrokenBuilder.Builder()
                        .setValue("foo"));
    }

    @Test(expected = IllegalStateException.class)
    public void testMissingFieldBuilder() throws Exception  {
        ThreadLocalBuildableTestHelper.testReset(
                new PojoMissingFieldBuilder.Builder()
                        .setValue("foo"));
    }

    private static final class PojoWorkingBuilder {

        public String getValue() {
            return _value;
        }

        private PojoWorkingBuilder(final PojoWorkingBuilder.Builder builder) {
            _value = builder._value;
        }

        private final String _value;

        private static final class Builder extends ThreadLocalBuilder<PojoWorkingBuilder> {

            protected Builder() {
                super(ThreadLocalBuildableTestHelperTest.PojoWorkingBuilder::new);
            }

            public Builder setValue(final String value) {
                _value = value;
                return this;
            }

            @Override
            protected void reset() {
                _value = null;
            }

            private String _value;
        }
    }

    private static final class PojoBrokenBuilder {

        public String getValue() {
            return _value;
        }

        private PojoBrokenBuilder(final Builder builder) {
            _value = builder._value;
        }

        private final String _value;

        private static final class Builder extends ThreadLocalBuilder<PojoBrokenBuilder> {

            protected Builder() {
                super(ThreadLocalBuildableTestHelperTest.PojoBrokenBuilder::new);
            }

            public Builder setValue(final String value) {
                _value = value;
                return this;
            }

            @Override
            protected void reset() {
                // Intentionally broken reset implementation
            }

            private String _value;
        }
    }

    private static final class PojoNonNullDefaultBuilder {

        public String getValue() {
            return _value;
        }

        private PojoNonNullDefaultBuilder(final PojoNonNullDefaultBuilder.Builder builder) {
            _value = builder._value;
        }

        private final String _value;

        private static final class Builder extends ThreadLocalBuilder<PojoNonNullDefaultBuilder> {

            protected Builder() {
                super(ThreadLocalBuildableTestHelperTest.PojoNonNullDefaultBuilder::new);
            }

            public Builder setValue(final String value) {
                _value = value;
                return this;
            }

            @Override
            protected void reset() {
                _value = "bar";
            }

            private String _value = "bar";
        }
    }

    private static final class PojoMissingFieldBuilder {

        public String getValue() {
            return _value;
        }

        private PojoMissingFieldBuilder(final PojoMissingFieldBuilder.Builder builder) {
            _value = builder._fooValue;
        }

        private final String _value;

        private static final class Builder extends ThreadLocalBuilder<PojoMissingFieldBuilder> {

            protected Builder() {
                super(ThreadLocalBuildableTestHelperTest.PojoMissingFieldBuilder::new);
            }

            public Builder setValue(final String value) {
                // The field name must be derived from the setter name!
                _fooValue = value;
                return this;
            }

            @Override
            protected void reset() {
                _fooValue = null;
            }

            private String _fooValue;
        }
    }
}

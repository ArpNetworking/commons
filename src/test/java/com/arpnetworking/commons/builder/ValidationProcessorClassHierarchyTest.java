/*
 * Copyright 2016 Inscope Metrics Inc.
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

import com.arpnetworking.commons.builder.annotations.SkipValidationProcessor;
import com.arpnetworking.commons.maven.javassist.Processed;
import net.sf.oval.ConstraintViolation;
import net.sf.oval.constraint.NotEmpty;
import net.sf.oval.constraint.NotNull;
import net.sf.oval.constraint.NotNullCheck;
import net.sf.oval.context.FieldContext;
import net.sf.oval.exception.ConstraintsViolatedException;
import org.junit.Assert;
import org.junit.Test;

import java.util.function.Function;
import javax.annotation.Nullable;

/**
 * Tests for the {@link ValidationProcessor} fpr processing of class
 * hierarchies in various states of processing.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
public class ValidationProcessorClassHierarchyTest {

    @Test
    public void testUnprocessedChildProcessedParent() {
        // No exception
        new UnprocessedChildBuilder()
                .setChildValue("Bar")
                .setParentValue("Foo")
                .build();

        // Child validation failure
        try {
            new UnprocessedChildBuilder()
                    .setParentValue("Foo")
                    .build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            Assert.assertEquals(1, e.getConstraintViolations().length);
            assertViolation(e.getConstraintViolations()[0], NotNullCheck.class, null, "_childValue");
        }

        // Parent validation failure
        try {
            new UnprocessedChildBuilder()
                    .setChildValue("Bar")
                    .build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            Assert.assertEquals(1, e.getConstraintViolations().length);
            assertViolation(e.getConstraintViolations()[0], NotNullCheck.class, null, "_parentValue");
        }

        // Child and parent validation failure
        try {
            new UnprocessedChildBuilder().build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            Assert.assertEquals(2, e.getConstraintViolations().length);
            assertViolation(e.getConstraintViolations()[0], NotNullCheck.class, null, "_childValue");
            assertViolation(e.getConstraintViolations()[1], NotNullCheck.class, null, "_parentValue");
        }
    }

    @Test
    public void testProcessedChildUnprocessedParent() {
        // No exception
        new ProcessedChildBuilder()
                .setChildValue("Bar")
                .setParentValue("Foo")
                .build();

        // Child validation failure
        try {
            new ProcessedChildBuilder()
                    .setParentValue("Foo")
                    .build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            Assert.assertEquals(1, e.getConstraintViolations().length);
            assertViolation(e.getConstraintViolations()[0], NotNullCheck.class, null, "_childValue");
        }

        // Parent validation failure
        try {
            new ProcessedChildBuilder()
                    .setChildValue("Bar")
                    .build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            Assert.assertEquals(1, e.getConstraintViolations().length);
            assertViolation(e.getConstraintViolations()[0], NotNullCheck.class, null, "_parentValue");
        }

        // Child and parent validation failure
        try {
            new ProcessedChildBuilder().build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            Assert.assertEquals(2, e.getConstraintViolations().length);
            assertViolation(e.getConstraintViolations()[0], NotNullCheck.class, null, "_childValue");
            assertViolation(e.getConstraintViolations()[1], NotNullCheck.class, null, "_parentValue");
        }
    }

    @Test
    public void testProcessedChildProcessedParent() {
        // No exception
        new FullyProcessedChildBuilder()
                .setChildValue("Bar")
                .setParentValue("Foo")
                .build();

        // Child validation failure
        try {
            new FullyProcessedChildBuilder()
                    .setParentValue("Foo")
                    .build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            Assert.assertEquals(1, e.getConstraintViolations().length);
            assertViolation(e.getConstraintViolations()[0], NotNullCheck.class, null, "_childValue");
        }

        // Parent validation failure
        try {
            new FullyProcessedChildBuilder()
                    .setChildValue("Bar")
                    .build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            Assert.assertEquals(1, e.getConstraintViolations().length);
            assertViolation(e.getConstraintViolations()[0], NotNullCheck.class, null, "_parentValue");
        }

        // Child and parent validation failure
        try {
            new FullyProcessedChildBuilder().build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            // NOTE: It looks like reflection and processing validation analyze
            // the class hierarchy in different directions.
            Assert.assertEquals(2, e.getConstraintViolations().length);
            assertViolation(e.getConstraintViolations()[0], NotNullCheck.class, null, "_parentValue");
            assertViolation(e.getConstraintViolations()[1], NotNullCheck.class, null, "_childValue");
        }
    }

    @Test
    public void testUnprocessedChildUnprocessedParent() {
        // No exception
        new FullyUnprocessedChildBuilder()
                .setChildValue("Bar")
                .setParentValue("Foo")
                .build();

        // Child validation failure
        try {
            new FullyUnprocessedChildBuilder()
                    .setParentValue("Foo")
                    .build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            Assert.assertEquals(1, e.getConstraintViolations().length);
            assertViolation(e.getConstraintViolations()[0], NotNullCheck.class, null, "_childValue");
        }

        // Parent validation failure
        try {
            new FullyUnprocessedChildBuilder()
                    .setChildValue("Bar")
                    .build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            Assert.assertEquals(1, e.getConstraintViolations().length);
            assertViolation(e.getConstraintViolations()[0], NotNullCheck.class, null, "_parentValue");
        }

        // Child and parent validation failure
        try {
            new FullyUnprocessedChildBuilder().build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            // NOTE: It looks like reflection and processing validation analyze
            // the class hierarchy in different directions.
            Assert.assertEquals(2, e.getConstraintViolations().length);
            assertViolation(e.getConstraintViolations()[0], NotNullCheck.class, null, "_childValue");
            assertViolation(e.getConstraintViolations()[1], NotNullCheck.class, null, "_parentValue");
        }
    }

    private static void assertViolation(
            final ConstraintViolation violation,
            final Class<?> checkClass,
            @Nullable final Object invalidValue,
            final String fieldName) {
        Assert.assertEquals(checkClass.getName(), violation.getCheckName());
        Assert.assertEquals(invalidValue, violation.getInvalidValue());
        Assert.assertTrue(violation.getContextPath().get(0) instanceof FieldContext);
        final FieldContext context = (FieldContext) violation.getContextPath().get(0);
        Assert.assertEquals(fieldName, context.getField().getName());
    }

    private static final class TestBean {

        public String getParentValue() {
            return _parentValue;
        }

        public String getChildValue() {
            return _childValue;
        }

        TestBean(final Builder<?> builder) {
            final ChildBuilder childBuilder = (ChildBuilder) builder;
            _parentValue = childBuilder.getParentValue();
            _childValue = childBuilder.getChildValue();
        }

        private final String _parentValue;
        private final String _childValue;
    }

    private interface ParentBuilder {
        ParentBuilder setParentValue(String value);

        String getParentValue();
    }

    private interface ChildBuilder extends ParentBuilder {
        ChildBuilder setChildValue(String value);

        String getChildValue();
    }

    private abstract static class ProcessedParentBuilder extends OvalBuilder<TestBean> implements ParentBuilder {

        ProcessedParentBuilder(final Function<Builder<TestBean>, TestBean> targetConstructor) {
            super(targetConstructor);
        }

        @Override
        public ProcessedParentBuilder setParentValue(final String value) {
            _parentValue = value;
            return this;
        }

        @Override
        public String getParentValue() {
            return _parentValue;
        }

        @NotNull
        @NotEmpty
        private String _parentValue;
    }

    @SkipValidationProcessor
    private abstract static class UnprocessedParentBuilder extends OvalBuilder<TestBean> implements ParentBuilder {

        UnprocessedParentBuilder(final Function<Builder<TestBean>, TestBean> targetConstructor) {
            super(targetConstructor);
        }

        @Override
        public UnprocessedParentBuilder setParentValue(final String value) {
            _parentValue = value;
            return this;
        }

        @Override
        public String getParentValue() {
            return _parentValue;
        }

        @NotNull
        @NotEmpty
        private String _parentValue;
    }

    private static class ProcessedChildBuilder extends UnprocessedParentBuilder implements ChildBuilder {

        ProcessedChildBuilder() {
            super(TestBean::new);
        }

        @Override
        public ProcessedChildBuilder setChildValue(final String value) {
            _childValue = value;
            return this;
        }

        @Override
        public String getChildValue() {
            return _childValue;
        }

        @NotNull
        @NotEmpty
        private String _childValue;
    }

    @SkipValidationProcessor
    private static class UnprocessedChildBuilder extends ProcessedParentBuilder implements ChildBuilder {

        UnprocessedChildBuilder() {
            super(TestBean::new);
        }

        @Override
        public UnprocessedChildBuilder setChildValue(final String value) {
            _childValue = value;
            return this;
        }

        @Override
        public String getChildValue() {
            return _childValue;
        }

        @NotNull
        @NotEmpty
        private String _childValue;
    }

    @Processed("com.example.AnotherProcessorClass")
    private static class FullyProcessedChildBuilder extends ProcessedParentBuilder implements ChildBuilder {

        FullyProcessedChildBuilder() {
            super(TestBean::new);
        }

        @Override
        public FullyProcessedChildBuilder setChildValue(final String value) {
            _childValue = value;
            return this;
        }

        @Override
        public String getChildValue() {
            return _childValue;
        }

        @NotNull
        @NotEmpty
        private String _childValue;
    }

    @SkipValidationProcessor
    private static class FullyUnprocessedChildBuilder extends UnprocessedParentBuilder implements ChildBuilder {

        FullyUnprocessedChildBuilder() {
            super(TestBean::new);
        }

        @Override
        public FullyUnprocessedChildBuilder setChildValue(final String value) {
            _childValue = value;
            return this;
        }

        @Override
        public String getChildValue() {
            return _childValue;
        }

        @NotNull
        @NotEmpty
        private String _childValue;
    }
}

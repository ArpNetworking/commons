/**
 * Copyright 2014 Groupon.com
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
import net.sf.oval.ConstraintViolation;
import net.sf.oval.constraint.Max;
import net.sf.oval.constraint.Min;
import net.sf.oval.constraint.NotEmpty;
import net.sf.oval.constraint.NotNull;
import net.sf.oval.exception.ConstraintsViolatedException;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import javax.naming.NamingException;

/**
 * Tests for the <code>OvalBuilder</code> class. Note, the purpose of this class
 * is not to test Oval, but the OvalBuilder and serve as illustration with a few
 * simple use cases.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 */
public class OvalBuilderTest {

    @Test
    public void testSuccess() {
        final int intValue = 1;
        final Integer rangeIntValue = 50;
        final TestBean testBean = new TestBean.Builder()
                .setInt(intValue)
                .setRangeInt(rangeIntValue)
                .build();
        Assert.assertNotNull(testBean);
        Assert.assertEquals(intValue, testBean.getInt());
        Assert.assertTrue(testBean.getRangeInt().isPresent());
        Assert.assertEquals(rangeIntValue, testBean.getRangeInt().get());
    }

    @Test
    public void testSuccessWithConstructorReference() {
        final int intValue = 1;
        final Integer rangeIntValue = 50;
        final TestBeanFunctionReference testBeanWithFunctionReference = new TestBeanFunctionReference.Builder()
                .setInt(intValue)
                .setRangeInt(rangeIntValue)
                .build();
        Assert.assertNotNull(testBeanWithFunctionReference);
        Assert.assertEquals(intValue, testBeanWithFunctionReference.getInt());
        Assert.assertTrue(testBeanWithFunctionReference.getRangeInt().isPresent());
        Assert.assertEquals(rangeIntValue, testBeanWithFunctionReference.getRangeInt().get());
    }

    @Test
    public void testSuccessRangeIntOptional() {
        final int intValue = 1;
        final TestBean testBean = new TestBean.Builder()
                .setInt(intValue)
                .build();
        Assert.assertNotNull(testBean);
        Assert.assertEquals(intValue, testBean.getInt());
        Assert.assertFalse(testBean.getRangeInt().isPresent());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testBeanNoDefaultConstructor() {
        new NoBuilderConstructorBean.Builder().build();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testBeanConstructorFailureWithException() {
        new BadThrowingBean.Builder().build();
    }

    @Test(expected = RuntimeException.class)
    public void testBeanConstructorFailureWithRuntimeException() {
        new BadRuntimeThrowingBean.Builder().build();
    }

    @Test
    public void testFailureImplicitNull() {
        try {
            new TestBean.Builder()
                    .setRangeInt(50)
                    .build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            Assert.assertNotNull(e.getConstraintViolations());
            Assert.assertEquals(1, e.getConstraintViolations().length);
            final ConstraintViolation cv = e.getConstraintViolations()[0];
            Assert.assertEquals("net.sf.oval.constraint.NotNullCheck", cv.getCheckName());
            Assert.assertThat(cv.getContext().toString(),
                    Matchers.containsString("OvalBuilderTest$TestBean$Builder._int"));
            Assert.assertNull(cv.getInvalidValue());
        }
    }

    @Test
    public void testFailureExplicitNull() {
        try {
            new TestBean.Builder()
                    .setInt(null)
                    .setRangeInt(50)
                    .build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            Assert.assertNotNull(e.getConstraintViolations());
            Assert.assertEquals(1, e.getConstraintViolations().length);
            final ConstraintViolation cv = e.getConstraintViolations()[0];
            Assert.assertEquals("net.sf.oval.constraint.NotNullCheck", cv.getCheckName());
            Assert.assertThat(cv.getContext().toString(),
                    Matchers.containsString("OvalBuilderTest$TestBean$Builder._int"));
            Assert.assertNull(cv.getInvalidValue());
        }
    }

    @Test
    public void testFailureMinViolation() {
        try {
            new TestBean.Builder()
                    .setInt(0)
                    .setRangeInt(-1)
                    .build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            Assert.assertNotNull(e.getConstraintViolations());
            Assert.assertEquals(1, e.getConstraintViolations().length);
            final ConstraintViolation cv = e.getConstraintViolations()[0];
            Assert.assertEquals("net.sf.oval.constraint.MinCheck", cv.getCheckName());
            Assert.assertThat(cv.getContext().toString(),
                    Matchers.containsString("OvalBuilderTest$TestBean$Builder._rangeInt"));
            Assert.assertEquals(-1, cv.getInvalidValue());
        }
    }

    @Test
    public void testFailureMaxViolation() {
        try {
            new TestBean.Builder()
                    .setInt(0)
                    .setRangeInt(101)
                    .build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            Assert.assertNotNull(e.getConstraintViolations());
            Assert.assertEquals(1, e.getConstraintViolations().length);
            final ConstraintViolation cv = e.getConstraintViolations()[0];
            Assert.assertEquals("net.sf.oval.constraint.MaxCheck", cv.getCheckName());
            Assert.assertThat(cv.getContext().toString(),
                    Matchers.containsString("OvalBuilderTest$TestBean$Builder._rangeInt"));
            Assert.assertEquals(101, cv.getInvalidValue());
        }
    }

    @Test
    public void testClone() {
        final TestBean beanA = new TestBean.Builder()
                .setInt(0)
                .setRangeInt(1)
                .setString("foo")
                .build();
        Assert.assertEquals(0, beanA.getInt());
        Assert.assertEquals(Optional.of(1), beanA.getRangeInt());
        Assert.assertEquals(com.google.common.base.Optional.of("foo"), beanA.getString());

        final TestBean beanB = OvalBuilder.<TestBean, TestBean.Builder>clone(beanA)
                .build();
        Assert.assertNotSame(beanA, beanB);
        Assert.assertEquals(0, beanB.getInt());
        Assert.assertEquals(Optional.of(1), beanB.getRangeInt());
        Assert.assertEquals(com.google.common.base.Optional.of("foo"), beanB.getString());
    }

    @Test
    public void testCloneOptionalAbsent() {
        final TestBean beanA = new TestBean.Builder()
                .setInt(0)
                .build();
        Assert.assertEquals(0, beanA.getInt());
        Assert.assertFalse(beanA.getRangeInt().isPresent());
        Assert.assertFalse(beanA.getString().isPresent());

        final TestBean beanB = OvalBuilder.<TestBean, TestBean.Builder>clone(beanA)
                .build();
        Assert.assertNotSame(beanA, beanB);
        Assert.assertEquals(0, beanB.getInt());
        Assert.assertFalse(beanB.getRangeInt().isPresent());
        Assert.assertFalse(beanB.getString().isPresent());
    }

    @Test
    public void testCloneBuilderConstructorThrows() {
        final BuilderConstructorThrowsBean bean = new BuilderConstructorThrowsBean();
        try {
            OvalBuilder.<BuilderConstructorThrowsBean, BuilderConstructorThrowsBean.Builder>clone(bean);
            // CHECKSTYLE.OFF: IllegalCatch - Need to validate details of the exception thrown.
        } catch (final RuntimeException rte) {
            // CHECKSTYLE.ON: IllegalCatch
            Assert.assertTrue(rte.getCause() instanceof InvocationTargetException);
            Assert.assertTrue(rte.getCause().getCause() instanceof IllegalArgumentException);
            Assert.assertEquals("This constructor throws!", rte.getCause().getCause().getMessage());
        }
    }

    @Test
    public void testCloneWithBuilder() {
        final TestBean beanA = new TestBean.Builder()
                .setInt(0)
                .setRangeInt(1)
                .setString("foo")
                .build();
        Assert.assertEquals(0, beanA.getInt());
        Assert.assertEquals(Optional.of(1), beanA.getRangeInt());
        Assert.assertEquals(com.google.common.base.Optional.of("foo"), beanA.getString());

        final TestBean beanB = OvalBuilder.clone(beanA, new TestBean.Builder())
                .build();
        Assert.assertNotSame(beanA, beanB);
        Assert.assertEquals(0, beanB.getInt());
        Assert.assertEquals(Optional.of(1), beanB.getRangeInt());
        Assert.assertEquals(com.google.common.base.Optional.of("foo"), beanB.getString());
    }

    @Test
    public void testCloneWithBuilderOptionalAbsent() {
        final TestBean beanA = new TestBean.Builder()
                .setInt(0)
                .build();
        Assert.assertEquals(0, beanA.getInt());
        Assert.assertFalse(beanA.getRangeInt().isPresent());
        Assert.assertFalse(beanA.getString().isPresent());

        final TestBean beanB = OvalBuilder.clone(beanA, new TestBean.Builder())
                .build();
        Assert.assertNotSame(beanA, beanB);
        Assert.assertEquals(0, beanB.getInt());
        Assert.assertFalse(beanB.getRangeInt().isPresent());
        Assert.assertFalse(beanB.getString().isPresent());
    }

    @Test
    public void testCloneWithBuilderSetterThrows() {
        final SetterThrowsBean bean = new SetterThrowsBean.Builder().build();
        Assert.assertNull(bean.getBar());

        try {
            OvalBuilder.clone(bean, new SetterThrowsBean.Builder());
            Assert.fail("Expected exception not thrown");
            // CHECKSTYLE.OFF: IllegalCatch - Need to validate details of the exception thrown.
        } catch (final RuntimeException rte) {
            // CHECKSTYLE.ON: IllegalCatch
            Assert.assertTrue(rte.getCause() instanceof InvocationTargetException);
            Assert.assertTrue(rte.getCause().getCause() instanceof IllegalArgumentException);
            Assert.assertEquals("This setter throws!", rte.getCause().getCause().getMessage());
        }
    }

    @Test
    public void testCloneWithBuilderNoGetterForSetter() {
        final NoGetterForSetterBean beanA = new NoGetterForSetterBean.Builder()
                .setFoo("Foo")
                .build();
        Assert.assertEquals("Foo", beanA.getBar());

        final NoGetterForSetterBean beanB = OvalBuilder.clone(beanA, new NoGetterForSetterBean.Builder()).build();
        Assert.assertNull(beanB.getBar());
    }

    @Test
    public void testGetGetterForSetterGetPrefix() throws NoSuchMethodException {
        final Optional<Method> method = OvalBuilder.getGetterForSetter(
                MethodBean.Builder.class.getMethod("setValid", Object.class),
                MethodBean.class);

        Assert.assertTrue(method.isPresent());
        Assert.assertEquals(MethodBean.class.getMethod("getValid"), method.get());
    }

    @Test
     public void testGetGetterForSetterIsPrefix() throws NoSuchMethodException {
        final Optional<Method> method = OvalBuilder.getGetterForSetter(
                MethodBean.Builder.class.getMethod("setValid2", Object.class),
                MethodBean.class);

        Assert.assertTrue(method.isPresent());
        Assert.assertEquals(MethodBean.class.getMethod("isValid2"), method.get());
    }

    @Test
    public void testGetGetterForSetterNoPrefix() throws NoSuchMethodException {
        final Optional<Method> method = OvalBuilder.getGetterForSetter(
                MethodBean.Builder.class.getMethod("setValid3", Object.class),
                MethodBean.class);

        Assert.assertTrue(method.isPresent());
        Assert.assertEquals(MethodBean.class.getMethod("valid3"), method.get());
    }

    @Test
    public void testGetGetterForSetterNoSuchMethod() throws NoSuchMethodException {
        final Optional<Method> method = OvalBuilder.getGetterForSetter(
                MethodBean.Builder.class.getMethod("setNoMatchingGetter", Object.class),
                MethodBean.class);

        Assert.assertFalse(method.isPresent());
    }

    @Test
    public void testIsGetterMethod() throws NoSuchMethodException {
        Assert.assertTrue(OvalBuilder.isGetterMethod(
                MethodBean.class.getMethod("getValid")));
        Assert.assertTrue(OvalBuilder.isGetterMethod(
                MethodBean.class.getMethod("isValid2")));

        Assert.assertFalse(OvalBuilder.isGetterMethod(
                MethodBean.class.getMethod("getInvalidReturnType")));
        Assert.assertFalse(OvalBuilder.isGetterMethod(
                MethodBean.class.getMethod("doInvalidName")));
        Assert.assertFalse(OvalBuilder.isGetterMethod(
                MethodBean.class.getMethod("getInvalidArgument", Object.class)));
        Assert.assertFalse(OvalBuilder.isGetterMethod(
                MethodBean.class.getMethod("getInvalidAVariablergument", Object[].class)));
    }

    @Test
    public void testIsSetterMethod() throws NoSuchMethodException {
        Assert.assertTrue(OvalBuilder.isSetterMethod(
                MethodBean.Builder.class.getMethod("setValid", Object.class)));

        Assert.assertFalse(OvalBuilder.isSetterMethod(
                MethodBean.Builder.class.getMethod("doInvalidName", Object.class)));
        Assert.assertFalse(OvalBuilder.isSetterMethod(
                MethodBean.Builder.class.getMethod("setInvalidReturnType", Object.class)));
        Assert.assertFalse(OvalBuilder.isSetterMethod(
                MethodBean.Builder.class.getMethod("setInvalidNoArgument")));
        Assert.assertFalse(OvalBuilder.isSetterMethod(
                MethodBean.Builder.class.getMethod("setInvalidMultipleArguments", Object.class, Object.class)));
        Assert.assertFalse(OvalBuilder.isSetterMethod(
                MethodBean.Builder.class.getMethod("setInvalidVariableArgument", Object[].class)));
    }

    @Test
    public void testToLogValue() {
        final Object builderAsLogValue = new TestBean.Builder().toLogValue();
        Assert.assertNotNull(builderAsLogValue);
    }

    @Test
    public void testToString() {
        final String builderAsString = new MethodBean.Builder().toString();
        Assert.assertNotNull(builderAsString);
        Assert.assertFalse(builderAsString.isEmpty());
    }

    /**
     * Test bean pojo.
     */
    @SuppressWarnings("deprecation")
    public static final class TestBean {

        public int getInt() {
            return _int;
        }

        public Optional<Integer> getRangeInt() {
            return _rangeInt;
        }

        public com.google.common.base.Optional<String> getString() {
            return _string;
        }

        private TestBean(final Builder builder) {
            _int = builder._int;
            _rangeInt = Optional.ofNullable(builder._rangeInt);
            _string = com.google.common.base.Optional.fromNullable(builder._string);
        }

        private final int _int;
        private final Optional<Integer> _rangeInt;
        private final com.google.common.base.Optional<String> _string;

        /**
         * OvalBuilder implementation for TestBean.
         */
        @SkipValidationProcessor
        public static final class Builder extends OvalBuilder<TestBean> {

            public Builder setInt(final Integer value) {
                _int = value;
                return this;
            }

            public Builder setRangeInt(final Integer value) {
                _rangeInt = value;
                return this;
            }

            public Builder setString(final String value) {
                _string = value;
                return this;
            }

            public Builder() {
                super(TestBean.class);
            }

            @NotNull
            private Integer _int;

            @Max(value = 100)
            @Min(value = 0)
            private Integer _rangeInt;

            @NotEmpty
            private String _string;
        }
    }

    /**
     * Test bean with function reference pojo.
     */
    public static final class TestBeanFunctionReference {

        public int getInt() {
            return _int;
        }

        public Optional<Integer> getRangeInt() {
            return _rangeInt;
        }

        public com.google.common.base.Optional<String> getString() {
            return _string;
        }

        private TestBeanFunctionReference(final Builder builder) {
            _int = builder._int;
            _rangeInt = Optional.ofNullable(builder._rangeInt);
            _string = com.google.common.base.Optional.fromNullable(builder._string);
        }

        private final int _int;
        private final Optional<Integer> _rangeInt;
        private final com.google.common.base.Optional<String> _string;

        /**
         * OvalBuilder implementation for TestBean.
         */
        @SkipValidationProcessor
        public static final class Builder extends OvalBuilder<TestBeanFunctionReference> {

            public Builder setInt(final Integer value) {
                _int = value;
                return this;
            }

            public Builder setRangeInt(final Integer value) {
                _rangeInt = value;
                return this;
            }

            public Builder setString(final String value) {
                _string = value;
                return this;
            }

            public Builder() {
                super(TestBeanFunctionReference::new);
            }

            @NotNull
            private Integer _int;

            @Max(value = 100)
            @Min(value = 0)
            private Integer _rangeInt;

            @NotEmpty
            private String _string;
        }
    }

    @SuppressWarnings("deprecation")
    private static final class BadThrowingBean {

        private BadThrowingBean(final Builder builder) throws NamingException {
            throw new NamingException();
        }

        @SkipValidationProcessor
        private static final class Builder extends OvalBuilder<BadThrowingBean> {

            private Builder() {
                super(BadThrowingBean.class);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private static final class BadRuntimeThrowingBean {

        private BadRuntimeThrowingBean(final Builder builder) {
            throw new NullPointerException();
        }

        @SkipValidationProcessor
        private static final class Builder extends OvalBuilder<BadRuntimeThrowingBean> {

            private Builder() {
                super(BadRuntimeThrowingBean.class);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private static final class NoBuilderConstructorBean {

        private NoBuilderConstructorBean() {}

        @SkipValidationProcessor
        private static final class Builder extends OvalBuilder<NoBuilderConstructorBean> {

            private Builder() {
                super(NoBuilderConstructorBean.class);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private static final class NoGetterForSetterBean {

        public String getBar() {
            return _bar;
        }

        private NoGetterForSetterBean(final Builder builder) {
            _bar = builder._foo;
        }

        private final String _bar;

        @SkipValidationProcessor
        private static final class Builder extends OvalBuilder<NoGetterForSetterBean> {

            public Builder setFoo(final String value) {
                _foo = value;
                return this;
            }

            private Builder() {
                super(NoGetterForSetterBean.class);
            }

            private String _foo;
        }
    }

    @SuppressWarnings("deprecation")
    private static final class SetterThrowsBean {

        public String getBar() {
            return _bar;
        }

        private SetterThrowsBean(final Builder builder) {
            _bar = builder._bar;
        }

        private final String _bar;

        @SkipValidationProcessor
        private static final class Builder extends OvalBuilder<SetterThrowsBean> {

            public Builder setBar(final String value) {
                throw new IllegalArgumentException("This setter throws!");
            }

            private Builder() {
                super(SetterThrowsBean.class);
            }

            private String _bar;
        }
    }

    @SuppressWarnings("deprecation")
    private static final class BuilderConstructorThrowsBean {

        BuilderConstructorThrowsBean() {}

        private BuilderConstructorThrowsBean(final Builder builder) {}

        @SkipValidationProcessor
        private static final class Builder extends OvalBuilder<BuilderConstructorThrowsBean> {

            private Builder() {
                super(BuilderConstructorThrowsBean.class);
                throw new IllegalArgumentException("This constructor throws!");
            }
        }
    }

    @SuppressWarnings("deprecation")
    private static final class MethodBean {

        public Object getValid() {
            return null;
        }

        public boolean isValid2() {
            return false;
        }

        public Object valid3() {
            return null;
        }

        public void getInvalidReturnType() {}

        public Object doInvalidName() {
            return null;
        }

        public Object getInvalidArgument(final Object value) {
            return null;
        }

        public Object getInvalidAVariablergument(final Object... values) {
            return null;
        }

        @SkipValidationProcessor
        public static final class Builder extends OvalBuilder<MethodBean> {

            Builder() {
                super(MethodBean.class);
            }

            public Builder setValid(final Object value) {
                return null;
            }

            public Builder setValid2(final Object value) {
                return null;
            }

            public Builder setValid3(final Object value) {
                return null;
            }

            public Builder doInvalidName(final Object value) {
                return null;
            }

            public void setInvalidReturnType(final Object value) {}

            public Builder setInvalidNoArgument() {
                return null;
            }

            public Builder setInvalidMultipleArguments(final Object value1, final Object value2) {
                return null;
            }

            public Builder setInvalidVariableArgument(final Object... values) {
                return null;
            }

            public Builder setNoMatchingGetter(final Object value) {
                return null;
            }
        }
    }
}

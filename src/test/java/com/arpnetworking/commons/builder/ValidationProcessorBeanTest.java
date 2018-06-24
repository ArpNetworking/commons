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

import net.sf.oval.ConstraintViolation;
import net.sf.oval.constraint.AssertFalseCheck;
import net.sf.oval.constraint.AssertNullCheck;
import net.sf.oval.constraint.AssertTrueCheck;
import net.sf.oval.constraint.AssertURLCheck;
import net.sf.oval.constraint.CheckWithCheck;
import net.sf.oval.constraint.DateRangeCheck;
import net.sf.oval.constraint.DigitsCheck;
import net.sf.oval.constraint.EmailCheck;
import net.sf.oval.constraint.EqualToFieldCheck;
import net.sf.oval.constraint.FutureCheck;
import net.sf.oval.constraint.HasSubstringCheck;
import net.sf.oval.constraint.InstanceOfAnyCheck;
import net.sf.oval.constraint.InstanceOfCheck;
import net.sf.oval.constraint.LengthCheck;
import net.sf.oval.constraint.MatchPatternCheck;
import net.sf.oval.constraint.MaxCheck;
import net.sf.oval.constraint.MaxLengthCheck;
import net.sf.oval.constraint.MaxSizeCheck;
import net.sf.oval.constraint.MemberOfCheck;
import net.sf.oval.constraint.MinCheck;
import net.sf.oval.constraint.MinLengthCheck;
import net.sf.oval.constraint.MinSizeCheck;
import net.sf.oval.constraint.NotBlankCheck;
import net.sf.oval.constraint.NotEmptyCheck;
import net.sf.oval.constraint.NotEqualCheck;
import net.sf.oval.constraint.NotEqualToFieldCheck;
import net.sf.oval.constraint.NotMatchPatternCheck;
import net.sf.oval.constraint.NotMemberOfCheck;
import net.sf.oval.constraint.NotNegativeCheck;
import net.sf.oval.constraint.NotNullCheck;
import net.sf.oval.constraint.PastCheck;
import net.sf.oval.constraint.RangeCheck;
import net.sf.oval.constraint.SizeCheck;
import net.sf.oval.constraint.ValidateWithMethodCheck;
import net.sf.oval.context.FieldContext;
import net.sf.oval.exception.ConstraintsViolatedException;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Tests for the <code>ValidationProcessorBean</code> class's
 * <code>OvalBuilder</code> transformed by <code>ValidationProcessor</code>.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 */
public final class ValidationProcessorBeanTest {

    @Test
    public void testTransformationValidationValid() {
        ValidationProcessorBean.Builder.createValid().build();
    }

    @Test(expected = ConstraintsViolatedException.class)
    public void testTransformerValidation() {
        new ValidationProcessorBean.Builder().build();
    }

    @Test
    public void testAssertFalseCheck() {
        final ValidationProcessorBean.Builder builder = ValidationProcessorBean.Builder.createValid()
                .setFalseBoolean(true);
        try {
            builder.build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            assertViolation(e, AssertFalseCheck.class, true, "_falseBoolean", builder);
        }
    }

    @Test
    public void testAssertNullCheck() {
        final ValidationProcessorBean.Builder builder = ValidationProcessorBean.Builder.createValid()
                .setNullObject(true);
        try {
            builder.build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            assertViolation(e, AssertNullCheck.class, true, "_nullObject", builder);
        }
    }

    @Test
    public void testAssertTrueCheck() {
        final ValidationProcessorBean.Builder builder = ValidationProcessorBean.Builder.createValid()
                .setTrueBoolean(false);
        try {
            builder.build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            assertViolation(e, AssertTrueCheck.class, false, "_trueBoolean", builder);
        }
    }

    @Test
    public void testAssertUrlCheck() {
        final ValidationProcessorBean.Builder builder = ValidationProcessorBean.Builder.createValid()
                .setUrlString("foo");
        try {
            builder.build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            assertViolation(e, AssertURLCheck.class, "foo", "_urlString", builder);
        }
    }

    @Test
    public void testCheckWithCheck() {
        final ValidationProcessorBean.Builder builder = ValidationProcessorBean.Builder.createValid()
                .setCheckWithString("False");
        try {
            builder.build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            assertViolation(e, CheckWithCheck.class, "False", "_checkWithString", builder);
        }
    }

    @Test
    public void testDateRangeCheck() {
        final Date outOfRangeDate = new Date(System.currentTimeMillis() + 60000);
        final ValidationProcessorBean.Builder builder = ValidationProcessorBean.Builder.createValid()
                .setRangeDate(outOfRangeDate);
        try {
            builder.build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            assertViolation(e, DateRangeCheck.class, outOfRangeDate, "_rangeDate", builder);
        }
    }

    @Test
    public void testDigitsCheck() {
        final ValidationProcessorBean.Builder builder = ValidationProcessorBean.Builder.createValid()
                .setDigitsString("12.12");
        try {
            builder.build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            assertViolation(e, DigitsCheck.class, "12.12", "_digitsString", builder);
        }
    }

    @Test
    public void testEmailCheck() {
        final ValidationProcessorBean.Builder builder = ValidationProcessorBean.Builder.createValid()
                .setEmailString("foo");
        try {
            builder.build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            assertViolation(e, EmailCheck.class, "foo", "_emailString", builder);
        }
    }

    @Test
    public void testEqualToFieldCheck() {
        final ValidationProcessorBean.Builder builder = ValidationProcessorBean.Builder.createValid()
                .setEqualToFieldString("Bar");
        try {
            builder.build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            assertViolation(e, EqualToFieldCheck.class, "Bar", "_equalToFieldString", builder);
        }
    }

    @Test
    public void testEqualToFieldCheckIgnoresValueNull() {
        ValidationProcessorBean.Builder.createValid()
                .setEqualToFieldString(null)
                .build();
    }

    @Test
    public void testEqualToFieldCheckIgnoresOtherValueNull() {
        final ValidationProcessorBean.Builder builder = ValidationProcessorBean.Builder.createValid()
                .setEqualToFieldCounterpartString(null);
        try {
            builder.build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            assertViolation(e, EqualToFieldCheck.class, "Foo", "_equalToFieldString", builder);
        }
    }

    @Test
    public void testEqualToFieldCheckIgnoresBothNull() {
        ValidationProcessorBean.Builder.createValid()
                .setEqualToFieldString(null)
                .setEqualToFieldCounterpartString(null)
                .build();
    }

    @Test
    public void testFutureCheck() {
        final Date futureDate = new Date(System.currentTimeMillis() - 60000);
        final ValidationProcessorBean.Builder builder = ValidationProcessorBean.Builder.createValid()
                .setFutureDate(futureDate);
        try {
            builder.build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            assertViolation(e, FutureCheck.class, futureDate, "_futureDate", builder);
        }
    }

    @Test
    public void testHasSubstringCheck() {
        final ValidationProcessorBean.Builder builder = ValidationProcessorBean.Builder.createValid()
                .setHasSubstringString("ABC");
        try {
            builder.build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            assertViolation(e, HasSubstringCheck.class, "ABC", "_hasSubstringString", builder);
        }
    }

    @Test
    public void testInstanceOfAnyCheck() {
        final ValidationProcessorBean.Builder builder = ValidationProcessorBean.Builder.createValid()
                .setInstanceOfAnyObject("ABC");
        try {
            builder.build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            assertViolation(e, InstanceOfAnyCheck.class, "ABC", "_instanceOfAnyObject", builder);
        }
    }

    @Test
    public void testInstanceOf() {
        final ValidationProcessorBean.Builder builder = ValidationProcessorBean.Builder.createValid()
                .setInstanceOfObject("ABC");
        try {
            builder.build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            assertViolation(e, InstanceOfCheck.class, "ABC", "_instanceOfObject", builder);
        }
    }

    @Test
    public void testLengthCheck() {
        final ValidationProcessorBean.Builder builder = ValidationProcessorBean.Builder.createValid()
                .setLengthString("ABCD");
        try {
            builder.build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            assertViolation(e, LengthCheck.class, "ABCD", "_lengthString", builder);
        }
    }

    @Test
    public void testMatchPatternCheck() {
        final ValidationProcessorBean.Builder builder = ValidationProcessorBean.Builder.createValid()
                .setMatchPatternString("Far");
        try {
            builder.build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            assertViolation(e, MatchPatternCheck.class, "Far", "_matchPatternString", builder);
        }
    }

    @Test
    public void testMaxCheck() {
        final ValidationProcessorBean.Builder builder = ValidationProcessorBean.Builder.createValid()
                .setMaxInteger(1);
        try {
            builder.build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            assertViolation(e, MaxCheck.class, 1, "_maxInteger", builder);
        }
    }

    @Test
    public void testMaxLengthCheck() {
        final ValidationProcessorBean.Builder builder = ValidationProcessorBean.Builder.createValid()
                .setMaxLengthString("ABCD");
        try {
            builder.build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            assertViolation(e, MaxLengthCheck.class, "ABCD", "_maxLengthString", builder);
        }
    }

    @Test
    public void testMaxSizeCheck() {
        final List<String> maxList = Arrays.asList("A", "B", "C");
        final ValidationProcessorBean.Builder builder = ValidationProcessorBean.Builder.createValid()
                .setMaxSizeList(maxList);
        try {
            builder.build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            assertViolation(e, MaxSizeCheck.class, maxList, "_maxSizeList", builder);
        }
    }

    @Test
    public void testMemberOfCheck() {
        final ValidationProcessorBean.Builder builder = ValidationProcessorBean.Builder.createValid()
                .setMemberOfString("Cat");
        try {
            builder.build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            assertViolation(e, MemberOfCheck.class, "Cat", "_memberOfString", builder);
        }
    }

    @Test
    public void testMinCheck() {
        final ValidationProcessorBean.Builder builder = ValidationProcessorBean.Builder.createValid()
                .setMinInteger(-1);
        try {
            builder.build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            assertViolation(e, MinCheck.class, -1, "_minInteger", builder);
        }
    }

    @Test
    public void testMinLengthCheck() {
        final ValidationProcessorBean.Builder builder = ValidationProcessorBean.Builder.createValid()
                .setMinLengthString("AB");
        try {
            builder.build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            assertViolation(e, MinLengthCheck.class, "AB", "_minLengthString", builder);
        }
    }

    @Test
    public void testMinSizeCheck() {
        final List<String> minList = Collections.singletonList("A");
        final ValidationProcessorBean.Builder builder = ValidationProcessorBean.Builder.createValid()
                .setMinSizeList(minList);
        try {
            builder.build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            assertViolation(e, MinSizeCheck.class, minList, "_minSizeList", builder);
        }
    }

    @Test
    public void testNotBlankCheck() {
        final ValidationProcessorBean.Builder builder = ValidationProcessorBean.Builder.createValid()
                .setNotBlankString("   ");
        try {
            builder.build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            assertViolation(e, NotBlankCheck.class, "   ", "_notBlankString", builder);
        }
    }

    @Test
    public void testNotEmptyCheck() {
        final ValidationProcessorBean.Builder builder = ValidationProcessorBean.Builder.createValid()
                .setNotEmptyString("");
        try {
            builder.build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            assertViolation(e, NotEmptyCheck.class, "", "_notEmptyString", builder);
        }
    }

    @Test
    public void testNotEqualCheck() {
        final ValidationProcessorBean.Builder builder = ValidationProcessorBean.Builder.createValid()
                .setNotEqualString("Foo");
        try {
            builder.build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            assertViolation(e, NotEqualCheck.class, "Foo", "_notEqualString", builder);
        }
    }

    @Test
    public void testNotEqualToFieldCheck() {
        final ValidationProcessorBean.Builder builder = ValidationProcessorBean.Builder.createValid()
                .setNotEqualToFieldString("Foo");
        try {
            builder.build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            assertViolation(e, NotEqualToFieldCheck.class, "Foo", "_notEqualToFieldString", builder);
        }
    }

    @Test
    public void testNotEqualToFieldCheckIgnoresValueNull() {
        ValidationProcessorBean.Builder.createValid()
                .setNotEqualToFieldString(null)
                .build();
    }

    @Test
    public void testNotEqualToFieldCheckIgnoresOtherValueNull() {
        ValidationProcessorBean.Builder.createValid()
                .setNotEqualToFieldCounterpartString(null)
                .build();
    }

    @Test
    public void testNotEqualToFieldCheckIgnoresBothNull() {
        ValidationProcessorBean.Builder.createValid()
                .setNotEqualToFieldString(null)
                .setNotEqualToFieldCounterpartString(null)
                .build();
    }

    @Test
    public void testNotMatchPatternCheck() {
        final ValidationProcessorBean.Builder builder = ValidationProcessorBean.Builder.createValid()
                .setNotMatchPatternString("Foooo");
        try {
            builder.build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            assertViolation(e, NotMatchPatternCheck.class, "Foooo", "_notMatchPatternString", builder);
        }
    }

    @Test
    public void testNotMemberOfCheck() {
        final ValidationProcessorBean.Builder builder = ValidationProcessorBean.Builder.createValid()
                .setNotMemberOfString("Bar");
        try {
            builder.build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            assertViolation(e, NotMemberOfCheck.class, "Bar", "_notMemberOfString", builder);
        }
    }

    @Test
    public void testNotNegativeCheck() {
        final ValidationProcessorBean.Builder builder = ValidationProcessorBean.Builder.createValid()
                .setNotNegativeInteger(-1);
        try {
            builder.build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            assertViolation(e, NotNegativeCheck.class, -1, "_notNegativeInteger", builder);
        }
    }

    @Test
    public void testNotNullCheck() {
        final ValidationProcessorBean.Builder builder = ValidationProcessorBean.Builder.createValid()
                .setNotNullObject(null);
        try {
            builder.build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            assertViolation(e, NotNullCheck.class, null, "_notNullObject", builder);
        }
    }

    @Test
    public void testPastCheck() {
        final Date pastDate = new Date(System.currentTimeMillis() + 60000);
        final ValidationProcessorBean.Builder builder = ValidationProcessorBean.Builder.createValid()
                .setPastDate(pastDate);
        try {
            builder.build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            assertViolation(e, PastCheck.class, pastDate, "_pastDate", builder);
        }
    }

    @Test
    public void testRangeCheck() {
        final ValidationProcessorBean.Builder builder = ValidationProcessorBean.Builder.createValid()
                .setRangeDouble(0.5d);
        try {
            builder.build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            assertViolation(e, RangeCheck.class, 0.5d, "_rangeDouble", builder);
        }
    }

    @Test
    public void testSizeCheck() {
        final List<String> sizeList = Arrays.asList("A", "B", "C");
        final ValidationProcessorBean.Builder builder = ValidationProcessorBean.Builder.createValid()
                .setSizeList(sizeList);
        try {
            builder.build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            assertViolation(e, SizeCheck.class, sizeList, "_sizeList", builder);
        }
    }

    @Test
    public void testValidateWithMethodCheck() {
        final ValidationProcessorBean.Builder builder = ValidationProcessorBean.Builder.createValid()
                .setValidateWithMethodString("false");
        try {
            builder.build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            assertViolation(e, ValidateWithMethodCheck.class, "false", "_validateWithMethodString", builder);
        }
    }

    @Test
    public void testValidateWithMethodCheckIgnoresNull() {
        ValidationProcessorBean.Builder.createValid()
                .setValidateWithMethodString(null)
                .build();
    }

    private static void assertViolation(
            final ConstraintsViolatedException e,
            final Class<?> checkClass,
            @Nullable final Object invalidValue,
            final String fieldName,
            final Object validatedObject) {
        final ConstraintViolation[] violations = e.getConstraintViolations();
        Assert.assertEquals("Violations: " + Arrays.toString(violations), 1, violations.length);
        Assert.assertEquals(checkClass.getName(), violations[0].getCheckName());
        Assert.assertEquals(invalidValue, violations[0].getInvalidValue());
        Assert.assertTrue(violations[0].getContext() instanceof FieldContext);
        final FieldContext context = (FieldContext) violations[0].getContext();
        Assert.assertEquals(fieldName, context.getField().getName());
        Assert.assertSame(validatedObject, violations[0].getValidatedObject());
    }
}

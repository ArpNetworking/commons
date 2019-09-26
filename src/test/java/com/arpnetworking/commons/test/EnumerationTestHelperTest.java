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

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

/**
 * Tests for the {@link EnumerationTestHelper} class.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
public final class EnumerationTestHelperTest {

    @Test(expected = AssertionError.class)
    public void testValueOfNotEnum()
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        EnumerationTestHelper.testValueOf(String.class);
    }

    @Test
    public void testValueOfEmptyEnum()
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        EnumerationTestHelper.testValueOf(Empty.class);
    }

    @Test
    public void testValueOfEnum()
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        EnumerationTestHelper.testValueOf(FooBar.class);
    }

    @Test(expected = AssertionError.class)
    public void testConstructionNotEnum()
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        EnumerationTestHelper.testConstruction(String.class, "fromString");
    }

    @Test
    public void testConstructionEmptyEnum()
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        EnumerationTestHelper.testConstruction(Empty.class, "doesNotExist");
    }

    @Test
    public void testConstructionEnum()
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        EnumerationTestHelper.testConstruction(FooBar.class, "fromString");
    }

    @Test(expected = InvocationTargetException.class)
    public void testConstructionEnumFailure()
            throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        EnumerationTestHelper.testConstruction(FooBar.class, "fromLowerString");
    }

    private enum Empty { }

    private enum FooBar {
        FOO,
        BAR;

        public static FooBar fromString(final String s) {
            return FooBar.valueOf(s);
        }

        public static FooBar fromLowerString(final String s) {
            return FooBar.valueOf(s.toLowerCase(Locale.getDefault()));
        }
    }
}

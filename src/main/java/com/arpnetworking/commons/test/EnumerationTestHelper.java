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

import org.junit.Assert;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Test utility for {@code enum} types.
 *
 * Dependencies:
 * <ul>
 *     <li>junit:junit</li>
 * </ul>
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 */
public final class EnumerationTestHelper {

    /**
     * Test enumeration "valueOf" constructor.
     *
     * This test covers generated code in an enumeration. It serves no
     * purpose other than facilitating 100% code coverage and is only needed
     * in the absence of tooling to allow such generated code paths to be
     * ignored.
     *
     * @param enumerationClass the enumeration {@code Class} to be tested
     * @param <T> the type of the enumeration to be tested
     * @throws InvocationTargetException if factory call throws an exception
     * @throws IllegalAccessException if factory method is inaccessible
     * @throws NoSuchMethodException if factory method does not exist
     */
    public static <T> void testValueOf(final Class<T> enumerationClass)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        testConstruction(enumerationClass, VALUE_OF_METHOD_NAME);
    }

    /**
     * Test enumeration custom constructor.
     *
     * Test that covers generated code in an enumeration. It serves no
     * purpose other than facilitating 100% code coverage and is only needed
     * in the absence of tooling to allow such generated codepaths to be
     * ignored.
     *
     * @param enumerationClass the enumeration {@code Class} to be tested
     * @param <T> the type of the enumeration to be tested
     * @param constructFunction the function name of the constructor for the enum class
     * @throws InvocationTargetException if factory call throws an exception
     * @throws IllegalAccessException if factory method is inaccessible
     * @throws NoSuchMethodException if factory method does not exist
     */
    public static <T> void testConstruction(final Class<T> enumerationClass, final String constructFunction)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Assert.assertTrue(enumerationClass.isEnum());
        for (final T value : enumerationClass.getEnumConstants()) {
            final String valueAsString = value.toString();
            final T actualUnit = valueOf(enumerationClass, valueAsString, constructFunction);
            Assert.assertSame(value, actualUnit);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T valueOf(final Class<T> enumerationClass, final String name, final String functionName)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        final Method valueOfMethod = enumerationClass.getMethod(functionName, String.class);
        return (T) valueOfMethod.invoke(null, name);
    }

    private EnumerationTestHelper() {}

    private static final String VALUE_OF_METHOD_NAME = "valueOf";
}

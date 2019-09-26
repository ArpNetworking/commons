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
import com.google.common.collect.Maps;
import org.junit.Assert;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

/**
 * Test utility for {@link Builder} created instances to validate that all
 * fields set through the {@link Builder} can be accessed through the instance's
 * getter methods. This is most suitable for testing POJOs where all fields set
 * on the {@link Builder} are typically copied and available on the instance.
 *
 * Dependencies:
 * <ul>
 *     <li>junit:junit</li>
 * </ul>
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
public final class BuildableTestHelper {

    /**
     * Test that fields set via {@code builder} setters are available via the
     * constructed instance's getters.
     *
     * @param builder the builder instance to test with non-default values set
     * for all fields
     * @param targetClass the {@code Class} of the instance created by the
     * specified builder instance
     * @param <T> the type built by the specified builder
     * @throws InvocationTargetException if a setter or getter call throws an exception
     * @throws IllegalAccessException if a setter or getter is inaccessible
     */
    public static <T> void testBuild(final Builder<? extends T> builder, final Class<T> targetClass)
            throws InvocationTargetException, IllegalAccessException {
        @SuppressWarnings("unchecked")
        final Class<? extends Builder<T>> builderClass = (Class<? extends Builder<T>>) builder.getClass();
        final Map<Method, Object> expectedValues = Maps.newHashMap();

        // Analyze the builder state
        for (final Method method : builderClass.getMethods()) {
            if (method.getName().startsWith(SETTER_PREFIX)
                    && Builder.class.isAssignableFrom(method.getReturnType())
                    && method.getParameters().length == 1
                    && !method.isVarArgs()) {
                final Optional<Field> field = getField(builderClass, method);
                if (!field.isPresent()) {
                    throw new IllegalStateException("Builder setter does not have matching field");
                }
                final Object expectedValue = getFieldValue(builder, field.get());
                Assert.assertNotNull("Expected value can not be null", expectedValue);
                expectedValues.put(method, expectedValue);
            }
        }

        // Build the target
        final T target = builder.build();

        // Validate the target
        for (final Map.Entry<Method, Object> entry : expectedValues.entrySet()) {
            final Method setter = entry.getKey();
            final Object expectedValue = entry.getValue();
            Object actualValue;
            final Optional<Method> getter = getterForSetter(targetClass, setter);
            final Optional<Field> field = getField(targetClass, setter);
            if (getter.isPresent()) {
                getter.get().setAccessible(true);
                actualValue = getter.get().invoke(target);
            } else if (field.isPresent()) {
                field.get().setAccessible(true);
                actualValue = getFieldValue(target, field.get());
            } else {
                throw new IllegalStateException("Unsupported access pattern");
            }
            if (actualValue instanceof Optional) {
                actualValue = ((Optional<?>) actualValue).orElse(null);
            }

            Assert.assertEquals(expectedValue, actualValue);
        }
    }

    private static <T> Optional<Field> getField(final Class<T> targetClass, final Method setter) {
        final String fieldName =
                "_" + Character.toLowerCase(setter.getName().charAt(SETTER_PREFIX.length()))
                        + setter.getName().substring(SETTER_PREFIX.length() + 1);
        try {
            return Optional.of(targetClass.getDeclaredField(fieldName));
        } catch (final NoSuchFieldException e) {
            return Optional.empty();
        }
    }

    private static <T> Object getFieldValue(final T target, final Field field) throws IllegalAccessException {
        field.setAccessible(true);
        return field.get(target);
    }

    private static <T> Optional<Method> getterForSetter(final Class<T> targetClass, final Method setter) {
        final String getterName = GETTER_PREFIX + setter.getName().substring(SETTER_PREFIX.length());
        try {
            return Optional.of(targetClass.getDeclaredMethod(getterName));
        } catch (final NoSuchMethodException e) {
            return Optional.empty();
        }
    }

    private BuildableTestHelper() {}

    private static final String SETTER_PREFIX = "set";
    private static final String GETTER_PREFIX = "get";
}

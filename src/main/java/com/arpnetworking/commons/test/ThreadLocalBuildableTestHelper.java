/*
 * Copyright 2020 Inscope Metrics, Inc.
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
import com.arpnetworking.commons.builder.ThreadLocalBuilder;
import com.google.common.collect.Maps;
import org.junit.Assert;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.Optional;

/**
 * Test utility for {@link ThreadLocalBuilder} created instances to validate
 * that all fields are set to the same initial values as after construction
 * when {@code ThreadLocalBuilder.reset()} is called. This is most suitable for
 * testing POJOs where all fields set on the {@link Builder} are typically
 * copied and available on the instance.
 *
 * Dependencies:
 * <ul>
 *     <li>junit:junit</li>
 * </ul>
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
public final class ThreadLocalBuildableTestHelper {

    /**
     * Test that fields set via {@code builder} setters are set to same initial
     * values as after construction when {@code ThreadLocalBuilder.reset()} is
     * called.
     *
     * <b>IMPORTANT:</b> This test is destructive on the provided builder!
     *
     * @param builder the builder instance to test with non-default values set
     * for all fields
     * @param <T> the type built by the specified builder
     * @throws InvocationTargetException if a setter or getter call throws an exception
     * @throws IllegalAccessException if a setter or getter is inaccessible
     * @throws InstantiationException if a new instance of {@code builder} cannot be created
     * @throws NoSuchMethodException if {@link ThreadLocalBuilder} no longer has a {@code reset()} method
     */
    // CHECKSTYLE.OFF: ThrowsCount - NoSuchMethodException must be thrown as conversion is untestable
    public static <T> void testReset(final ThreadLocalBuilder<? extends T> builder)
            throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        // CHECKSTYLE.ON: ThrowsCount
        @SuppressWarnings("unchecked")
        final Class<? extends ThreadLocalBuilder<T>> builderClass = (Class<? extends ThreadLocalBuilder<T>>) builder.getClass();
        final Map<Field, Object> expectedValues = Maps.newHashMap();

        // Capture the builder state
        for (final Method method : BuilderTestUtility.getSetters(builderClass)) {
            final Optional<Field> field = BuilderTestUtility.getField(builderClass, method);
            if (!field.isPresent()) {
                throw new IllegalStateException(String.format(
                        "Builder setter %s does not have matching field",
                        method.getName()
                ));
            }
            final Object expectedValue = BuilderTestUtility.getFieldValue(builder, field.get());
            Assert.assertNotNull("Expected value for " + field.get().getName() + " can not be null", expectedValue);
            expectedValues.put(field.get(), expectedValue);
        }

        // Create a new builder for comparison
        final Constructor<? extends ThreadLocalBuilder<T>> ctor = builderClass.getDeclaredConstructor();
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            ctor.setAccessible(true);
            return null;
        });
        final ThreadLocalBuilder<? extends T> newBuilder = ctor.newInstance();

        // Reset the provided builder for comparison
        // NOTE: This is destructive on the state of the provided builder!
        final Method resetMethod = ThreadLocalBuilder.class.getDeclaredMethod("reset");
        resetMethod.setAccessible(true);
        resetMethod.invoke(builder);

        // Compare the builder states
        // 1) The reset state should differ from the provided state (precondition)
        // 2) The new state should differ from the provided state (precondition)
        // 3) The reset state should be the _same_ as the new state
        // ^ A new default value should NOT be created for each instance
        for (final Map.Entry<Field, Object> entry : expectedValues.entrySet()) {
            final Field field = entry.getKey();
            final Object providedValue = entry.getValue();
            final Object newBuilderValue = BuilderTestUtility.getFieldValue(newBuilder, field);
            final Object resetBuilderValue = BuilderTestUtility.getFieldValue(builder, field);

            Assert.assertNotEquals(
                    String.format(
                            "Field %s on provided builder is same as constructed value",
                            field.getName()),
                    providedValue,
                    newBuilderValue);
            Assert.assertNotEquals(
                    String.format(
                            "Field %s on provided builder is same as reset value",
                            field.getName()),
                    providedValue,
                    resetBuilderValue);
            // It would be better for this next one to be `assertSame()` rather than `assertEquals()`,
            //   since that would require default field values to be reused, thereby discouraging `reset()`
            //   from creating new objects.
            // However: consider JSON deserialization with a @JsonAnySetter.
            //   Such a method might get called many, many times, and we don't want to build a new ImmutableMap each time,
            //   so we want the underlying map to be mutable.
            // ...but that means that `reset()` _must_ create a new Map.
            // So, for now, this test checks `.equals` instead of `==`.
            // TODO(spencerpearson): can/should we somehow specially bless ^^^ that pattern,
            //   so that in _other_ cases we can use `==`, like we want to?
            Assert.assertEquals(
                    String.format(
                            "Field %s is not the same on reset and construction",
                            field.getName()),
                    newBuilderValue,
                    resetBuilderValue);
        }
    }

    private ThreadLocalBuildableTestHelper() {}
}

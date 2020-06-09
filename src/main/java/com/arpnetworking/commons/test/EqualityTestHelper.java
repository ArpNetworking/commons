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
import org.junit.Assert;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

/**
 * Test utility for {@link Builder} creates instances to validate that all
 * fields set on the {@link Builder} are accessible by the corresponding getter
 * on the built instance. This is most suitable for testing POJOs where all
 * fields set on the {@link Builder} are typically copied and available on the
 * instance.
 *
 * Dependencies:
 * <ul>
 *     <li>junit:junit</li>
 * </ul>
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
public final class EqualityTestHelper {

    /**
     * Test the equality of a pojo. The test method cannot use the default value
     * of each type as the alternate value for each field because the default
     * value may not be valid for that field. Therefore, the test method
     * requires two distinct and valid builder instances.
     *
     * @param builderA builder pre-populated with values all distinct from {@code builderB}
     * @param builderB builder pre-populated with values all distinct from {@code builderA}
     * @param targetClass The {@link Class} of the instance created by the builder.
     * @param <T> The type built by the builder.
     * @throws InvocationTargetException if a setter or getter call throws an exception
     * @throws IllegalAccessException if a setter or getter is inaccessible
     */
    public static <T> void testEquality(
            final Builder<? extends T> builderB,
            final Builder<? extends T> builderA,
            final Class<T> targetClass) throws InvocationTargetException, IllegalAccessException {

        // The type's of the two builders must be the same
        @SuppressWarnings("unchecked")
        final Class<? extends Builder<T>> builderAClass = (Class<? extends Builder<T>>) builderA.getClass();
        @SuppressWarnings("unchecked")
        final Class<? extends Builder<T>> builderBClass = (Class<? extends Builder<T>>) builderB.getClass();

        Assert.assertEquals(builderAClass, builderBClass);

        // Both builders must produce valid instances
        final Object objectA = builderA.build();
        final Object objectB = builderB.build();

        // Test the simple equality cases
        // NOTE: Do not use Objects.equals here to ensure that the actual equals method is exercised
        Assert.assertTrue("Self equality failed", objectA.equals(objectA));
        Assert.assertTrue("Clone equality failed", objectA.equals(builderA.build()));
        Assert.assertFalse("Null inequality failed", objectA.equals(null));
        Assert.assertFalse("Type inequality failed", objectA.equals(new Object()));

        // While we're here let's just check the hash codes of two separate but identical instances are the same
        Assert.assertEquals("Equal instance hashcodes differ", objectA.hashCode(), builderA.build().hashCode());

        // Create permutations of objectA using each field value from objectB and test field by field inequality
        for (final Method method : builderAClass.getMethods()) {
            if (method.getName().startsWith(SETTER_PREFIX)
                    && Builder.class.isAssignableFrom(method.getReturnType())
                    && method.getParameters().length == 1
                    && !method.isVarArgs()) {
                // Find the matching getter
                final Optional<Method> getter = getterForSetter(targetClass, method);
                Assert.assertTrue("Getter not found for setter: " + method, getter.isPresent());

                // Set the value from B on builder for A
                getter.get().setAccessible(true);
                final Object originalValue = getter.get().invoke(objectA);
                final Object alternateValue = getter.get().invoke(objectB);
                method.invoke(builderA, unwrapOptional(alternateValue));

                // Create a variation of objectA with a single field from objectB
                final Object alternatePojo = builderA.build();

                // Assert that they are not equal
                Assert.assertFalse("Pojo inequality failed for: " + getter, Objects.equals(objectA, alternatePojo));

                // Restore the value from objectA on builder for objectA
                method.invoke(builderA, unwrapOptional(originalValue));
            }
        }
    }

    private static Object unwrapOptional(final Object object) {
        if (object instanceof Optional) {
            final Optional<?> optional = (Optional) object;
            return optional.orElse(null);
        }
        return object;
    }

    private static <T> Optional<Method> getterForSetter(final Class<T> targetClass, final Method setter) {
        final String getterName = GETTER_PREFIX + setter.getName().substring(SETTER_PREFIX.length());
        Class<?> clazz = targetClass;
        while (clazz != null) {
            try {
                return Optional.of(clazz.getDeclaredMethod(getterName));
            } catch (final NoSuchMethodException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return Optional.empty();
    }

    private EqualityTestHelper() {}

    private static final String SETTER_PREFIX = "set";
    private static final String GETTER_PREFIX = "get";
}

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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Shared methods for {@link com.arpnetworking.commons.builder.Builder} test
 * helpers.
 *
 * @author Ville Koskela (ville at koskilabs dot com)
 */
public final class BuilderTestUtility {

    static List<Method> getSetters(final Class<? extends Builder<?>> builderClass) {
        final List<Method> setters = new ArrayList<>();
        for (final Method method : builderClass.getMethods()) {
            if (method.getName().startsWith(SETTER_PREFIX)
                    && Builder.class.isAssignableFrom(method.getReturnType())
                    && method.getParameters().length == 1
                    && !method.isVarArgs()) {
                setters.add(method);
            }
        }
        return setters;
    }

    static <T> Optional<Field> getField(final Class<T> targetClass, final Method setter) {
        final String fieldName =
                "_" + Character.toLowerCase(setter.getName().charAt(SETTER_PREFIX.length()))
                        + setter.getName().substring(SETTER_PREFIX.length() + 1);
        try {
            return Optional.of(targetClass.getDeclaredField(fieldName));
        } catch (final NoSuchFieldException e) {
            return Optional.empty();
        }
    }

    static <T> Object getFieldValue(final T target, final Field field) throws IllegalAccessException {
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            field.setAccessible(true);
            return null;
        });
        return field.get(target);
    }

    static <T> Optional<Method> getterForSetter(final Class<T> targetClass, final Method setter) {
        final String getterName = GETTER_PREFIX + setter.getName().substring(SETTER_PREFIX.length());
        try {
            return Optional.of(targetClass.getDeclaredMethod(getterName));
        } catch (final NoSuchMethodException e) {
            return Optional.empty();
        }
    }

    private BuilderTestUtility() {}

    static final String SETTER_PREFIX = "set";
    static final String GETTER_PREFIX = "get";
}

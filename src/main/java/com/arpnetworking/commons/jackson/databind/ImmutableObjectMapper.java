/*
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
package com.arpnetworking.commons.jackson.databind;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * Immutable decorator for {@link ObjectMapper}.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
public final class ImmutableObjectMapper {

    /**
     * Decorate an {@link ObjectMapper} instance so that it is immutable.
     * <i>Warning:</i> Anyone with a reference to the original
     * {@link ObjectMapper} instance may modify it and thus appear to
     * modify the decorated instance as well.
     *
     * @param objectMapper The {@link ObjectMapper} instance to decorate.
     * @return Immutable {@link ObjectMapper} instance.
     */
    public static ObjectMapper of(final ObjectMapper objectMapper) {
        return enhanceAndProxy(objectMapper, ObjectMapper.class);
    }

    /*package private*/ static <T> T enhanceAndProxy(final T proxy, final Class<T> clazz) {
        try {
            final ByteBuddy byteBuddy = new ByteBuddy();
            final Class<? extends T> enhanced = byteBuddy.subclass(clazz)
                    .method(ElementMatchers.any())
                    .intercept(InvocationHandlerAdapter.of(new SafeMethodCallback<>(proxy)))
                    .method(new UnsafeMethodMatcher())
                    .intercept(InvocationHandlerAdapter.of(new UnsafeMethodCallback()))
                    .make()
                    .load(ImmutableObjectMapper.class.getClassLoader())
                    .getLoaded();

            return enhanced.getDeclaredConstructor().newInstance();
        } catch (final InvocationTargetException
                       | InstantiationException
                       | IllegalAccessException
                       | NoSuchMethodException
                       | IllegalAccessError e) {
            throw new RuntimeException(e);
        }
    }

    private ImmutableObjectMapper() {}


    private static final class UnsafeMethodMatcher implements ElementMatcher<MethodDescription> {

        UnsafeMethodMatcher() {}

        // CHECKSTYLE.OFF: IllegalInstantiation - No Guava Dependency
        private static final Set<String> SAFE_METHOD_NAMES = new HashSet<>();
        // CHECKSTYLE.ON: IllegalInstantiation

        // CHECKSTYLE.OFF: ExecutableStatementCount - Data definition
        static {
            // Read Methods
            SAFE_METHOD_NAMES.add("reader");
            SAFE_METHOD_NAMES.add("readerForUpdating");
            SAFE_METHOD_NAMES.add("readTree");
            SAFE_METHOD_NAMES.add("readValue");
            SAFE_METHOD_NAMES.add("readValues");
            SAFE_METHOD_NAMES.add("readerWithView");
            // Write Methods
            SAFE_METHOD_NAMES.add("writer");
            SAFE_METHOD_NAMES.add("writerFor");
            SAFE_METHOD_NAMES.add("writeTree");
            SAFE_METHOD_NAMES.add("writeValue");
            SAFE_METHOD_NAMES.add("writeValueAsBytes");
            SAFE_METHOD_NAMES.add("writeValueAsString");
            SAFE_METHOD_NAMES.add("writerWithDefaultPrettyPrinter");
            SAFE_METHOD_NAMES.add("writerWithType");
            SAFE_METHOD_NAMES.add("writerWithView");
            // Inspection & Miscellaneous
            SAFE_METHOD_NAMES.add("acceptJsonFormatVisitor");
            SAFE_METHOD_NAMES.add("canDeserialize");
            SAFE_METHOD_NAMES.add("canSerialize");
            SAFE_METHOD_NAMES.add("constructType");
            SAFE_METHOD_NAMES.add("convertValue");
            SAFE_METHOD_NAMES.add("copy");
            SAFE_METHOD_NAMES.add("createArrayNode");
            SAFE_METHOD_NAMES.add("createObjectNode");
            SAFE_METHOD_NAMES.add("defaultClassIntrospector");
            SAFE_METHOD_NAMES.add("findMixInClassFor");
            SAFE_METHOD_NAMES.add("findModules");
            SAFE_METHOD_NAMES.add("getDateFormat");
            SAFE_METHOD_NAMES.add("getDeserializationConfig");
            SAFE_METHOD_NAMES.add("getDeserializationContext");
            SAFE_METHOD_NAMES.add("generateJsonSchema");
            SAFE_METHOD_NAMES.add("getNodeFactory");
            SAFE_METHOD_NAMES.add("getPropertyNamingStrategy");
            SAFE_METHOD_NAMES.add("getSerializationConfig");
            SAFE_METHOD_NAMES.add("getSerializerFactory");
            SAFE_METHOD_NAMES.add("getTypeFactory");
            SAFE_METHOD_NAMES.add("isEnabled");
            SAFE_METHOD_NAMES.add("mixInCount");
            SAFE_METHOD_NAMES.add("treeAsTokens");
            SAFE_METHOD_NAMES.add("treeToValue");
            SAFE_METHOD_NAMES.add("valueToTree");
            SAFE_METHOD_NAMES.add("version");

            // The following are unsafe in addition to obvious mutators:
            // * getFactory
            // * getJsonFactory
            // * getSerializerProvider
            // * getSubtypeResolver
            // * getVisibilityChecker
        }
        // CHECKSTYLE.ON: ExecutableStatementCount

        @Override
        public boolean matches(@Nonnull final MethodDescription target) {
            Objects.requireNonNull(target);

            // All non-public methods are considered safe
            if ((target.getModifiers() & Modifier.PUBLIC) == 0) {
                return false;
            }
            // All methods declared on root Object class are considered safe
            if (target.getDeclaringType().getTypeName().equals(Object.class.getName())) {
                return false;
            }
            // All methods explicitly listed are considered safe
            return !SAFE_METHOD_NAMES.contains(target.getName());
        }
    }

    private static final class SafeMethodCallback<T> implements InvocationHandler {

        SafeMethodCallback(final T wrapper) {
            _wrapper = wrapper;
        }

        // CHECKSTYLE.OFF: IllegalThrows - Required by external interface
        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            // CHECKSTYLE.ON: IllegalThrows
            method.setAccessible(true);
            try {
                return method.invoke(_wrapper, args);
            } catch (final InvocationTargetException e) {
                // Method.invoke() throws InvocationTargetException when the underlying method throws
                // so we rather rethrow the original exception instead of having it wrapped in InvocationTargetException
                throw e.getCause();
            }
        }

        private final T _wrapper;
    }

    private static final class UnsafeMethodCallback implements InvocationHandler {
        // CHECKSTYLE.OFF: IllegalThrows - Required by external interface
        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) {
            // CHECKSTYLE.ON: IllegalThrows
            throw new UnsupportedOperationException(String.format(
                    "Cannot mutate immutable ObjectMapper; method=%s",
                    method.getName()));
        }
    }
}

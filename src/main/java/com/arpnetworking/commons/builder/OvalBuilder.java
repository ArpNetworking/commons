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

import com.arpnetworking.logback.annotations.LogValue;
import com.arpnetworking.steno.LogValueMapFactory;
import com.arpnetworking.steno.Logger;
import com.arpnetworking.steno.LoggerFactory;
import net.sf.oval.ConstraintViolation;
import net.sf.oval.Validator;
import net.sf.oval.exception.ConstraintsViolatedException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * This abstract class for builders that define data constraints using Oval
 * annotations.
 *
 * Dependencies:
 * <ul>
 *     <li>net.sf.oval:oval</li>
 *     <li>com.google.guava:guava</li>
 * </ul>
 *
 * TODO(vkoskela): Once we convert entirely to Java's Optional we can remove the Guava dependency.
 *
 * @param <T> The type of object created by the builder.
 *
 * @author Ville Koskela (vkoskela at groupon dot com)
 */
public abstract class OvalBuilder<T> implements Builder<T> {

    /**
     * Static factory initializes the source type's builder with state from
     * the source instance. The builder implementation and its default
     * constructor must be accessible by OvalBuilder.
     *
     * @param <T> The type of object created by the builder.
     * @param <B> The type of builder to return.
     * @param source The source of initial state.
     * @return Instance of builder {@code <B>} populated from source.
     */
    @SuppressWarnings("unchecked")
    public static <T, B extends Builder<? super T>> B clone(final T source) {
        B builder = null;
        try {
            final Class<B> builderClass = (Class<B>) Class.forName(
                    source.getClass().getName() + "$Builder",
                    true, // initialize
                    source.getClass().getClassLoader());
            final Constructor<B> builderConstructor = builderClass.getDeclaredConstructor();
            AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                builderConstructor.setAccessible(true);
                return null;
            });
            builder = builderConstructor.newInstance();
        } catch (final InvocationTargetException | NoSuchMethodException | InstantiationException
                | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return clone(source, builder);
    }

    /**
     * Static factory initializes the specified builder with state from the
     * source instance.
     *
     * @param <T> The type of object created by the builder.
     * @param <B> The type of builder to return.
     * @param source The source of initial state.
     * @param target The target builder instance.
     * @return Target populated from source.
     */
    public static <T, B extends Builder<? super T>> B clone(final T source, final B target) {
        for (final Method targetMethod : target.getClass().getMethods()) {
            if (isSetterMethod(targetMethod)) {
                final Optional<Method> getterMethod = getGetterForSetter(targetMethod, source.getClass());
                if (getterMethod.isPresent()) {
                    try {
                        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                            getterMethod.get().setAccessible(true);
                            return null;
                        });
                        Object value = getterMethod.get().invoke(source);
                        if (value instanceof Optional) {
                            final Optional<?> optional = (Optional<?>) value;
                            value = optional.orElse(null);
                        } else if (value instanceof com.google.common.base.Optional) {
                            final com.google.common.base.Optional<?> optional = (com.google.common.base.Optional<?>) value;
                            value = optional.orNull();
                        }
                        targetMethod.invoke(target, value);
                    } catch (final IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    LOGGER.warn()
                            .setEvent("OvalBuilder")
                            .setMessage("No getter for setter")
                            .addData("setter", targetMethod)
                            .addData("source", source)
                            .addData("target", target)
                            .log();
                }
            }
        }
        return target;
    }

    /**
     * Generate a Steno log compatible representation.
     *
     * @return Steno log compatible representation.
     */
    @LogValue
    public Object toLogValue() {
        return LogValueMapFactory.builder(this)
                .put("targetClass", _targetClass)
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toLogValue().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T build() {
        final List<ConstraintViolation> violations = VALIDATOR.validate(this);
        if (!violations.isEmpty()) {
            throw new ConstraintsViolatedException(violations);
        }
        return construct();
    }

    /**
     * Protected method to construct the target class reflectively from the
     * specified type by passing its constructor an instance of this builder.
     *
     * @return Instance of target class created from this builder.
     */
    protected T construct() {
        try {
            final Constructor<? extends T> constructor = _targetClass.getDeclaredConstructor(this.getClass());
            AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                constructor.setAccessible(true);
                return null;
            });
            return constructor.newInstance(this);

        } catch (final NoSuchMethodException
                | SecurityException
                | InstantiationException
                | IllegalAccessException
                | IllegalArgumentException e) {
            throw new UnsupportedOperationException(
                    String.format(UNABLE_TO_CONSTRUCT_TARGET_CLASS, _targetClass),
                    e);
        } catch (final InvocationTargetException e) {
            // If the constructor of the class threw an exception, unwrap it and
            // rethrow it. If the constructor throws anything other than a
            // RuntimeException we wrap it.
            final Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new UnsupportedOperationException(
                    String.format(UNABLE_TO_CONSTRUCT_TARGET_CLASS, _targetClass),
                    cause);
        }
    }

    /**
     * Protected constructor for subclasses.
     *
     * @param targetClass The concrete type to be created by this builder.
     */
    protected OvalBuilder(final Class<? extends T> targetClass) {
        _targetClass = targetClass;
    }

    /* package private */ static Optional<Method> getGetterForSetter(final Method setter, final Class<?> clazz) {
        // Attempt to find "getFoo" and then "isFoo"; the parameter type is not
        // definitively indicative of get vs is because an Optional wrapped
        // boolean can be exposed as get instead of is. Finally, attempt no prefix
        // in cases where the setter is setIsFoo and getter is isFoo; this also
        // covers setFoo and foo.
        final String baseName = setter.getName().substring(SETTER_METHOD_PREFIX.length());
        try {
            final String getterName = GETTER_GET_METHOD_PREFIX + baseName;
            return Optional.of(clazz.getDeclaredMethod(getterName));
        } catch (final NoSuchMethodException e1) {
            try {
                final String getterName = GETTER_IS_METHOD_PREFIX + baseName;
                return Optional.of(clazz.getDeclaredMethod(getterName));
            } catch (final NoSuchMethodException e2) {
                try {
                    final String getterName = baseName.substring(0, 1).toLowerCase(Locale.getDefault()) + baseName.substring(1);
                    return Optional.of(clazz.getDeclaredMethod(getterName));
                } catch (final NoSuchMethodException e3) {
                    return Optional.empty();
                }
            }
        }
    }

    /* package private */ static boolean isGetterMethod(final Method method) {
        return (method.getName().startsWith(GETTER_GET_METHOD_PREFIX)
                || method.getName().startsWith(GETTER_IS_METHOD_PREFIX))
                &&
                !Void.TYPE.isAssignableFrom(method.getReturnType())
                &&
                !method.isVarArgs()
                &&
                method.getParameterTypes().length == 0;
    }

    /* package private */ static boolean isSetterMethod(final Method method) {
        return method.getName().startsWith(SETTER_METHOD_PREFIX)
                &&
                Builder.class.isAssignableFrom(method.getReturnType())
                &&
                !method.isVarArgs()
                &&
                method.getParameterTypes().length == 1;
    }

    private final Class<? extends T> _targetClass;

    private static final Validator VALIDATOR = new Validator();
    private static final Logger LOGGER = LoggerFactory.getLogger(OvalBuilder.class);

    private static final String GETTER_IS_METHOD_PREFIX = "is";
    private static final String GETTER_GET_METHOD_PREFIX = "get";
    private static final String SETTER_METHOD_PREFIX = "set";
    private static final String UNABLE_TO_CONSTRUCT_TARGET_CLASS = "Unable to construct target class; target_class=%s";
}

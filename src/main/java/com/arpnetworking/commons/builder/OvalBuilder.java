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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

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
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
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
            Constructor<B> cachedBuilderConstructor = (Constructor<B>) BUILDER_CONSTRUCTOR_CACHE.get(source.getClass());
            if (cachedBuilderConstructor == null) {
                final Class<B> builderClass = (Class<B>) Class.forName(
                        source.getClass().getName() + "$Builder",
                        true, // initialize
                        source.getClass().getClassLoader());
                final Constructor<B> builderConstructor = builderClass.getDeclaredConstructor();
                AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                    builderConstructor.setAccessible(true);
                    return null;
                });
                BUILDER_CONSTRUCTOR_CACHE.put(source.getClass(), builderConstructor);
                cachedBuilderConstructor = builderConstructor;
            }
            builder = cachedBuilderConstructor.newInstance();
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
        List<GetterSetter> cachedBuilderMethods = BUILDER_METHOD_CACHE.get(source.getClass());
        if (cachedBuilderMethods == null) {
            cachedBuilderMethods = new java.util.ArrayList<>();
            for (final Method targetMethod : target.getClass().getMethods()) {
                if (isSetterMethod(targetMethod)) {
                    final Optional<Method> getterMethod = getGetterForSetter(targetMethod, source.getClass());
                    if (getterMethod.isPresent()) {
                        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                            getterMethod.get().setAccessible(true);
                            return null;
                        });
                        cachedBuilderMethods.add(new GetterSetter(getterMethod.get(), targetMethod));
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
            BUILDER_METHOD_CACHE.put(source.getClass(), cachedBuilderMethods);
        }
        for (final GetterSetter getterSetter : cachedBuilderMethods) {
            try {
                getterSetter.transfer(source, target);
            } catch (final IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
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
        final List<ConstraintViolation> violations = Lists.newArrayList();
        validate(violations);
        if (!violations.isEmpty()) {
            throw new ConstraintsViolatedException(violations);
        }
        return construct();
    }

    /**
     * Validate this <code>Builder</code> instance.
     *
     * @param violations <code>List</code> of <code>ConstraintViolation</code>
     * instances to populate.
     */
    protected void validate(final List<ConstraintViolation> violations) {
        violations.addAll(VALIDATOR.validate(this));
    }

    /**
     * Protected method to construct the target class reflectively from the
     * specified type by passing its constructor an instance of this builder.
     *
     * @return Instance of target class created from this builder.
     */
    protected T construct() {
        if (_targetConstructor.isPresent()) {
            return _targetConstructor.get().apply(this);
        }
        try {
            final Constructor<? extends T> constructor = _targetClass.get().getDeclaredConstructor(this.getClass());
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
     * @param <B> The <code>Builder</code> type.
     * @param targetConstructor The constructor for the concrete type to be created by this builder.
     */
    @SuppressWarnings("unchecked")
    protected <B extends Builder<T>> OvalBuilder(final Function<B, T> targetConstructor) {
        _targetClass = Optional.empty();
        _targetConstructor = Optional.of((Function<Builder<T>, T>) targetConstructor);
    }

    /**
     * Protected constructor for subclasses.
     *
     * @param targetClass The concrete type to be created by this builder.
     * @deprecated For performance reasons pass a constructor function reference instead.
     */
    @Deprecated
    protected OvalBuilder(final Class<? extends T> targetClass) {
        _targetClass = Optional.of(targetClass);
        _targetConstructor = Optional.empty();
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

    private final Optional<Class<? extends T>> _targetClass;
    private final Optional<Function<Builder<T>, T>> _targetConstructor;

    private static final Validator VALIDATOR = new Validator();
    private static final Map<Class<?>, Constructor<? extends Builder<?>>> BUILDER_CONSTRUCTOR_CACHE = Maps.newConcurrentMap();
    private static final Map<Class<?>, List<GetterSetter>> BUILDER_METHOD_CACHE = Maps.newConcurrentMap();
    private static final Logger LOGGER = LoggerFactory.getLogger(OvalBuilder.class);

    private static final String GETTER_IS_METHOD_PREFIX = "is";
    private static final String GETTER_GET_METHOD_PREFIX = "get";
    private static final String SETTER_METHOD_PREFIX = "set";
    private static final String UNABLE_TO_CONSTRUCT_TARGET_CLASS = "Unable to construct target class; target_class=%s";

    private static final class GetterSetter {

        GetterSetter(final Method getter, final Method setter) {
            _getter = getter;
            _setter = setter;
        }

        public void transfer(final Object from, final Object to) throws InvocationTargetException, IllegalAccessException {
            Object value = _getter.invoke(from);
            if (value instanceof Optional) {
                final Optional<?> optional = (Optional<?>) value;
                value = optional.orElse(null);
            } else if (value instanceof com.google.common.base.Optional) {
                final com.google.common.base.Optional<?> optional = (com.google.common.base.Optional<?>) value;
                value = optional.orNull();
            }
            _setter.invoke(to, value);
        }

        private final Method _getter;
        private final Method _setter;
    }
}

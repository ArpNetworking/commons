/**
 * Copyright 2017 Inscope Metrics Inc.
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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;

/**
 * This abstract builder allows concrete builder instances to be consumed in
 * an instance per thread basis to build any number of instances without
 * creating more builder instances than necessary.
 *
 * In fact, if the consuming thread does not context switch while executing
 * the consumer, then only up to one instance of each builder will exist
 * per thread. Consequently, it is recommended that asynchronous operations
 * be executed outside the consumer to ensure a minimum number of builder
 * instances are constructed.
 *
 * Finally, do not attempt to "steal" the builder instance from the consumer.
 * Any use of the builder instance supplied to the consumer outside the
 * consumer will result in undefined behavior.
 *
 * All concrete subclasses must implement a public no-args constructor.
 *
 * @param <T> The type of object created by the builder.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 */
public abstract class ThreadLocalBuilder<T> extends OvalBuilder<T> {

    /**
     * Consume an instance of the provided builder class after resetting it and
     * return the instance resulting from building the builder.
     *
     * This version infers types by linking the builder class to the return
     * type. However, due to type-erasure Java {@code Class} instances do not
     * capture generic parameters and using this method will result in compiler
     * warnings when {@code <T>} is itself generic (e.g. {@code List<String>}.
     * In these cases you should use {@code buildGeneric} instead.
     *
     * @param <T> The type of object created by the builder.
     * @param <B> The type of the  builder used.
     * @param threadLocalBuilderClass The {@code Class<>} instance for the builder to use.
     * @param builderConsumer The {@code Consumer} for the builder instance.
     * @return The instance of {@code <T>} build from the builder after consuming it.
     */
    public static <T, B extends ThreadLocalBuilder<T>> T build(
            final Class<B> threadLocalBuilderClass,
            final Consumer<B> builderConsumer) {
        return buildGeneric(threadLocalBuilderClass, builderConsumer);
    }

    /**
     * Consume an instance of the provided builder class after resetting it and
     * return the instance resulting from building the builder.
     *
     * This version does not infer types and the return type and builder type
     * must be specified by the caller. This decouples {@code <T>} from
     * {@code <B>} allowing the return type {@code <T>} to be parameterized.
     * This is necessary due to type-erasure where Java {@code Class} instances
     * do not capture generic parameters. If {@code <T>} is not parameterized
     * then you should use {@code build} instead.
     *
     * @param <T> The type of object created by the builder.
     * @param <B> The type of the  builder used.
     * @param threadLocalBuilderClass The {@code Class<>} instance for the builder to use.
     * @param builderConsumer The {@code Consumer} for the builder instance.
     * @return The instance of {@code <T>} build from the builder after consuming it.
     */
    @SuppressWarnings("rawtypes")
    public static <T, B extends ThreadLocalBuilder<T>> T buildGeneric(
            final Class<? extends ThreadLocalBuilder> threadLocalBuilderClass,
            final Consumer<B> builderConsumer) {

        // Look-up the thread local builder's instance queue
        @SuppressWarnings("unchecked")
        final Class<? extends ThreadLocalBuilder<?>> genericThreadLocalBuilderClass =
                (Class<? extends ThreadLocalBuilder<?>>) threadLocalBuilderClass;
        final Queue<ThreadLocalBuilder<?>> instanceQueue = THREAD_LOCAL_BUILDERS_BY_TYPE.get().computeIfAbsent(
                genericThreadLocalBuilderClass,
                key -> new LinkedList<>());

        // Pull an instance from the queue or create one if one does not exist
        @Nullable ThreadLocalBuilder<?> threadLocalBuilder = instanceQueue.poll();
        if (threadLocalBuilder == null) {
            try {
                threadLocalBuilder = threadLocalBuilderClass.newInstance();
            } catch (final IllegalAccessException | InstantiationException e) {
                throw new RuntimeException("Cannot instantiate builder: " + threadLocalBuilderClass.getName(), e);
            }
        }

        // Reset the builder instance
        threadLocalBuilder.reset();

        try {
            // Consume the instance and build
            @SuppressWarnings("unchecked")
            final B castThreadLocalBuilder = (B) threadLocalBuilder;
            builderConsumer.accept(castThreadLocalBuilder);
            return castThreadLocalBuilder.build();
        } finally {
            // Return the builder instance to the queue
            instanceQueue.add(threadLocalBuilder);
        }
    }

    /**
     * Reset the builder state. This method will be invoked after construction
     * and also before any re-use of the builder instance.
     */
    protected abstract void reset();

    /**
     * Protected constructor for subclasses.
     *
     * @param <B> The <code>Builder</code> type.
     * @param targetConstructor The constructor for the concrete type to be created by this builder.
     */
    protected <B extends Builder<T>> ThreadLocalBuilder(final Function<B, T> targetConstructor) {
        super(targetConstructor);
    }

    private static final ThreadLocal<Map<Class<? extends ThreadLocalBuilder<?>>, Queue<ThreadLocalBuilder<?>>>>
            THREAD_LOCAL_BUILDERS_BY_TYPE = ThreadLocal.withInitial(HashMap::new);
}

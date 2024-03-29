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

import com.arpnetworking.commons.jackson.databind.module.BuilderModule;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Function;

/**
 * Create a "standard" {@link ObjectMapper} instance.
 *
 * Dependencies:
 * <ul>
 *     <li>com.fasterxml.jackson.core:jackson-core</li>
 *     <li>com.fasterxml.jackson.core:jackson-databind</li>
 *     <li>com.fasterxml.jackson.datatype:jackson-datatype-guava (Optional)</li>
 *     <li>com.google.guava:guava (Optional; only required if above is used)</li>
 *     <li>com.fasterxml.jackson.datatype:jackson-datatype-jdk8 (Optional)</li>
 *     <li>com.fasterxml.jackson.datatype:jackson-datatype-jsr310 (Optional)</li>
 *     <li>cglib:cglib (3.1+; transitively through ImmutableObjectMapper)</li>
 * </ul>
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
public final class ObjectMapperFactory {

    /**
     * Create a new {@link ObjectMapper} configured with standard
     * settings. New instances can be safely customized by clients.
     *
     * @return New mutable {@link ObjectMapper} instance.
     */
    public static ObjectMapper createInstance() {
        return createInstance(createJsonFactory());
    }

    /**
     * Create a new {@link ObjectMapper} configured with standard
     * settings. New instances can be safely customized by clients.
     *
     * @param jsonFactory Instance of {@link JsonFactory}.
     * @return New mutable {@link ObjectMapper} instance.
     */
    public static ObjectMapper createInstance(final JsonFactory jsonFactory) {
        return createModifiableObjectMapper(new ObjectMapper(jsonFactory));
    }

    /**
     * Get {@link ObjectMapper} instance configured with standard
     * settings. These instances are considered shared and are immutable.
     *
     * @return Shared immutable {@link ObjectMapper} instance.
     */
    @SuppressFBWarnings("MS_EXPOSE_REP")
    public static ObjectMapper getInstance() {
        return UNMODIFIABLE_OBJECT_MAPPER;
    }

    private static JsonFactory createJsonFactory() {
        return JsonFactory.builder()
                .enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION)
                .build();
    }

    private static ObjectMapper createModifiableObjectMapper() {
        return createModifiableObjectMapper(new ObjectMapper(createJsonFactory()));
    }

    /* package private */ static ObjectMapper createModifiableObjectMapper(final ObjectMapper objectMapper) {
        objectMapper.registerModule(new BuilderModule());
        registerModule(objectMapper, "com.fasterxml.jackson.datatype.guava.GuavaModule");
        registerModule(objectMapper, "com.fasterxml.jackson.datatype.jdk8.Jdk8Module");
        registerModule(objectMapper, "com.fasterxml.jackson.datatype.jsr310.JavaTimeModule");
        registerAdditionalModules(objectMapper, System::getProperty);
        objectMapper.configure(DeserializationFeature.FAIL_ON_TRAILING_TOKENS, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false);
        objectMapper.setDateFormat(new StdDateFormat());
        return objectMapper;
    }

    /* package private */ static void registerAdditionalModules(
            final ObjectMapper objectMapper,
            final Function<String, String> propertyAccessor) {
        final String moduleClassNames = propertyAccessor.apply("commons.object-mapper-additional-module-class-names");
        if (moduleClassNames != null) {
            for (final String moduleClassName : moduleClassNames.split(",")) {
                registerModule(objectMapper, moduleClassName);
            }
        }
    }

    @SuppressFBWarnings("REC_CATCH_EXCEPTION")
    /* package private */ static void registerModule(final ObjectMapper objectMapper, final String className) {
        final Optional<Class<? extends Module>> moduleClass = getClass(className);
        if (moduleClass.isPresent()) {
            try {
                final Module module = moduleClass.get().getDeclaredConstructor().newInstance();
                objectMapper.registerModule(module);
                // CHECKSTYLE.OFF: IllegalCatch - Catch any exceptions thrown by reflection or the module constructor.
            } catch (final Exception e) {
                // CHECKSTYLE.ON: IllegalCatch
                LOGGER.warn(
                        String.format(
                                "Unable to instantiate module; module=%s",
                                moduleClass.get()),
                        e);
            }
        }
    }

    /* package private */ static <T> Optional<Class<? extends T>> getClass(final String className) {
        try {
            @SuppressWarnings("unchecked")
            final Optional<Class<? extends T>> clazz = Optional.<Class<? extends T>>of((Class<T>) Class.forName(className));
            return clazz;
        } catch (final ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    private ObjectMapperFactory() {}

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectMapperFactory.class);
    private static final ObjectMapper UNMODIFIABLE_OBJECT_MAPPER = ImmutableObjectMapper.of(
            createModifiableObjectMapper());
}

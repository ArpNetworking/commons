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
package com.arpnetworking.commons.jackson.databind;

import com.arpnetworking.commons.jackson.databind.module.BuilderModule;
import com.arpnetworking.steno.Logger;
import com.arpnetworking.steno.LoggerFactory;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;

import java.util.Optional;
import java.util.function.Function;

/**
 * Create a "standard" <code>ObjectMapper</code> instance.
 *
 * Dependencies:
 * <ul>
 *     <li>com.fasterxml.jackson.core:jackson-core</li>
 *     <li>com.fasterxml.jackson.core:jackson-databind</li>
 *     <li>com.fasterxml.jackson.datatype:jackson-datatype-guava (Optional)</li>
 *     <li>com.fasterxml.jackson.datatype:jackson-datatype-jdk8 (Optional)</li>
 *     <li>com.fasterxml.jackson.datatype:jackson-datatype-joda (Optional)</li>
 *     <li>cglib:cglib (3.1+; transitively through ImmutableObjectMapper)</li>
 * </ul>
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 */
public final class ObjectMapperFactory {

    /**
     * Create a new <code>ObjectMapper</code> configured with standard
     * settings. New instances can be safely customized by clients.
     *
     * @return New mutable <code>ObjectMapper</code> instance.
     */
    public static ObjectMapper createInstance() {
        return createModifiableObjectMapper();
    }

    /**
     * Create a new <code>ObjectMapper</code> configured with standard
     * settings. New instances can be safely customized by clients.
     *
     * @param jsonFactory Instance of <code>JsonFactory</code>.
     * @return New mutable <code>ObjectMapper</code> instance.
     */
    public static ObjectMapper createInstance(final JsonFactory jsonFactory) {
        return createModifiableObjectMapper(new ObjectMapper(jsonFactory));
    }

    /**
     * Get <code>ObjectMapper</code> instance configured with standard
     * settings. These instances are considered shared and are immutable.
     *
     * @return Shared immutable <code>ObjectMapper</code> instance.
     */
    public static ObjectMapper getInstance() {
        return UNMODIFIABLE_OBJECT_MAPPER;
    }

    private static ObjectMapper createModifiableObjectMapper() {
        return createModifiableObjectMapper(new ObjectMapper());
    }

    /* package private */ static ObjectMapper createModifiableObjectMapper(final ObjectMapper objectMapper) {
        objectMapper.registerModule(new BuilderModule());
        registerModule(objectMapper, "com.fasterxml.jackson.datatype.guava.GuavaModule");
        registerModule(objectMapper, "com.fasterxml.jackson.datatype.jdk8.Jdk8Module");
        registerModule(objectMapper, "com.fasterxml.jackson.datatype.joda.JodaModule");
        registerAdditionalModules(objectMapper, System::getProperty);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false);
        objectMapper.setDateFormat(new ISO8601DateFormat());
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

    /* package private */ static void registerModule(final ObjectMapper objectMapper, final String className) {
        final Optional<Class<? extends Module>> moduleClass = getClass(className);
        if (moduleClass.isPresent()) {
            try {
                final Module module = moduleClass.get().newInstance();
                objectMapper.registerModule(module);
                // CHECKSTYLE.OFF: IllegalCatch - Catch any exceptions thrown by reflection or the module constructor.
            } catch (final Exception e) {
                // CHECKSTYLE.ON: IllegalCatch
                LOGGER.warn()
                        .setEvent("ObjectMapperFactory")
                        .setMessage("Unable to instantiate module")
                        .addData("module", moduleClass.get())
                        .setThrowable(e)
                        .log();
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

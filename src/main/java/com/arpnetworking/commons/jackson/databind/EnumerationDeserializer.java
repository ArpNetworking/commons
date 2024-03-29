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

import com.arpnetworking.commons.jackson.databind.exceptions.EnumerationNotFoundException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.Optional;

/**
 * Custom deserializer for Enums. The deserialization occurs based on the
 * deserialization strategy specified on an {@link EnumerationDeserializerStrategy}.
 *
 * Dependencies:
 * <ul>
 *     <li>com.fasterxml.jackson.core:jackson-core</li>
 *     <li>com.fasterxml.jackson.core:jackson-databind</li>
 * </ul>
 *
 * @param <T> The class/type of enumeration.
 *
 * @author Carlos Indo (carlos at groupon dot com)
 * @see EnumerationDeserializerStrategy
 */
public final class EnumerationDeserializer<T extends Enum<T>> extends JsonDeserializer<T> {

    /**
     * Creates a new instance of {@link JsonDeserializer}.
     *
     * @param <T> The type of enumeration.
     * @param enumClass The {@link Class} for the enumeration type.
     * @param strategy The {@link EnumerationDeserializerStrategy} instance.
     * @return New instance of {@link JsonDeserializer}.
     */
    public static <T extends Enum<T>> JsonDeserializer<T> newInstance(
            final Class<T> enumClass,
            final EnumerationDeserializerStrategy<T> strategy) {
        return new EnumerationDeserializer<T>(enumClass, strategy);
    }

    @Override
    public T deserialize(final JsonParser jp, final DeserializationContext ctxt)
            throws IOException {
        // NOTE: By contract with JsonDeserializer the stringValue cannot be null
        final String stringValue = jp.getValueAsString();
        final Optional<T> value = _strategy.toEnum(_enumClass, stringValue);
        if (value.isEmpty()) {
            throw new EnumerationNotFoundException(
                    stringValue,
                    _enumClass,
                    jp.currentLocation());
        }
        return value.get();
    }

    private EnumerationDeserializer(final Class<T> enumClass, final EnumerationDeserializerStrategy<T> strategy) {
        _strategy = strategy;
        _enumClass = enumClass;
    }

    private final EnumerationDeserializerStrategy<T> _strategy;
    private final Class<T> _enumClass;
}

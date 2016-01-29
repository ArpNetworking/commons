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
package com.arpnetworking.commons.jackson.databind.exceptions;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Exception when you cannot find an Enumeration value of a particular type.
 *
 * Dependencies:
 * <ul>
 *     <li>com.fasterxml.jackson.core:jackson-core</li>
 * </ul>
 *
 * @author Carlos Indo (carlos at groupon dot com)
 * @author Ville Koskela (vkoskela at groupon dot com)
 */
public final class EnumerationNotFoundException extends JsonProcessingException {

    /**
     * Creates an <code>EnumerationNotFoundException</code>.
     * A predefined message is created based on the value not found and
     * the enum class.
     *
     * @param <E> the enum type
     * @param value the value of the enum not found.
     * @param enumClass the enum type class.
     * @param location the location in the json where this occurred.
     * @param cause the cause of this exception.
     */
    public <E extends Enum<E>> EnumerationNotFoundException(
            final String value,
            final Class<E> enumClass,
            final JsonLocation location,
            final Throwable cause) {
        super(
                String.format(
                        "Enumeration not found; enumClass=%s, value=%s",
                        enumClass,
                        value),
                location,
                cause);
    }

    /**
     * Creates an <code>EnumerationNotFoundException</code>.
     * A predefined message is created based on the value not found and
     * the enum class.
     *
     * @param <E> the enum type
     * @param value the value of the enum not found.
     * @param enumClass the enum type class.
     * @param location the location in the json where this occurred.
     */
    public <E extends Enum<E>> EnumerationNotFoundException(
            final String value,
            final Class<E> enumClass,
            final JsonLocation location) {
        this(value, enumClass, location, null);
    }

    private static final long serialVersionUID = 1447758748842501027L;
}

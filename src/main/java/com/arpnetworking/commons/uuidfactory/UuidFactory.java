/*
 * Copyright 2016 Groupon.com
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
package com.arpnetworking.commons.uuidfactory;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Interface for classes that crate universally unique identifiers (UUIDs).
 * This interface does not specify the manner or type of identifier that is
 * created and implementations are not restricted to any particular method
 * or type of identifier. However, implementations are expected to document
 * the characteristics and method of generation in particular as it pertains
 * to the uniquness of the identifier.
 *
 * Dependencies:
 * <ul>
 *     <li><i>None</i></li>
 * </ul>
 *
 * @author Matthew Hayter (mhayter at groupon dot com)
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
public interface UuidFactory extends Supplier<UUID> {

    /**
     * Create a new {@code UUID}.
     *
     * @return A new {@code UUID}.
     */
    UUID create();

    @Override
    default UUID get() {
        return create();
    }
}

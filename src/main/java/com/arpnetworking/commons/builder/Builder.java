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
package com.arpnetworking.commons.builder;

/**
 * Generic builder interface.
 *
 * Dependencies:
 * <ul>
 *     <li><i>None</i></li>
 * </ul>
 *
 * @param <T> The type of the object created by this builder.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
public interface Builder<T> {

    /**
     * Create a new instance of {@code <T>} based on the attributes currently
     * set on this builder. The method will throw an exception if a valid
     * instance of {@code <T>} cannot be constructed.
     *
     * @return New instance of {@code <T>}.
     */
    T build();
}

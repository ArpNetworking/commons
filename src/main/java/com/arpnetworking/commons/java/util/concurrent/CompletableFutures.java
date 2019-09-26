/*
 * Copyright 2018 Inscope Metrics, Inc.
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
package com.arpnetworking.commons.java.util.concurrent;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * Helper methods for Java's CompletableFuture.
 *
 * Dependencies:
 * <i>None</i>
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
public final class CompletableFutures {

    /**
     * Variant of {@code CompletableFuture.allOf(CompletableFuture<?>...)}
     * that accepts a list of {@code CompletableFuture<T>} instead of an array.
     * Encapsulates the array creation and associated unsafe casting and raw
     * types warnings.
     *
     * @param futures the CompletableFutures
     * @return a new CompletableFuture that is completed when all of the
     * given CompletableFutures complete
     */
    public static CompletableFuture<?> allOf(final Collection<?> futures) {
        @SuppressWarnings({"unchecked", "rawtypes"})
        final CompletableFuture[] futuresArray = futures.toArray(new CompletableFuture[0]);
        return CompletableFuture.allOf(futuresArray);
    }

    private CompletableFutures() {}
}

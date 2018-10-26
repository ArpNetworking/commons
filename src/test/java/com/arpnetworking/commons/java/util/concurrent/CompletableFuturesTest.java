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

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Tests for the CompletableFutures class.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 */
public final class CompletableFuturesTest {

    @Test
    public void testEmpty() throws ExecutionException, InterruptedException {
        final List<CompletableFuture<?>> futures = new ArrayList<>();
        final CompletableFuture<?> allFuture = CompletableFutures.allOf(futures);
        Assert.assertNull(allFuture.get());
        Assert.assertFalse(allFuture.isCompletedExceptionally());
    }

    @Test
    public void testSuccess() throws ExecutionException, InterruptedException {
        final List<CompletableFuture<?>> futures = new ArrayList<>();
        futures.add(completed("foo"));
        futures.add(completed("bar"));
        final CompletableFuture<?> allFuture = CompletableFutures.allOf(futures);
        Assert.assertNull(allFuture.get());
        Assert.assertFalse(allFuture.isCompletedExceptionally());
    }

    @Test
    public void testFailure() throws InterruptedException {
        final List<CompletableFuture<?>> futures = new ArrayList<>();
        futures.add(failed("foo"));
        futures.add(failed("bar"));
        final CompletableFuture<?> allFuture = CompletableFutures.allOf(futures);
        Assert.assertTrue(allFuture.isCompletedExceptionally());
        try {
            allFuture.get();
            Assert.fail("Expected exception not thrown");
        } catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
            Assert.assertTrue(cause.getMessage().equals("foo") || cause.getMessage().equals("bar"));
        }
    }

    @Test
    public void testMixed() throws InterruptedException {
        final List<CompletableFuture<?>> futures = new ArrayList<>();
        futures.add(completed("foo"));
        futures.add(failed("bar"));
        final CompletableFuture<?> allFuture = CompletableFutures.allOf(futures);
        Assert.assertTrue(allFuture.isCompletedExceptionally());
        try {
            allFuture.get();
            Assert.fail("Expected exception not thrown");
        } catch (final ExecutionException e) {
            Assert.assertTrue(e.getCause().getMessage().equals("bar"));
        }
    }

    private static CompletableFuture<String> completed(final String s) {
        final CompletableFuture<String> future = new CompletableFuture<>();
        future.complete(s);
        return future;
    }

    private static CompletableFuture<String> failed(final String s) {
        final CompletableFuture<String> future = new CompletableFuture<>();
        future.completeExceptionally(new Exception(s));
        return future;
    }
}

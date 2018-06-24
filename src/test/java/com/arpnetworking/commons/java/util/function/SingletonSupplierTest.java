/*
 * Copyright 2017 Inscope Metrics, Inc.
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
package com.arpnetworking.commons.java.util.function;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Tests for the SingletonSupplier class.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 */
public final class SingletonSupplierTest {

    @Test
    public void testSingleton() {
        final AtomicInteger value = new AtomicInteger(0);
        final Supplier<Integer> supplier = new SingletonSupplier<>(value::incrementAndGet);

        Assert.assertEquals(0, value.get());
        Assert.assertEquals(1, supplier.get().intValue());
        Assert.assertEquals(1, value.get());
        Assert.assertEquals(1, supplier.get().intValue());
        Assert.assertEquals(1, value.get());
    }

    @Test
    public void testDoubleCheck() {
        final AtomicBoolean success = new AtomicBoolean(true);
        final Semaphore semaphoreA = new Semaphore(0);
        final ReentrantLock lock = new ReentrantLock();
        final AtomicInteger value = new AtomicInteger(0);
        final Supplier<Integer> supplier = new SingletonSupplier<>(
                () -> {
                    try {
                        semaphoreA.acquire();
                    } catch (final InterruptedException e) {
                        success.set(false);
                    }
                    return value.incrementAndGet();
                },
                lock);

        final ExecutorService executorService = new ThreadPoolExecutor(
                2, 2, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1));

        executorService.submit(() -> {
            // NOTE: This is not ideal as getQueueLength returns only an estimate
            while (!lock.isLocked() && lock.getQueueLength() != 1) {
                try {
                    Thread.sleep(100);
                } catch (final InterruptedException e) {
                    success.set(false);
                }
            }
            semaphoreA.release(2);
        });

        executorService.submit(() -> {
            try {
                Assert.assertEquals(0, value.get());
                Assert.assertEquals(1, supplier.get().intValue());
                // CHECKSTYLE.OFF: IllegalCatch - This is what Assert throws
            } catch (final Throwable e) {
                // CHECKSTYLE.ON: IllegalCatch
                success.set(false);
            }
        });

        Assert.assertEquals(0, value.get());
        Assert.assertEquals(1, supplier.get().intValue());
        Assert.assertEquals(1, value.get());
        Assert.assertEquals(1, supplier.get().intValue());
        Assert.assertEquals(1, value.get());
        Assert.assertTrue(success.get());
    }
}

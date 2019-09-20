/*
 * Copyright 2016 Inscope Metrics Inc.
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

import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Tests for the DefaultUuidFactory class.
 *
 * NOTE: due to code/branch coverage, we need to make sure that we run tests with the EGD property both set and unset.
 * The mutex allows us to make sure that the property is set for one, but not the other.  Unfortunately,
 * this means we must also restore the original state of the EGD property to avoid a race.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
public class ThreadLocalSecureRandomUuidFactoryTest {
    @Test
    public void test() throws InterruptedException {
        try {
            PROPERTY_SET_MUTEX.acquire();
            final UuidFactory uuidFactory = new ThreadLocalSecureRandomUuidFactory();
            Assert.assertNotEquals(uuidFactory.create(), uuidFactory.create());
            Assert.assertNotEquals(uuidFactory.get(), uuidFactory.get());

            final UUID uuid = uuidFactory.create();
            Assert.assertEquals(4, uuid.version());
            Assert.assertEquals(2, uuid.variant());
        } finally {
            PROPERTY_SET_MUTEX.release();
        }
    }

    @Test
    public void testWithURandom() throws InterruptedException {
        // Run this test in a separate thread to ensure a new SecureRandom
        // thread local is created.
        try {
            PROPERTY_SET_MUTEX.acquire();
            final String oldValue = System.getProperty(EGD_PROPERTY);
            System.setProperty(EGD_PROPERTY, "file:/dev/./urandom");
            final UuidFactory uuidFactory = new ThreadLocalSecureRandomUuidFactory();
            final ExecutorService executor = new ThreadPoolExecutor(
                    1,  // core threads
                    1,  // max threads
                    1,  // keep alive
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(1));
            executor.submit((Runnable) () -> Assert.assertNotEquals(uuidFactory.create(), uuidFactory.create()));
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.SECONDS);

            // Restore the security property
            if (oldValue != null) {
                System.setProperty(EGD_PROPERTY, oldValue);
            } else {
                System.clearProperty(EGD_PROPERTY);
            }
        } finally {
            PROPERTY_SET_MUTEX.release();
        }
    }

    @Test
    public void testToLong() {
        final ByteBuffer bytes = ByteBuffer.allocate(5 * 8);
        bytes.putLong(Long.MIN_VALUE);
        bytes.putLong(-1L);
        bytes.putLong(0L);
        bytes.putLong(1L);
        bytes.putLong(Long.MAX_VALUE);

        Assert.assertEquals(Long.MIN_VALUE, ThreadLocalSecureRandomUuidFactory.toLong(bytes.array(), 0));
        Assert.assertEquals(-1L, ThreadLocalSecureRandomUuidFactory.toLong(bytes.array(), 8));
        Assert.assertEquals(0L, ThreadLocalSecureRandomUuidFactory.toLong(bytes.array(), 16));
        Assert.assertEquals(1L, ThreadLocalSecureRandomUuidFactory.toLong(bytes.array(), 24));
        Assert.assertEquals(Long.MAX_VALUE, ThreadLocalSecureRandomUuidFactory.toLong(bytes.array(), 32));
    }

    @Test
    public void testToInt() {
        final ByteBuffer bytes = ByteBuffer.allocate(5 * 4);
        bytes.putInt(Integer.MIN_VALUE);
        bytes.putInt(-1);
        bytes.putInt(0);
        bytes.putInt(1);
        bytes.putInt(Integer.MAX_VALUE);

        Assert.assertEquals(Integer.MIN_VALUE, ThreadLocalSecureRandomUuidFactory.toInt(bytes.array(), 0));
        Assert.assertEquals(-1, ThreadLocalSecureRandomUuidFactory.toInt(bytes.array(), 4));
        Assert.assertEquals(0, ThreadLocalSecureRandomUuidFactory.toInt(bytes.array(), 8));
        Assert.assertEquals(1, ThreadLocalSecureRandomUuidFactory.toInt(bytes.array(), 12));
        Assert.assertEquals(Integer.MAX_VALUE, ThreadLocalSecureRandomUuidFactory.toInt(bytes.array(), 16));
    }

    private static final Semaphore PROPERTY_SET_MUTEX = new Semaphore(1);
    private static final String EGD_PROPERTY = "java.security.egd";

}

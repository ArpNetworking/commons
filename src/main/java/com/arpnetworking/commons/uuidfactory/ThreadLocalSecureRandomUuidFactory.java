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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * Generates a new {@code UUID} using thread local {@code SecureRandom}. Based
 * heavily on implementation of the java-uuid-generator (JUG):
 *
 * https://github.com/cowtowncoder/java-uuid-generator
 *
 * Dependencies:
 * <ul>
 *     <li><i>None</i></li>
 * </ul>
 *
 * Performance:
 *
 * 1) Default JVM arguments.
 *
 * DefaultUuidFactory: 338,516 tps
 * ThreadLocalSecureRandomUuidFactory: 334,730 tps
 * SplittableRandomUuidFactory: 22,211,429 tps
 *
 * 2) Using urandom with:  {@code -Djava.security.egd=file:/dev/./urandom}
 *
 * DefaultUuidFactory: 1,312,707 tps
 * ThreadLocalSecureRandomUuidFactory: 4,854,940
 * SplittableRandomUuidFactory: 22,760,567 tps (no change)
 *
 * These are adhoc test results conducted on a 1.7 Ghz Core i7 with 8 GB of
 * 1600 Mhz DDR3 and a 256 GB SSD running Mac OS 10.12.2 under Oracle JRE
 * version 1.8.0_92-b14.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
@SuppressFBWarnings("DMI_RANDOM_USED_ONLY_ONCE")
public final class ThreadLocalSecureRandomUuidFactory implements UuidFactory {

    @Override
    public UUID create() {
        // As stated in JUG documentation the nextBytes call on SecureRandom is
        // noticeably faster than calling nextLong twice. This was confirmed
        // through adhoc performance testing.
        final byte[] buffer = new byte[16];
        SECURE_RANDOM_THREAD_LOCAL.get().nextBytes(buffer);

        long msb = toLong(buffer, 0);
        msb &= 0xffffffffffff0fffL;
        msb |= 0x0000000000004000L;

        long lsb = toLong(buffer, 1);
        lsb &= 0x3fffffffffffffffL;
        lsb |= 0x8000000000000000L;

        return new UUID(msb, lsb);
    }

    /* package private */ static long toLong(final byte[] buffer, final int offset) {
        final long value1 = toInt(buffer, offset);
        final long value2 = toInt(buffer, offset + 4);
        return (value1 << 32) + ((value2 << 32) >>> 32);
    }

    /* package private */ static long toInt(final byte[] buffer, final int initialOffset) {
        int offset = initialOffset;
        return (buffer[offset] << 24)
                + ((buffer[++offset] & 0xFF) << 16)
                + ((buffer[++offset] & 0xFF) << 8)
                + (buffer[++offset] & 0xFF);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadLocalSecureRandomUuidFactory.class);

    private static final ThreadLocal<SecureRandom> SECURE_RANDOM_THREAD_LOCAL = new ThreadLocal<>() {
        @Override
        protected SecureRandom initialValue() {
            if (!URANDOM.equals(System.getProperty(KEY))) {
                LOGGER.warn(
                        String.format(
                                "Using ThreadLocalSecureRandomUuidFactory without -D%s=%s",
                                KEY,
                                URANDOM
                        ));
            }
            final SecureRandom secureRandom = new SecureRandom();

            // Force seeding; this may block until sufficient entropy exists
            secureRandom.nextBytes(new byte[1]);

            return secureRandom;
        }

        private static final String KEY = "java.security.egd";
        private static final String URANDOM = "file:/dev/./urandom";
    };
}

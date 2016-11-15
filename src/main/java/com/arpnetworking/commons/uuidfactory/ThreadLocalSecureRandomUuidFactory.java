/**
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

import com.arpnetworking.steno.Logger;
import com.arpnetworking.steno.LoggerFactory;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * Generates a new {@code UUID} using thread local {@code SecureRandom}. Based
 * heavily on implementation of the java-uuid-generator (JUG):
 *
 * https://github.com/cowtowncoder/java-uuid-generator
 *
 * And the implementation of {@code java.util.UUID}.
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
 *
 * 2) Using urandom with:  {@code -Djava.security.egd=file:/dev/./urandom}
 *
 * DefaultUuidFactory: 1,312,707 tps
 * ThreadLocalSecureRandomUuidFactory: 4,854,940
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 */
public final class ThreadLocalSecureRandomUuidFactory implements UuidFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID create() {
        // As stated in JUG documentation the nextBytes call on SecureRandom is
        // noticeably faster than calling nextLong twice. This was confirmed
        // through adhoc performance testing.
        final byte[] buffer = new byte[16];
        SECURE_RANDOM_THREAD_LOCAL.get().nextBytes(buffer);
        // The following is logic from java.util.UUID:
        buffer[6]  &= 0x0f;  /* clear version        */
        buffer[6]  |= 0x40;  /* set to version 4     */
        buffer[8]  &= 0x3f;  /* clear variant        */
        buffer[8]  |= 0x80;  /* set to IETF variant  */
        final long msb = toLong(buffer, 0);
        final long lsb = toLong(buffer, 1);
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

    private static final ThreadLocal<SecureRandom> SECURE_RANDOM_THREAD_LOCAL = new ThreadLocal<SecureRandom>() {
        @Override
        protected SecureRandom initialValue() {
            if (!URANDOM.equals(System.getProperty(KEY))) {
                LOGGER.warn()
                        .setMessage(
                                String.format(
                                        "Using ThreadLocalSecureRandomUuidFactory without -D%s=%s",
                                        KEY,
                                        URANDOM))
                        .log();
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

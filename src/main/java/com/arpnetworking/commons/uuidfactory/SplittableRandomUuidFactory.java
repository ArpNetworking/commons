/*
 * Copyright 2017 Inscope Metrics, Inc
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

import java.util.SplittableRandom;
import java.util.UUID;

/**
 * Uses a {@code ThreadLocal} {@code SplittableRandom} to create type 4
 * random {@code UUID} instances.
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
 * @author Brandon Arp (brandon dot arp at inscopemetrics dot io)
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
public class SplittableRandomUuidFactory implements UuidFactory {

    @Override
    public UUID create() {
        final SplittableRandom random = _localRandom.get();
        long gMost = random.nextLong();
        gMost &= 0xffffffffffff0fffL;
        gMost |= 0x0000000000004000L;

        long gLeast = random.nextLong();
        gLeast &= 0x3fffffffffffffffL;
        gLeast |= 0x8000000000000000L;
        return new UUID(gMost, gLeast);
    }

    private final SplittableRandom _random = new SplittableRandom();
    private final ThreadLocal<SplittableRandom> _localRandom = ThreadLocal.withInitial(_random::split);
}

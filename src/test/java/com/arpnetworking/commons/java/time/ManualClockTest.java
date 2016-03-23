/**
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
package com.arpnetworking.commons.java.time;

import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

/**
 * Tests for the ManualClock class.
 *
 * @author Ville Koskela (vkoskela at groupon dot com)
 */
public class ManualClockTest {

    @Test
    public void testInstant() {
        final ManualClock manualClock = new ManualClock(Instant.ofEpochSecond(1458592075), Duration.ofSeconds(10), ZoneId.of("UTC"));
        Assert.assertEquals(Instant.ofEpochSecond(1458592075), manualClock.instant());
        Assert.assertEquals(Instant.ofEpochSecond(1458592075), manualClock.instant());
        Assert.assertEquals(Instant.ofEpochSecond(1458592075).toEpochMilli(), manualClock.millis());
    }

    @Test
    public void testTick() {
        final ManualClock manualClock = new ManualClock(Instant.ofEpochSecond(1458592075), Duration.ofSeconds(10), ZoneId.of("UTC"));
        Assert.assertEquals(Instant.ofEpochSecond(1458592075), manualClock.instant());
        manualClock.tick();
        Assert.assertEquals(Instant.ofEpochSecond(1458592085), manualClock.instant());
        Assert.assertEquals(Instant.ofEpochSecond(1458592085).toEpochMilli(), manualClock.millis());
    }

    @Test
    public void testZone() {
        final ManualClock manualClock = new ManualClock(Instant.ofEpochSecond(1458592075), Duration.ofSeconds(10), ZoneId.of("UTC"));
        Assert.assertEquals(ZoneId.of("UTC"), manualClock.getZone());
        final ManualClock otherZoneManualClock = manualClock.withZone(ZoneId.of("America/Los_Angeles"));
        Assert.assertEquals(ZoneId.of("UTC"), manualClock.getZone());
        Assert.assertEquals(ZoneId.of("America/Los_Angeles"), otherZoneManualClock.getZone());
    }
}

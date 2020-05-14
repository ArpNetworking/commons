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
package com.arpnetworking.commons.java.time;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

/**
 * Concrete implementation of {@link Clock} which returns the same
 * {@link Instant} until advanced manually by invoking the
 * {@link ManualClock#tick()} method upon which the time moves forward by the
 * amount specified to the constructor.
 *
 * Dependencies:
 * <ul>
 *     <li><i>None</i></li>
 * </ul>
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
public class ManualClock extends Clock {

    @Override
    public ZoneId getZone() {
        return _zone;
    }

    @Override
    public ManualClock withZone(final ZoneId zone) {
        return new ManualClock(_instant, _tickDuration, zone);
    }

    @Override
    public Instant instant() {
        return _instant;
    }

    /**
     * Move this {@link Clock} forward by a single tick duration.
     */
    public void tick() {
        _instant = _instant.plus(_tickDuration);
    }

    /**
     * Public constructor.
     *
     * @param initialTime The initial time returned by this clock.
     * @param tickDuration The duration incurred for each {@link ManualClock#tick()} invocation.
     * @param zone The {@link ZoneId} for the {@link Clock}.
     */
    public ManualClock(final Instant initialTime, final Duration tickDuration, final ZoneId zone) {
        _instant = initialTime;
        _tickDuration = tickDuration;
        _zone = zone;
    }

    private final ZoneId _zone;
    private final Duration _tickDuration;

    private Instant _instant;
}

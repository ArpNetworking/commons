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
package com.arpnetworking.commons.slf4j;

import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Rate limited {@code Logger} wrapper.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
public final class RateLimitedLogger {

    /**
     * Public constructor.
     *
     * @param name the name of the event being rate limited
     * @param logger the underlying slf4j {@code Logger} instance
     * @param duration the minimum duration between logged events
     */
    public RateLimitedLogger(
            final String name,
            final Logger logger,
            final Duration duration) {
        this(name, logger, duration, Clock.systemUTC());
    }

    /* package private */ RateLimitedLogger(
            final String name,
            final Logger logger,
            final Duration duration,
            final Clock clock) {
        _name = name;
        _logger = logger;
        _duration = duration;
        _clock = clock;
    }

    /**
     * Obtain a rate limited reference to the underlying logger. If the rate
     * has been exceeded then a no-op logger instance is returned and the
     * log message is effectively discarded. Discarded messages are tracked
     * and a count of discarded messages is output with the next successful
     * call to {@code getLogger}.
     *
     * @return either the underlying logger or a no-op logger
     */
    public Logger getLogger() {
        if (shouldLog()) {
            final int skipped = _skipped.getAndSet(0);
            final Instant lastLogTime = _lastLogTime.getAndSet(_clock.instant());
            if (skipped > 0) {
                _logger.info(
                        String.format(
                                "Skipped %d messages for '%s' since last getLogger at %s",
                                skipped,
                                _name,
                                lastLogTime));
            }
            return _logger;
        } else {
            return NOPLogger.NOP_LOGGER;
        }
    }

    private boolean shouldLog() {
        final Instant now = _clock.instant();
        if (_lastLogTime.get() != null) {
            if (!_lastLogTime.get().plus(_duration).isBefore(now)) {
                _skipped.incrementAndGet();
                return false;
            }
        }
        return true;
    }

    private final String _name;
    private final Logger _logger;
    private final Duration _duration;
    private final Clock _clock;
    private final AtomicReference<Instant> _lastLogTime = new AtomicReference<>();
    private final AtomicInteger _skipped = new AtomicInteger(0);
}

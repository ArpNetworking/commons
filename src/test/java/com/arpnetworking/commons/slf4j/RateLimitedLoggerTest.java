/**
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

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.hamcrest.MockitoHamcrest;
import org.slf4j.Logger;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

/**
 * Tests for {@link RateLimitedLogger}.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 */
public final class RateLimitedLoggerTest {

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testConstructor() {
        final RateLimitedLogger rateLimitedLogger = new RateLimitedLogger(
                "Test",
                _logger,
                Duration.ofSeconds(30));
        Assert.assertNotNull(rateLimitedLogger);
    }

    @Test
    public void testRateLimitedLoggerSkipping() {
        final RateLimitedLogger rateLimitedLogger = new RateLimitedLogger(
                "testRateLimitedLoggerSkippingLogger",
                _logger,
                Duration.ofSeconds(1),
                new TickingClock(Clock.systemUTC(), Duration.ofMillis(600)));

        rateLimitedLogger.getLogger().debug("Test1");
        Mockito.verify(_logger).debug("Test1");
        Mockito.verifyNoMoreInteractions(_logger);

        rateLimitedLogger.getLogger().debug("Test2");
        Mockito.verify(_logger, Mockito.never()).debug("Test2");
        Mockito.verifyNoMoreInteractions(_logger);

        rateLimitedLogger.getLogger().debug("Test3");
        Mockito.verify(_logger).debug("Test3");
        Mockito.verify(_logger).info(MockitoHamcrest.argThat(Matchers.matchesPattern(
                "Skipped 1 messages for 'testRateLimitedLoggerSkippingLogger' since last getLogger at .*")));
        Mockito.verifyNoMoreInteractions(_logger);
    }

    @Test
    public void testRateLimitedLoggerNoSkipping() {
        final RateLimitedLogger rateLimitedLogger = new RateLimitedLogger(
                "testRateLimitedLoggerNoSkippingLogger",
                _logger,
                Duration.ofSeconds(1),
                new TickingClock(Clock.systemUTC(), Duration.ofMillis(1200)));

        rateLimitedLogger.getLogger().debug("Test1");
        Mockito.verify(_logger).debug("Test1");
        Mockito.verifyNoMoreInteractions(_logger);

        rateLimitedLogger.getLogger().debug("Test2");
        Mockito.verify(_logger).debug("Test2");
        Mockito.verifyNoMoreInteractions(_logger);

        rateLimitedLogger.getLogger().debug("Test3");
        Mockito.verify(_logger).debug("Test3");
        Mockito.verifyNoMoreInteractions(_logger);
    }

    @Mock
    private Logger _logger;

    private static final class TickingClock extends Clock {

        /* package private */ TickingClock(final Clock clock, final Duration duration) {
            _clock = clock;
            _duration = duration;
            _time = _clock.instant();
        }

        @Override
        public ZoneId getZone() {
            return _clock.getZone();
        }

        @Override
        public Clock withZone(final ZoneId zone) {
            return new TickingClock(_clock.withZone(zone), _duration);
        }

        @Override
        public Instant instant() {
            final Instant instant = _time;
            _time = _time.plus(_duration);
            return instant;
        }

        private final Clock _clock;
        private final Duration _duration;
        private Instant _time;
    }
}

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

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.hamcrest.MockitoHamcrest;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

/**
 * Tests for {@link RateLimitedLogger}.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
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

        rateLimitedLogger.getEffectiveLogger().debug("Test1");
        Mockito.verify(_logger).debug("Test1");
        Mockito.verifyNoMoreInteractions(_logger);

        rateLimitedLogger.getEffectiveLogger().debug("Test2");
        Mockito.verify(_logger, Mockito.never()).debug("Test2");
        Mockito.verifyNoMoreInteractions(_logger);

        rateLimitedLogger.getEffectiveLogger().debug("Test3");
        Mockito.verify(_logger).debug("Test3");
        Mockito.verify(_logger).info(MockitoHamcrest.argThat(Matchers.matchesPattern(
                "Skipped 1 messages for 'testRateLimitedLoggerSkippingLogger' since last log at .*")));
        Mockito.verifyNoMoreInteractions(_logger);
    }

    @Test
    public void testRateLimitedLoggerNoSkipping() {
        final RateLimitedLogger rateLimitedLogger = new RateLimitedLogger(
                "testRateLimitedLoggerNoSkippingLogger",
                _logger,
                Duration.ofSeconds(1),
                new TickingClock(Clock.systemUTC(), Duration.ofMillis(1200)));

        rateLimitedLogger.getEffectiveLogger().debug("Test1");
        Mockito.verify(_logger).debug("Test1");
        Mockito.verifyNoMoreInteractions(_logger);

        rateLimitedLogger.getEffectiveLogger().debug("Test2");
        Mockito.verify(_logger).debug("Test2");
        Mockito.verifyNoMoreInteractions(_logger);

        rateLimitedLogger.getEffectiveLogger().debug("Test3");
        Mockito.verify(_logger).debug("Test3");
        Mockito.verifyNoMoreInteractions(_logger);
    }

    @Test
    public void testSlf4jPassThroughGetName() {
        final RateLimitedLogger rateLimitedLogger = new RateLimitedLogger(
                "testSlf4jPassThrough",
                _logger,
                Duration.ofSeconds(1),
                Clock.systemDefaultZone(),
                () -> true);

        // Trace
        rateLimitedLogger.getName();
        Mockito.verify(_logger).getName();
    }

    @Test
    public void testSlf4jPassThroughTrace() {
        final Throwable throwable = new NullPointerException("test exception");
        final RateLimitedLogger rateLimitedLogger = new RateLimitedLogger(
                "testSlf4jPassThrough",
                _logger,
                Duration.ofSeconds(1),
                Clock.systemDefaultZone(),
                () -> true);

        // Trace
        rateLimitedLogger.trace("1");
        Mockito.verify(_logger).trace("1");
        rateLimitedLogger.trace("2", "3");
        Mockito.verify(_logger).trace("2", "3");
        rateLimitedLogger.trace("4", "5", "6", "7");
        Mockito.verify(_logger).trace("4", "5", "6", "7");
        rateLimitedLogger.trace("8", "9", "10");
        Mockito.verify(_logger).trace("8", "9", "10");
        rateLimitedLogger.trace("11", throwable);
        Mockito.verify(_logger).trace("11", throwable);
        rateLimitedLogger.isTraceEnabled();
        Mockito.verify(_logger).isTraceEnabled();

        rateLimitedLogger.trace(_marker, "12");
        Mockito.verify(_logger).trace(_marker, "12");
        rateLimitedLogger.trace(_marker, "13", "14");
        Mockito.verify(_logger).trace(_marker, "13", "14");
        rateLimitedLogger.trace(_marker, "15", "16", "17", "18");
        Mockito.verify(_logger).trace(_marker, "15", "16", "17", "18");
        rateLimitedLogger.trace(_marker, "19", "20", "21");
        Mockito.verify(_logger).trace(_marker, "19", "20", "21");
        rateLimitedLogger.trace(_marker, "22", throwable);
        Mockito.verify(_logger).trace(_marker, "22", throwable);
        rateLimitedLogger.isTraceEnabled(_marker);
        Mockito.verify(_logger).isTraceEnabled(_marker);
    }

    @Test
    public void testSlf4jPassThroughDebug() {
        final Throwable throwable = new NullPointerException("test exception");
        final RateLimitedLogger rateLimitedLogger = new RateLimitedLogger(
                "testSlf4jPassThrough",
                _logger,
                Duration.ofSeconds(1),
                Clock.systemDefaultZone(),
                () -> true);

        // Debug
        rateLimitedLogger.debug("1");
        Mockito.verify(_logger).debug("1");
        rateLimitedLogger.debug("2", "3");
        Mockito.verify(_logger).debug("2", "3");
        rateLimitedLogger.debug("4", "5", "6", "7");
        Mockito.verify(_logger).debug("4", "5", "6", "7");
        rateLimitedLogger.debug("8", "9", "10");
        Mockito.verify(_logger).debug("8", "9", "10");
        rateLimitedLogger.debug("11", throwable);
        Mockito.verify(_logger).debug("11", throwable);
        rateLimitedLogger.isDebugEnabled();
        Mockito.verify(_logger).isDebugEnabled();

        rateLimitedLogger.debug(_marker, "12");
        Mockito.verify(_logger).debug(_marker, "12");
        rateLimitedLogger.debug(_marker, "13", "14");
        Mockito.verify(_logger).debug(_marker, "13", "14");
        rateLimitedLogger.debug(_marker, "15", "16", "17", "18");
        Mockito.verify(_logger).debug(_marker, "15", "16", "17", "18");
        rateLimitedLogger.debug(_marker, "19", "20", "21");
        Mockito.verify(_logger).debug(_marker, "19", "20", "21");
        rateLimitedLogger.debug(_marker, "22", throwable);
        Mockito.verify(_logger).debug(_marker, "22", throwable);
        rateLimitedLogger.isDebugEnabled(_marker);
        Mockito.verify(_logger).isDebugEnabled(_marker);
    }

    @Test
    public void testSlf4jPassThroughInfo() {
        final Throwable throwable = new NullPointerException("test exception");
        final RateLimitedLogger rateLimitedLogger = new RateLimitedLogger(
                "testSlf4jPassThrough",
                _logger,
                Duration.ofSeconds(1),
                Clock.systemDefaultZone(),
                () -> true);

        // Info
        rateLimitedLogger.info("1");
        Mockito.verify(_logger).info("1");
        rateLimitedLogger.info("2", "3");
        Mockito.verify(_logger).info("2", "3");
        rateLimitedLogger.info("4", "5", "6", "7");
        Mockito.verify(_logger).info("4", "5", "6", "7");
        rateLimitedLogger.info("8", "9", "10");
        Mockito.verify(_logger).info("8", "9", "10");
        rateLimitedLogger.info("11", throwable);
        Mockito.verify(_logger).info("11", throwable);
        rateLimitedLogger.isInfoEnabled();
        Mockito.verify(_logger).isInfoEnabled();

        rateLimitedLogger.info(_marker, "12");
        Mockito.verify(_logger).info(_marker, "12");
        rateLimitedLogger.info(_marker, "13", "14");
        Mockito.verify(_logger).info(_marker, "13", "14");
        rateLimitedLogger.info(_marker, "15", "16", "17", "18");
        Mockito.verify(_logger).info(_marker, "15", "16", "17", "18");
        rateLimitedLogger.info(_marker, "19", "20", "21");
        Mockito.verify(_logger).info(_marker, "19", "20", "21");
        rateLimitedLogger.info(_marker, "22", throwable);
        Mockito.verify(_logger).info(_marker, "22", throwable);
        rateLimitedLogger.isInfoEnabled(_marker);
        Mockito.verify(_logger).isInfoEnabled(_marker);
    }

    @Test
    public void testSlf4jPassThroughWarn() {
        final Throwable throwable = new NullPointerException("test exception");
        final RateLimitedLogger rateLimitedLogger = new RateLimitedLogger(
                "testSlf4jPassThrough",
                _logger,
                Duration.ofSeconds(1),
                Clock.systemDefaultZone(),
                () -> true);

        // Warn
        rateLimitedLogger.warn("1");
        Mockito.verify(_logger).warn("1");
        rateLimitedLogger.warn("2", "3");
        Mockito.verify(_logger).warn("2", "3");
        rateLimitedLogger.warn("4", "5", "6", "7");
        Mockito.verify(_logger).warn("4", "5", "6", "7");
        rateLimitedLogger.warn("8", "9", "10");
        Mockito.verify(_logger).warn("8", "9", "10");
        rateLimitedLogger.warn("11", throwable);
        Mockito.verify(_logger).warn("11", throwable);
        rateLimitedLogger.isWarnEnabled();
        Mockito.verify(_logger).isWarnEnabled();

        rateLimitedLogger.warn(_marker, "12");
        Mockito.verify(_logger).warn(_marker, "12");
        rateLimitedLogger.warn(_marker, "13", "14");
        Mockito.verify(_logger).warn(_marker, "13", "14");
        rateLimitedLogger.warn(_marker, "15", "16", "17", "18");
        Mockito.verify(_logger).warn(_marker, "15", "16", "17", "18");
        rateLimitedLogger.warn(_marker, "19", "20", "21");
        Mockito.verify(_logger).warn(_marker, "19", "20", "21");
        rateLimitedLogger.warn(_marker, "22", throwable);
        Mockito.verify(_logger).warn(_marker, "22", throwable);
        rateLimitedLogger.isWarnEnabled(_marker);
        Mockito.verify(_logger).isWarnEnabled(_marker);
    }

    @Test
    public void testSlf4jPassThroughError() {
        final Throwable throwable = new NullPointerException("test exception");
        final RateLimitedLogger rateLimitedLogger = new RateLimitedLogger(
                "testSlf4jPassThrough",
                _logger,
                Duration.ofSeconds(1),
                Clock.systemDefaultZone(),
                () -> true);

        // Error
        rateLimitedLogger.error("1");
        Mockito.verify(_logger).error("1");
        rateLimitedLogger.error("2", "3");
        Mockito.verify(_logger).error("2", "3");
        rateLimitedLogger.error("4", "5", "6", "7");
        Mockito.verify(_logger).error("4", "5", "6", "7");
        rateLimitedLogger.error("8", "9", "10");
        Mockito.verify(_logger).error("8", "9", "10");
        rateLimitedLogger.error("11", throwable);
        Mockito.verify(_logger).error("11", throwable);
        rateLimitedLogger.isErrorEnabled();
        Mockito.verify(_logger).isErrorEnabled();

        rateLimitedLogger.error(_marker, "12");
        Mockito.verify(_logger).error(_marker, "12");
        rateLimitedLogger.error(_marker, "13", "14");
        Mockito.verify(_logger).error(_marker, "13", "14");
        rateLimitedLogger.error(_marker, "15", "16", "17", "18");
        Mockito.verify(_logger).error(_marker, "15", "16", "17", "18");
        rateLimitedLogger.error(_marker, "19", "20", "21");
        Mockito.verify(_logger).error(_marker, "19", "20", "21");
        rateLimitedLogger.error(_marker, "22", throwable);
        Mockito.verify(_logger).error(_marker, "22", throwable);
        rateLimitedLogger.isErrorEnabled(_marker);
        Mockito.verify(_logger).isErrorEnabled(_marker);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testGetLogger() {
        final RateLimitedLogger rateLimitedLogger = new RateLimitedLogger(
                "testRateLimitedLoggerNoSkippingLogger",
                _logger,
                Duration.ofSeconds(1),
                Clock.systemDefaultZone(),
                () -> true);
        Assert.assertSame(_logger, rateLimitedLogger.getLogger());
    }

    @Mock
    private Logger _logger;
    @Mock
    private Marker _marker;

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

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
import org.slf4j.Marker;
import org.slf4j.helpers.NOPLogger;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Rate limited {@code Logger} wrapper.
 *
 * TODO(ville): Should we count no-op log messages against the rate?
 * e.g. logging an debug message when debug is not enabled
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
public final class RateLimitedLogger implements Logger {

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
        _shouldLog = this::shouldLog;
    }

    /* package private */ RateLimitedLogger(
            final String name,
            final Logger logger,
            final Duration duration,
            final Clock clock,
            final Supplier<Boolean> shouldLog) {
        _name = name;
        _logger = logger;
        _duration = duration;
        _clock = clock;
        _shouldLog = shouldLog;
    }


    @Override
    public String getName() {
        return _logger.getName();
    }

    @Override
    public boolean isTraceEnabled() {
        return _logger.isTraceEnabled();
    }

    @Override
    public void trace(final String s) {
        getEffectiveLogger().trace(s);
    }

    @Override
    public void trace(final String s, final Object o) {
        getEffectiveLogger().trace(s, o);
    }

    @Override
    public void trace(final String s, final Object o, final Object o1) {
        getEffectiveLogger().trace(s, o, o1);
    }

    @Override
    public void trace(final String s, final Object... objects) {
        getEffectiveLogger().trace(s, objects);
    }

    @Override
    public void trace(final String s, final Throwable throwable) {
        getEffectiveLogger().trace(s, throwable);
    }

    @Override
    public boolean isTraceEnabled(final Marker marker) {
        return _logger.isTraceEnabled(marker);
    }

    @Override
    public void trace(final Marker marker, final String s) {
        getEffectiveLogger().trace(marker, s);
    }

    @Override
    public void trace(final Marker marker, final String s, final Object o) {
        getEffectiveLogger().trace(marker, s, o);
    }

    @Override
    public void trace(final Marker marker, final String s, final Object o, final Object o1) {
        getEffectiveLogger().trace(marker, s, o, o1);
    }

    @Override
    public void trace(final Marker marker, final String s, final Object... objects) {
        getEffectiveLogger().trace(marker, s, objects);
    }

    @Override
    public void trace(final Marker marker, final String s, final Throwable throwable) {
        getEffectiveLogger().trace(marker, s, throwable);
    }

    @Override
    public boolean isDebugEnabled() {
        return _logger.isDebugEnabled();
    }

    @Override
    public void debug(final String s) {
        getEffectiveLogger().debug(s);
    }

    @Override
    public void debug(final String s, final Object o) {
        getEffectiveLogger().debug(s, o);
    }

    @Override
    public void debug(final String s, final Object o, final Object o1) {
        getEffectiveLogger().debug(s, o, o1);
    }

    @Override
    public void debug(final String s, final Object... objects) {
        getEffectiveLogger().debug(s, objects);
    }

    @Override
    public void debug(final String s, final Throwable throwable) {
        getEffectiveLogger().debug(s, throwable);
    }

    @Override
    public boolean isDebugEnabled(final Marker marker) {
        return _logger.isDebugEnabled(marker);
    }

    @Override
    public void debug(final Marker marker, final String s) {
        getEffectiveLogger().debug(marker, s);
    }

    @Override
    public void debug(final Marker marker, final String s, final Object o) {
        getEffectiveLogger().debug(marker, s, o);
    }

    @Override
    public void debug(final Marker marker, final String s, final Object o, final Object o1) {
        getEffectiveLogger().debug(marker, s, o, o1);
    }

    @Override
    public void debug(final Marker marker, final String s, final Object... objects) {
        getEffectiveLogger().debug(marker, s, objects);
    }

    @Override
    public void debug(final Marker marker, final String s, final Throwable throwable) {
        getEffectiveLogger().debug(marker, s, throwable);
    }

    @Override
    public boolean isInfoEnabled() {
        return _logger.isInfoEnabled();
    }

    @Override
    public void info(final String s) {
        getEffectiveLogger().info(s);
    }

    @Override
    public void info(final String s, final Object o) {
        getEffectiveLogger().info(s, o);
    }

    @Override
    public void info(final String s, final Object o, final Object o1) {
        getEffectiveLogger().info(s, o, o1);
    }

    @Override
    public void info(final String s, final Object... objects) {
        getEffectiveLogger().info(s, objects);
    }

    @Override
    public void info(final String s, final Throwable throwable) {
        getEffectiveLogger().info(s, throwable);
    }

    @Override
    public boolean isInfoEnabled(final Marker marker) {
        return _logger.isInfoEnabled(marker);
    }

    @Override
    public void info(final Marker marker, final String s) {
        getEffectiveLogger().info(marker, s);
    }

    @Override
    public void info(final Marker marker, final String s, final Object o) {
        getEffectiveLogger().info(marker, s, o);
    }

    @Override
    public void info(final Marker marker, final String s, final Object o, final Object o1) {
        getEffectiveLogger().info(marker, s, o, o1);
    }

    @Override
    public void info(final Marker marker, final String s, final Object... objects) {
        getEffectiveLogger().info(marker, s, objects);
    }

    @Override
    public void info(final Marker marker, final String s, final Throwable throwable) {
        getEffectiveLogger().info(marker, s, throwable);
    }

    @Override
    public boolean isWarnEnabled() {
        return _logger.isWarnEnabled();
    }

    @Override
    public void warn(final String s) {
        getEffectiveLogger().warn(s);
    }

    @Override
    public void warn(final String s, final Object o) {
        getEffectiveLogger().warn(s, o);
    }

    @Override
    public void warn(final String s, final Object... objects) {
        getEffectiveLogger().warn(s, objects);
    }

    @Override
    public void warn(final String s, final Object o, final Object o1) {
        getEffectiveLogger().warn(s, o, o1);
    }

    @Override
    public void warn(final String s, final Throwable throwable) {
        getEffectiveLogger().warn(s, throwable);
    }

    @Override
    public boolean isWarnEnabled(final Marker marker) {
        return _logger.isWarnEnabled(marker);
    }

    @Override
    public void warn(final Marker marker, final String s) {
        getEffectiveLogger().warn(marker, s);
    }

    @Override
    public void warn(final Marker marker, final String s, final Object o) {
        getEffectiveLogger().warn(marker, s, o);
    }

    @Override
    public void warn(final Marker marker, final String s, final Object o, final Object o1) {
        getEffectiveLogger().warn(marker, s, o, o1);
    }

    @Override
    public void warn(final Marker marker, final String s, final Object... objects) {
        getEffectiveLogger().warn(marker, s, objects);
    }

    @Override
    public void warn(final Marker marker, final String s, final Throwable throwable) {
        getEffectiveLogger().warn(marker, s, throwable);
    }

    @Override
    public boolean isErrorEnabled() {
        return _logger.isErrorEnabled();
    }

    @Override
    public void error(final String s) {
        getEffectiveLogger().error(s);
    }

    @Override
    public void error(final String s, final Object o) {
        getEffectiveLogger().error(s, o);
    }

    @Override
    public void error(final String s, final Object o, final Object o1) {
        getEffectiveLogger().error(s, o, o1);
    }

    @Override
    public void error(final String s, final Object... objects) {
        getEffectiveLogger().error(s, objects);
    }

    @Override
    public void error(final String s, final Throwable throwable) {
        getEffectiveLogger().error(s, throwable);
    }

    @Override
    public boolean isErrorEnabled(final Marker marker) {
        return _logger.isErrorEnabled(marker);
    }

    @Override
    public void error(final Marker marker, final String s) {
        getEffectiveLogger().error(marker, s);
    }

    @Override
    public void error(final Marker marker, final String s, final Object o) {
        getEffectiveLogger().error(marker, s, o);
    }

    @Override
    public void error(final Marker marker, final String s, final Object o, final Object o1) {
        getEffectiveLogger().error(marker, s, o, o1);
    }

    @Override
    public void error(final Marker marker, final String s, final Object... objects) {
        getEffectiveLogger().error(marker, s, objects);
    }

    @Override
    public void error(final Marker marker, final String s, final Throwable throwable) {
        getEffectiveLogger().error(marker, s, throwable);
    }

    /**
     * Obtain a rate limited reference to the underlying logger. If the rate
     * has been exceeded then a no-op logger instance is returned and the
     * log message is effectively discarded. Discarded messages are tracked
     * and a count of discarded messages is output with the next successful
     * call to {@code getLogger}.
     *
     * @return either the underlying logger or a no-op logger
     * @deprecated Use {@link RateLimitedLogger} directly as a {@link Logger}
     */
    @Deprecated
    public Logger getLogger() {
        return getEffectiveLogger();
    }

    /* package private */ Logger getEffectiveLogger() {
        if (_shouldLog.get()) {
            final int skipped = _skipped.getAndSet(0);
            final Instant lastLogTime = _lastLogTime.getAndSet(_clock.instant());
            if (skipped > 0) {
                _logger.info(
                        String.format(
                                "Skipped %d messages for '%s' since last log at %s",
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
    private final Supplier<Boolean> _shouldLog;
    private final AtomicReference<Instant> _lastLogTime = new AtomicReference<>();
    private final AtomicInteger _skipped = new AtomicInteger(0);
}

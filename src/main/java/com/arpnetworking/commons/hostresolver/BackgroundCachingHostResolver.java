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
package com.arpnetworking.commons.hostresolver;

import com.arpnetworking.commons.java.util.function.SingletonSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Caches results of a <code>HostResolver</code> and refresh it in the
 * background to avoid any blocking.
 *
 * Dependencies:
 * <ul>
 *     <li><i>None</i></li>
 * </ul>
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 */
public class BackgroundCachingHostResolver implements HostResolver {

    /**
     * Accessor for shared singleton instance of {@code BackgroundCachingHostResolver}
     * with default configuration of updating every 30 seconds.
     *
     * @return singleton instance of {@code BackgroundCachingHostResolver}
     */
    public static HostResolver getInstance() {
        return SINGLETON_BACKGROUND_CACHING_HOST_RESOLVER.get();
    }

    @Override
    public String getLocalHostName() throws UnknownHostException {
        final String localHostName = _cachedLocalHostName.get();
        if (localHostName == null) {
            throw new UnknownHostException();
        }
        return localHostName;
    }

    /* package private */ void updateHost() {
        try {
            _cachedLocalHostName.set(_wrappedHostResolver.getLocalHostName());
        } catch (final UnknownHostException e) {
            LOGGER.warn("Unable to resolve host", e);
        }
    }

    /**
     * Constructor for a <code>BackgroundCachingHostResolver</code> wrapping the <code>DefaultHostResolver</code>.
     *
     * @param rate The time period in which to execute one background refresh.
     */
    public BackgroundCachingHostResolver(final Duration rate) {
        this(DEFAULT_WRAPPED_HOST_RESOLVER, rate);
    }

    /**
     * Constructor for a <code>BackgroundCachingHostResolver</code> wrapping the specified <code>HostResolver</code>.
     *
     * @param wrappedHostResolver The <code>HostResolver</code> to delegate to periodically.
     * @param rate The time period in which to execute one background refresh.
     */
    public BackgroundCachingHostResolver(final HostResolver wrappedHostResolver, final Duration rate) {
        this(
                wrappedHostResolver,
                rate,
                new ScheduledThreadPoolExecutor(
                        1,
                        runnable -> {
                            final Thread thread = new Thread(runnable, "BackgroundCachingHostResolver");
                            thread.setDaemon(true);
                            return thread;
                        }));
    }

    /* package private */ BackgroundCachingHostResolver(
            final HostResolver wrappedHostResolver,
            final Duration rate,
            final ScheduledExecutorService scheduledExecutorService) {
        _wrappedHostResolver = wrappedHostResolver;
        _scheduledExecutorService = scheduledExecutorService;

        updateHost();

        _scheduledExecutorService.scheduleAtFixedRate(
                this::updateHost,
                rate.toMillis(),
                rate.toMillis(),
                TimeUnit.MILLISECONDS);
    }

    private final ScheduledExecutorService _scheduledExecutorService;
    private final HostResolver _wrappedHostResolver;

    private AtomicReference<String> _cachedLocalHostName = new AtomicReference<>();

    private static final Duration DEFAULT_RATE = Duration.ofSeconds(30);
    private static final Supplier<HostResolver> SINGLETON_BACKGROUND_CACHING_HOST_RESOLVER =
            new SingletonSupplier<>(() -> new BackgroundCachingHostResolver(DEFAULT_RATE));
    private static final HostResolver DEFAULT_WRAPPED_HOST_RESOLVER = new DefaultHostResolver();
    private static final Logger LOGGER = LoggerFactory.getLogger(BackgroundCachingHostResolver.class);
}

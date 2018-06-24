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
package com.arpnetworking.commons.hostresolver;

import java.net.UnknownHostException;
import java.time.Clock;
import java.time.Duration;
import java.util.Optional;

/**
 * Caches results of a <code>HostResolver</code>.
 *
 * Dependencies:
 * <ul>
 *     <li><i>None</i></li>
 * </ul>
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 */
public class CachingHostResolver implements HostResolver {

    @Override
    public String getLocalHostName() throws UnknownHostException {
        if (_lastResolvedAt.isPresent()) {
            final long millisSinceLastResolved = _clock.millis() - _lastResolvedAt.get();
            if (millisSinceLastResolved < _ttlInMillis && _cachedLocalHostName.isPresent()) {
                return _cachedLocalHostName.get();
            }
        }
        _lastResolvedAt = Optional.of(_clock.millis());
        _cachedLocalHostName = Optional.ofNullable(_wrappedHostResolver.getLocalHostName());
        return _cachedLocalHostName.get();
    }

    /**
     * Constructor for a <code>CachingHostResolver</code> wrapping the <code>DefaultHostResolver</code>.
     *
     * @param ttl The time to live on the cache. Does <b>not</b> perform negative caching.
     */
    public CachingHostResolver(final Duration ttl) {
        this(DEFAULT_WRAPPED_HOST_RESOLVER, ttl);
    }

    /**
     * Constructor for a <code>CachingHostResolver</code> wrapping the specified <code>HostResolver</code>.
     *
     * @param wrappedHostResolver The <code>HostResolver</code> to delegate to on a cache miss.
     * @param ttl The time to live on the cache. Does <b>not</b> perform negative caching.
     */
    public CachingHostResolver(final HostResolver wrappedHostResolver, final Duration ttl) {
        this(wrappedHostResolver, Clock.systemUTC(), ttl);
    }

    /* package private */ CachingHostResolver(
            final HostResolver wrappedHostResolver,
            final Clock clock,
            final Duration ttl) {
        _wrappedHostResolver = wrappedHostResolver;
        _clock = clock;
        _ttlInMillis = ttl.toMillis();
        _lastResolvedAt = Optional.empty();
        _cachedLocalHostName = Optional.empty();
    }

    private final HostResolver _wrappedHostResolver;
    private final Clock _clock;
    private final long _ttlInMillis;

    private Optional<Long> _lastResolvedAt;
    private Optional<String> _cachedLocalHostName;

    private static final HostResolver DEFAULT_WRAPPED_HOST_RESOLVER = new DefaultHostResolver();
}

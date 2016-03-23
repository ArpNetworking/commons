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
package com.arpnetworking.commons.hostresolver;

import com.arpnetworking.commons.java.time.ManualClock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.net.UnknownHostException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

/**
 * Tests for the DefaultHostResolver class.
 *
 * @author Ville Koskela (vkoskela at groupon dot com)
 */
public class CachingHostResolverTest {

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCaching() throws UnknownHostException {
        final HostResolver cachingHostResolver = new CachingHostResolver(
                _hostResolver,
                Clock.fixed(Instant.ofEpochSecond(1458582933L), ZoneId.of("UTC")),
                Duration.ofMillis(1));
        Mockito.doReturn("foo.example.com").when(_hostResolver).getLocalHostName();
        Assert.assertEquals("foo.example.com", cachingHostResolver.getLocalHostName());
        Mockito.verify(_hostResolver).getLocalHostName();
        Assert.assertEquals("foo.example.com", cachingHostResolver.getLocalHostName());
        Mockito.verifyNoMoreInteractions(_hostResolver);
    }

    @Test
    public void testCacheExpiration() throws UnknownHostException {
        final ManualClock manualClock = new ManualClock(Instant.ofEpochSecond(1458582933L), Duration.ofSeconds(2), ZoneId.of("UTC"));
        final HostResolver cachingHostResolver = new CachingHostResolver(
                _hostResolver,
                manualClock,
                Duration.ofSeconds(3));

        // Initial call caches the result by invoking the wrapped provider
        Mockito.doReturn("foo.example.com").when(_hostResolver).getLocalHostName();
        Assert.assertEquals("foo.example.com", cachingHostResolver.getLocalHostName());
        Mockito.verify(_hostResolver).getLocalHostName();

        manualClock.tick();

        // Second call within timeout does _not_ invoke the wrapped provider
        Mockito.doReturn("foo2.example.com").when(_hostResolver).getLocalHostName();
        Assert.assertEquals("foo.example.com", cachingHostResolver.getLocalHostName());
        Mockito.verifyNoMoreInteractions(_hostResolver);

        manualClock.tick();

        // Third call after the timeout invokes the wrapped provider
        Assert.assertEquals("foo2.example.com", cachingHostResolver.getLocalHostName());
        Mockito.verify(_hostResolver, Mockito.times(2)).getLocalHostName();

        manualClock.tick();

        // Fourth call within the timeout does _not_ invoke the wrapped provider
        Assert.assertEquals("foo2.example.com", cachingHostResolver.getLocalHostName());
        Mockito.verifyNoMoreInteractions(_hostResolver);
    }

    @Test
    public void testNoNegativeCaching() throws UnknownHostException {
        final HostResolver cachingHostResolver = new CachingHostResolver(
                _hostResolver,
                Clock.fixed(Instant.ofEpochSecond(1458582933L), ZoneId.of("UTC")),
                Duration.ofMillis(1));
        Mockito.doThrow(new UnknownHostException()).when(_hostResolver).getLocalHostName();
        try {
            cachingHostResolver.getLocalHostName();
            Assert.fail("Expected exception not thrown");
        } catch (final UnknownHostException uhe) {
            Mockito.verify(_hostResolver).getLocalHostName();
        }
        try {
            cachingHostResolver.getLocalHostName();
            Assert.fail("Expected exception not thrown");
        } catch (final UnknownHostException uhe) {
            Mockito.verify(_hostResolver, Mockito.times(2)).getLocalHostName();
        }
    }

    @Test
    public void testConstructor() throws UnknownHostException {
        final CachingHostResolver cachingHostResolver = new CachingHostResolver(Duration.ofSeconds(10));
        Assert.assertNotNull(cachingHostResolver);
        Assert.assertNotNull(cachingHostResolver.getLocalHostName());
    }

    @Test
    public void testConstructorWithWrapper() throws UnknownHostException {
        final CachingHostResolver cachingHostResolver = new CachingHostResolver(_hostResolver, Duration.ofSeconds(10));
        Assert.assertNotNull(cachingHostResolver);
        Mockito.doReturn("foo.example.com").when(_hostResolver).getLocalHostName();
        Assert.assertEquals("foo.example.com", cachingHostResolver.getLocalHostName());

    }

    @Mock
    private HostResolver _hostResolver;
}

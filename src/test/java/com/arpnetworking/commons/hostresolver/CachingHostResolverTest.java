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

import com.arpnetworking.commons.java.time.ManualClock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.net.UnknownHostException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

/**
 * Tests for the CachingHostResolver class.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
@RunWith(Parameterized.class)
public class CachingHostResolverTest {

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        final Function<HostResolver, String> methodA = CachingHostResolverTest::methodA;
        final Function<HostResolver, String> methodB = CachingHostResolverTest::methodB;

        return Arrays.asList(new Object[][] { { methodA }, { methodB } });
    }

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
        Assert.assertEquals("foo.example.com", _function.apply(cachingHostResolver));
        Mockito.verify(_hostResolver).getLocalHostName();
        Assert.assertEquals("foo.example.com", _function.apply(cachingHostResolver));
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
        Assert.assertEquals("foo.example.com", _function.apply(cachingHostResolver));
        Mockito.verify(_hostResolver).getLocalHostName();

        manualClock.tick();

        // Second call within timeout does _not_ invoke the wrapped provider
        Mockito.doReturn("foo2.example.com").when(_hostResolver).getLocalHostName();
        Assert.assertEquals("foo.example.com", _function.apply(cachingHostResolver));
        Mockito.verifyNoMoreInteractions(_hostResolver);

        manualClock.tick();

        // Third call after the timeout invokes the wrapped provider
        Assert.assertEquals("foo2.example.com", _function.apply(cachingHostResolver));
        Mockito.verify(_hostResolver, Mockito.times(2)).getLocalHostName();

        manualClock.tick();

        // Fourth call within the timeout does _not_ invoke the wrapped provider
        Assert.assertEquals("foo2.example.com", _function.apply(cachingHostResolver));
        Mockito.verifyNoMoreInteractions(_hostResolver);
    }

    @Test
    public void testConstructor() throws UnknownHostException {
        final CachingHostResolver cachingHostResolver = new CachingHostResolver(Duration.ofSeconds(10));
        Assert.assertNotNull(cachingHostResolver);
        Assert.assertNotNull(_function.apply(cachingHostResolver));
    }

    @Test
    public void testConstructorWithWrapper() throws UnknownHostException {
        final CachingHostResolver cachingHostResolver = new CachingHostResolver(_hostResolver, Duration.ofSeconds(10));
        Assert.assertNotNull(cachingHostResolver);
        Mockito.doReturn("foo.example.com").when(_hostResolver).getLocalHostName();
        Assert.assertEquals("foo.example.com", _function.apply(cachingHostResolver));

    }

    // NOTE: This test is not parameterized as it is specific to "getLocalHostName"
    @Test
    public void testNoNegativeCachingGetLocalHostName() throws UnknownHostException {
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

    // NOTE: This test is not parameterized as it is specific to "get"
    @Test
    public void testNoNegativeCachingGet() throws UnknownHostException {
        final HostResolver cachingHostResolver = new CachingHostResolver(
                _hostResolver,
                Clock.fixed(Instant.ofEpochSecond(1458582933L), ZoneId.of("UTC")),
                Duration.ofMillis(1));
        Mockito.doThrow(new UnknownHostException()).when(_hostResolver).getLocalHostName();
        try {
            cachingHostResolver.get();
            Assert.fail("Expected exception not thrown");
            // CHECKSTYLE.OFF: IllegalCatch - Required for testing
        } catch (final RuntimeException rte) {
            // CHECKSTYLE.ON: IllegalCatch
            Mockito.verify(_hostResolver).getLocalHostName();
        }
        try {
            cachingHostResolver.get();
            Assert.fail("Expected exception not thrown");
            // CHECKSTYLE.OFF: IllegalCatch - Required for testing
        } catch (final RuntimeException rte) {
            // CHECKSTYLE.ON: IllegalCatch
            Mockito.verify(_hostResolver, Mockito.times(2)).getLocalHostName();
        }
    }

    private static String methodA(final HostResolver hostResolver) {
        try {
            return hostResolver.getLocalHostName();
        } catch (final UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private static String methodB(final HostResolver hostResolver) {
        return hostResolver.get();
    }

    public CachingHostResolverTest(final Function<HostResolver, String> function) {
        this._function = function;
    }

    @Mock
    private HostResolver _hostResolver;
    private final Function<HostResolver, String> _function;
}

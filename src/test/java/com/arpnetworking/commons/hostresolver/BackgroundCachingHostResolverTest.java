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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Tests for the BackgroundCachingHostResolver class.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
@RunWith(Parameterized.class)
public class BackgroundCachingHostResolverTest {

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        final Function<HostResolver, String> methodA = BackgroundCachingHostResolverTest::methodA;
        final Function<HostResolver, String> methodB = BackgroundCachingHostResolverTest::methodB;

        return Arrays.asList(new Object[][] { { methodA }, { methodB } });
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSingleton() {
        final HostResolver hostResolverA = BackgroundCachingHostResolver.getInstance();
        Assert.assertTrue(hostResolverA instanceof BackgroundCachingHostResolver);

        final HostResolver hostResolverB = BackgroundCachingHostResolver.getInstance();
        Assert.assertTrue(hostResolverB instanceof BackgroundCachingHostResolver);

        Assert.assertSame(hostResolverA, hostResolverB);
    }


    @Test
    public void testCaching() throws UnknownHostException {
        final ScheduledExecutorService executor = Mockito.mock(ScheduledExecutorService.class);
        Mockito.doReturn("foo.example.com").when(_hostResolver).getLocalHostName();

        final HostResolver backgroundCachingHostResolver = new BackgroundCachingHostResolver(
                _hostResolver,
                DURATION,
                executor);

        Mockito.verify(executor).scheduleAtFixedRate(
                Mockito.any(Runnable.class),
                Mockito.eq(DURATION.toMillis()),
                Mockito.eq(DURATION.toMillis()),
                Mockito.eq(TimeUnit.MILLISECONDS));

        Assert.assertEquals("foo.example.com", _function.apply(backgroundCachingHostResolver));
        Mockito.verify(_hostResolver).getLocalHostName();
        Assert.assertEquals("foo.example.com", _function.apply(backgroundCachingHostResolver));
        Mockito.verifyNoMoreInteractions(_hostResolver);
    }

    @Test
    public void testBackgroundRefresh() throws UnknownHostException, InterruptedException {
        final ScheduledExecutorService executor = Mockito.mock(ScheduledExecutorService.class);
        Mockito.doReturn("foo.example.com").when(_hostResolver).getLocalHostName();

        final BackgroundCachingHostResolver backgroundCachingHostResolver = new BackgroundCachingHostResolver(
                _hostResolver,
                DURATION,
                executor);

        Mockito.verify(executor).scheduleAtFixedRate(
                Mockito.any(Runnable.class),
                Mockito.eq(DURATION.toMillis()),
                Mockito.eq(DURATION.toMillis()),
                Mockito.eq(TimeUnit.MILLISECONDS));

        // Initial call caches the result by invoking the wrapped provider
        Assert.assertEquals("foo.example.com", _function.apply(backgroundCachingHostResolver));
        Mockito.verify(_hostResolver).getLocalHostName();

        // Second call within timeout does _not_ invoke the wrapped provider
        Mockito.doReturn("foo2.example.com").when(_hostResolver).getLocalHostName();
        Assert.assertEquals("foo.example.com", _function.apply(backgroundCachingHostResolver));
        Mockito.verifyNoMoreInteractions(_hostResolver);

        // Update the host
        backgroundCachingHostResolver.updateHost();

        // Third call after the timeout invokes the wrapped provider
        Assert.assertEquals("foo2.example.com", _function.apply(backgroundCachingHostResolver));
        Mockito.verify(_hostResolver, Mockito.times(2)).getLocalHostName();

        // Fourth call within the timeout does _not_ invoke the wrapped provider
        Assert.assertEquals("foo2.example.com", _function.apply(backgroundCachingHostResolver));
        Mockito.verifyNoMoreInteractions(_hostResolver);
    }

    @Test
    public void testNoNegativeCaching() throws UnknownHostException, InterruptedException {
        final ScheduledExecutorService executor = Mockito.mock(ScheduledExecutorService.class);
        Mockito.doReturn("foo.example.com").when(_hostResolver).getLocalHostName();

        final BackgroundCachingHostResolver backgroundCachingHostResolver = new BackgroundCachingHostResolver(
                _hostResolver,
                DURATION,
                executor);

        Mockito.verify(executor).scheduleAtFixedRate(
                Mockito.any(Runnable.class),
                Mockito.eq(DURATION.toMillis()),
                Mockito.eq(DURATION.toMillis()),
                Mockito.eq(TimeUnit.MILLISECONDS));

        Assert.assertEquals("foo.example.com", _function.apply(backgroundCachingHostResolver));
        Mockito.verify(_hostResolver).getLocalHostName();

        Mockito.doThrow(new UnknownHostException()).when(_hostResolver).getLocalHostName();

        backgroundCachingHostResolver.updateHost();

        Mockito.verify(_hostResolver, Mockito.times(2)).getLocalHostName();
        Assert.assertEquals("foo.example.com", _function.apply(backgroundCachingHostResolver));
    }

    @Test
    public void testConstructor() throws UnknownHostException {
        final HostResolver backgroundCachingHostResolver = new BackgroundCachingHostResolver(Duration.ofSeconds(10));
        Assert.assertNotNull(backgroundCachingHostResolver);
        Assert.assertNotNull(_function.apply(backgroundCachingHostResolver));
    }

    // NOTE: This test is not parameterized as it is specific to "getLocalHostName"
    @Test
    public void testInitialFailureGetLocalHostName() throws UnknownHostException {
        final ScheduledExecutorService executor = Mockito.mock(ScheduledExecutorService.class);

        final HostResolver backgroundCachingHostResolver = new BackgroundCachingHostResolver(
                _hostResolver,
                DURATION,
                executor);

        Mockito.verify(executor).scheduleAtFixedRate(
                Mockito.any(Runnable.class),
                Mockito.eq(DURATION.toMillis()),
                Mockito.eq(DURATION.toMillis()),
                Mockito.eq(TimeUnit.MILLISECONDS));

        Mockito.doThrow(new UnknownHostException()).when(_hostResolver).getLocalHostName();
        try {
            backgroundCachingHostResolver.getLocalHostName();
            Assert.fail("Expected exception not thrown");
        } catch (final UnknownHostException e) {
            // Expected exception
        }
    }

    // NOTE: This test is not parameterized as it is specific to "get"
    @Test
    public void testInitialFailureGet() throws UnknownHostException {
        final ScheduledExecutorService executor = Mockito.mock(ScheduledExecutorService.class);

        final HostResolver backgroundCachingHostResolver = new BackgroundCachingHostResolver(
                _hostResolver,
                DURATION,
                executor);

        Mockito.verify(executor).scheduleAtFixedRate(
                Mockito.any(Runnable.class),
                Mockito.eq(DURATION.toMillis()),
                Mockito.eq(DURATION.toMillis()),
                Mockito.eq(TimeUnit.MILLISECONDS));

        Mockito.doThrow(new UnknownHostException()).when(_hostResolver).getLocalHostName();
        try {
            backgroundCachingHostResolver.get();
            Assert.fail("Expected exception not thrown");
            // CHECKSTYLE.OFF: IllegalCatch - Required for testing
        } catch (final RuntimeException e) {
            // CHECKSTYLE.ON: IllegalCatch
            // Expected exception
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

    public BackgroundCachingHostResolverTest(final Function<HostResolver, String> function) {
        this._function = function;
    }

    @Mock
    private HostResolver _hostResolver;
    private final Function<HostResolver, String> _function;

    private static final Duration DURATION = Duration.ofSeconds(10);
}

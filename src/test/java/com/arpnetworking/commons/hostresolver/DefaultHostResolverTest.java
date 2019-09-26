/*
 * Copyright 2014 Groupon.com
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;

/**
 * Tests for the DefaultHostResolver class.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
@RunWith(Parameterized.class)
public class DefaultHostResolverTest {

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        final Supplier<String> methodA = DefaultHostResolverTest::methodA;
        final Supplier<String> methodB = DefaultHostResolverTest::methodB;

        return Arrays.asList(new Object[][] { { methodA }, { methodB } });
    }

    @Test
    public void test() throws UnknownHostException {
        final String hostName = _supplier.get();
        Assert.assertNotNull(hostName);
        Assert.assertFalse(hostName.isEmpty());
    }

    // NOTE: This test is not parameterized as it is specific to "get"
    @Test
    public void testException() throws UnknownHostException {
        try {
            new TestDefaultHostResolver().get();
            Assert.fail("Expected exception not thrown");
            // CHECKSTYLE.OFF: IllegalCatch - Required for testing
        } catch (final RuntimeException rte) {
            // CHECKSTYLE.ON: IllegalCatch
            // Expected exception
        }
    }

    private static String methodA() {
        try {
            return HOST_RESOLVER.getLocalHostName();
        } catch (final UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private static String methodB() {
        return HOST_RESOLVER.get();
    }

    public DefaultHostResolverTest(final Supplier<String> supplier) {
        this._supplier = supplier;
    }

    private final Supplier<String> _supplier;

    private static final HostResolver HOST_RESOLVER = new DefaultHostResolver();

    private static final class TestDefaultHostResolver extends DefaultHostResolver {
        @Override
        public String getLocalHostName() throws UnknownHostException {
            throw new UnknownHostException("The host is not known!");
        }
    }
}

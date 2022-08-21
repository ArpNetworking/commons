/*
 * Copyright 2022 InscopeMetrics
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

import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Test;

import java.net.UnknownHostException;
import java.util.HashMap;

/**
 * Tests for the EnvHostResolver class.
 *
 * @author Brandon Arp (brandon dot arp at inscopemetrics dot io)
 */
public class EnvHostResolverTest {

    @Test
    public void testGetLocalHostNameHostname() throws UnknownHostException {
        final HashMap<String, String> env = Maps.newHashMap();
        final String expectedHostName = "test-host";
        env.put("HOSTNAME", expectedHostName);

        final EnvHostResolver envHostResolver = new EnvHostResolver(env::get);
        final String hostName = envHostResolver.getLocalHostName();
        Assert.assertEquals(expectedHostName, hostName);
    }

    @Test
    public void testGetHostname() {
        final HashMap<String, String> env = Maps.newHashMap();
        final String expectedHostName = "test-host";
        env.put("HOSTNAME", expectedHostName);

        final EnvHostResolver envHostResolver = new EnvHostResolver(env::get);
        final String hostName = envHostResolver.get();
        Assert.assertEquals(expectedHostName, hostName);
    }

    @Test(expected = UnknownHostException.class)
    public void testNoEnvUnknown() throws UnknownHostException {
        final EnvHostResolver noHostResolver = new EnvHostResolver(x -> null);
        noHostResolver.getLocalHostName();
    }

    @Test
    public void testGetLocalHostNameComputername() throws UnknownHostException {
        final HashMap<String, String> env = Maps.newHashMap();
        final String expectedHostName = "test-host";
        env.put("COMPUTERNAME", expectedHostName);

        final EnvHostResolver envHostResolver = new EnvHostResolver(env::get);
        final String hostName = envHostResolver.getLocalHostName();
        Assert.assertEquals(expectedHostName, hostName);
    }

    @Test
    public void testGetComputername() {
        final HashMap<String, String> env = Maps.newHashMap();
        final String expectedHostName = "test-host";
        env.put("COMPUTERNAME", expectedHostName);

        final EnvHostResolver envHostResolver = new EnvHostResolver(env::get);
        final String hostName = envHostResolver.get();
        Assert.assertEquals(expectedHostName, hostName);
    }

    @Test
    public void testGetLocalHostNameHostnameOverrides() throws UnknownHostException {
        final HashMap<String, String> env = Maps.newHashMap();
        final String expectedHostName = "test-host";
        env.put("HOSTNAME", expectedHostName);
        env.put("COMPUTERNAME", "wrong-name");

        final EnvHostResolver envHostResolver = new EnvHostResolver(env::get);
        final String hostName = envHostResolver.getLocalHostName();
        Assert.assertEquals(expectedHostName, hostName);
    }

    @Test
    public void testGetHostnameOverrides() {
        final HashMap<String, String> env = Maps.newHashMap();
        final String expectedHostName = "test-host";
        env.put("HOSTNAME", expectedHostName);
        env.put("COMPUTERNAME", "wrong-name");

        final EnvHostResolver envHostResolver = new EnvHostResolver(env::get);
        final String hostName = envHostResolver.get();
        Assert.assertEquals(expectedHostName, hostName);
    }

    @Test
    public void testException() throws UnknownHostException {
        try {
            new TestEnvHostResolver().get();
            Assert.fail("Expected exception not thrown");
            // CHECKSTYLE.OFF: IllegalCatch - Required for testing
        } catch (final RuntimeException rte) {
            // CHECKSTYLE.ON: IllegalCatch
            // Expected exception
        }
    }

    private static final class TestEnvHostResolver extends EnvHostResolver {
        @Override
        public String getLocalHostName() throws UnknownHostException {
            throw new UnknownHostException("The host is not known!");
        }
    }

}

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

import java.net.UnknownHostException;
import java.util.Optional;
import java.util.function.Function;

/**
 * Resolves a host name using environment variables.
 *
 * Dependencies:
 * <ul>
 *     <li><i>None</i></li>
 * </ul>
 *
 * @author Brandon Arp (brandon dot arp at inscopemetrics dot io)
 */
public class EnvHostResolver implements HostResolver {

    @Override
    public String getLocalHostName() throws UnknownHostException {
        final String hostname = Optional.ofNullable(_envLookup.apply("HOSTNAME")).orElseGet(() -> _envLookup.apply("COMPUTERNAME"));
        if (hostname == null) {
            throw new UnknownHostException("Could not resolve hostname");
        }
        return hostname;
    }

    /** Public constructor. */
    public EnvHostResolver() {
        // Empty constructor
        this(System::getenv);
    }

    EnvHostResolver(final Function<String, String> envLookup) {
        this._envLookup = envLookup;
    }

    private final Function<String, String> _envLookup;
}

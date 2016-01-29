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
package com.arpnetworking.commons.jackson.databind.exceptions;

import com.fasterxml.jackson.core.JsonLocation;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the EnumerationNotFoundException class.
 *
 * @author Ville Koskela (vkoskela at groupon dot com)
 */
public class EnumerationNotFoundExceptionTest {

    @Test
    public void testConstructor() {
        final JsonLocation location = JsonLocation.NA;
        final EnumerationNotFoundException e = new EnumerationNotFoundException(
                "FOO",
                TestEnumeration.class,
                location);
        Assert.assertTrue(e.getMessage().contains("FOO"));
        Assert.assertTrue(e.getMessage().contains(TestEnumeration.class.toString()));
        Assert.assertEquals(location, e.getLocation());
        Assert.assertNull(e.getCause());
    }

    @Test
    public void testConstructorWithCause() {
        final JsonLocation location = JsonLocation.NA;
        final EnumerationNotFoundException e = new EnumerationNotFoundException(
                "FOO",
                TestEnumeration.class,
                location,
                new NullPointerException());
        Assert.assertTrue(e.getMessage().contains("FOO"));
        Assert.assertTrue(e.getMessage().contains(TestEnumeration.class.toString()));
        Assert.assertEquals(location, e.getLocation());
        Assert.assertTrue(e.getCause() instanceof NullPointerException);
    }

    private enum TestEnumeration {
        FOO,
        BAR;
    }
}

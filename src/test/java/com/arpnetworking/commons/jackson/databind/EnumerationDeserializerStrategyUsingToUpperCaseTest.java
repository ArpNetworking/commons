/**
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
package com.arpnetworking.commons.jackson.databind;

import com.arpnetworking.commons.jackson.databind.exceptions.EnumerationNotFoundException;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

/**
 * Tests for the ConfigurationException class.
 *
 * @author Ville Koskela (vkoskela at groupon dot com)
 */
public class EnumerationDeserializerStrategyUsingToUpperCaseTest {

    @Test
    public void testStrategy() throws EnumerationNotFoundException {
        final EnumerationDeserializerStrategy<TestEnum> strategy = EnumerationDeserializerStrategyUsingToUpperCase.newInstance();
        final Optional<TestEnum> result = strategy.toEnum(TestEnum.class, "abc");
        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(TestEnum.ABC, result.get());
    }

    @Test
    public void testStrategyFailure() throws EnumerationNotFoundException {
        final EnumerationDeserializerStrategy<TestEnum> strategy = EnumerationDeserializerStrategyUsingToUpperCase.newInstance();
        final Optional<TestEnum> result = strategy.toEnum(TestEnum.class, "foo");
        Assert.assertFalse(result.isPresent());
    }

    private enum TestEnum {
        ABC;
    }
}

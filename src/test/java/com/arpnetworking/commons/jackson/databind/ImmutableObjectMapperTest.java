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
package com.arpnetworking.commons.jackson.databind;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;

/**
 * Tests for {@link ImmutableObjectMapper}.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
public class ImmutableObjectMapperTest {

    @Test
    public void testImmutableMethodSucceeds() throws IOException {
        final ObjectMapper objectMapper = ImmutableObjectMapper.of(new ObjectMapper());
        objectMapper.readTree("{}");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testMutableMethodFails() {
        final ObjectMapper objectMapper = ImmutableObjectMapper.of(new ObjectMapper());
        objectMapper.getFactory();
    }

    @Test
    public void testNestedImmutableMethodSucceeds() throws IOException {
        final ObjectMapper objectMapper = ImmutableObjectMapper.of(
                ImmutableObjectMapper.of(new ObjectMapper()));
        objectMapper.readTree("{}");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testNestedMutableMethodFails() {
        final ObjectMapper objectMapper = ImmutableObjectMapper.of(
                ImmutableObjectMapper.of(new ObjectMapper()));
        objectMapper.getFactory();
    }

    @Test
    public void testConstructorIsPrivate() throws Exception {
        final Constructor<ImmutableObjectMapper> constructor = ImmutableObjectMapper.class.getDeclaredConstructor();
        Assert.assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test(expected = JsonMappingException.class)
    public void testActualExceptionsAreThrown() throws IOException {
        final ObjectMapper objectMapper = ImmutableObjectMapper.of(new ObjectMapper());
        objectMapper.readValue("".getBytes(Charset.defaultCharset()), String.class);
    }

    @Test(expected = RuntimeException.class)
    public void testBadEnhanceClass() {
       class BadEnhanceClass {
              BadEnhanceClass(final String someArg) {
              }
       }
       ImmutableObjectMapper.enhanceAndProxy(new BadEnhanceClass("foo"), BadEnhanceClass.class);
    }
}

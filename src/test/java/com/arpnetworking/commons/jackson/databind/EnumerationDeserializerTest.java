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

import com.arpnetworking.commons.jackson.databind.exceptions.EnumerationNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Optional;

/**
 * Tests for the <code>EnumerationDeserializer</code> class.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
public class EnumerationDeserializerTest {

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testDeserializer() throws Exception {
        Mockito.doReturn(Optional.of(TestEnum.FOO)).when(_strategy).toEnum(TestEnum.class, "bar");

        final SimpleModule module = new SimpleModule();
        module.addDeserializer(TestEnum.class, EnumerationDeserializer.newInstance(TestEnum.class, _strategy));
        final ObjectMapper objectMapper = ObjectMapperFactory.createInstance();
        objectMapper.registerModule(module);

        final TestContainer c = objectMapper.readValue("{\"enum\":\"bar\"}", TestContainer.class);
        Mockito.verify(_strategy).toEnum(TestEnum.class, "bar");
        Mockito.verifyNoMoreInteractions(_strategy);
        Assert.assertEquals(TestEnum.FOO, c.getEnum());
    }

    @Test
    public void testEnumerationNotFound() throws Exception {
        Mockito.doReturn(Optional.empty()).when(_strategy).toEnum(TestEnum.class, "bar");

        final SimpleModule module = new SimpleModule();
        module.addDeserializer(TestEnum.class, EnumerationDeserializer.newInstance(TestEnum.class, _strategy));
        final ObjectMapper objectMapper = ObjectMapperFactory.createInstance();
        objectMapper.registerModule(module);

        try {
            final TestContainer c = objectMapper.readValue("{\"enum\":\"bar\"}", TestContainer.class);
            Assert.fail("Expected exception not thrown");
        } catch (final IOException e) {
            Assert.assertTrue(e.getCause() instanceof EnumerationNotFoundException);
            Mockito.verify(_strategy).toEnum(TestEnum.class, "bar");
            Mockito.verifyNoMoreInteractions(_strategy);
        }
    }

    @Mock
    private EnumerationDeserializerStrategy<TestEnum> _strategy;

    private static final class TestContainer {
        @SuppressWarnings("unused")
        public void setEnum(final TestEnum e) {
            _enum = e;
        }

        public TestEnum getEnum() {
            return _enum;
        }

        private TestEnum _enum;
    }

    private enum TestEnum {
        FOO
    }
}

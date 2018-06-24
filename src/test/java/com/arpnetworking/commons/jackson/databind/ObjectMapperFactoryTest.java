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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.joda.time.Duration;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Tests for <code>ObjectMapperFactory</code>.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 */
public class ObjectMapperFactoryTest {

    @Test
    public void testNewInstance() {
        final ObjectMapper objectMapper1 = ObjectMapperFactory.createInstance();
        final ObjectMapper objectMapper2 = ObjectMapperFactory.createInstance();
        Assert.assertNotSame(objectMapper1, objectMapper2);

        // Deserialization feature
        objectMapper1.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        objectMapper2.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Assert.assertTrue(objectMapper1.getDeserializationConfig().isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        Assert.assertFalse(objectMapper2.getDeserializationConfig().isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));

        // Serialization feature
        objectMapper1.configure(SerializationFeature.INDENT_OUTPUT, true);
        objectMapper2.configure(SerializationFeature.INDENT_OUTPUT, false);
        Assert.assertTrue(objectMapper1.getSerializationConfig().isEnabled(SerializationFeature.INDENT_OUTPUT));
        Assert.assertFalse(objectMapper2.getSerializationConfig().isEnabled(SerializationFeature.INDENT_OUTPUT));
    }

    @Test
    public void testNewInstanceConversion() throws IOException {
        final String expectedJson = "{}";
        final ObjectMapper objectMapper = ObjectMapperFactory.createInstance();
        final JsonNode jsonNode = objectMapper.readValue(expectedJson, JsonNode.class);
        final String actualJson = objectMapper.writeValueAsString(jsonNode);
        Assert.assertEquals(expectedJson, actualJson);
    }

    @Test
    public void testNewInstanceWithCustomJsonFactory() {
        final JsonFactory jsonFactory = Mockito.mock(JsonFactory.class);
        final ObjectMapper objectMapper = ObjectMapperFactory.createInstance(jsonFactory);
        Assert.assertSame(jsonFactory, objectMapper.getFactory());
    }

    @Test
    public void testGetInstance() {
        final ObjectMapper objectMapper1 = ObjectMapperFactory.getInstance();
        final ObjectMapper objectMapper2 = ObjectMapperFactory.getInstance();
        Assert.assertSame(objectMapper1, objectMapper2);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetInstanceUnmodifiableDeserializationConfig() {
        final ObjectMapper objectMapper = ObjectMapperFactory.getInstance();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetInstanceUnmodifiableSerializationConfig() {
        final ObjectMapper objectMapper = ObjectMapperFactory.getInstance();
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

    @Test
    public void testGetInstanceToString() {
        final String asString = ObjectMapperFactory.getInstance().toString();
        Assert.assertNotNull(asString);
    }

    @Test
    public void testGetInstanceConversion() throws IOException {
        final String expectedJson = "{}";
        final ObjectMapper objectMapper = ObjectMapperFactory.getInstance();
        final JsonNode jsonNode = objectMapper.readValue(expectedJson, JsonNode.class);
        final String actualJson = objectMapper.writeValueAsString(jsonNode);
        Assert.assertEquals(expectedJson, actualJson);
    }

    @Test
    public void testRegisterModule() {
        final ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        ObjectMapperFactory.registerModule(objectMapper, "foo.bar");
        Mockito.verifyZeroInteractions(objectMapper);

        ObjectMapperFactory.registerModule(objectMapper, "com.fasterxml.jackson.datatype.guava.GuavaModule");
        Mockito.verify(objectMapper).registerModule(Mockito.any(Module.class));
    }

    @Test
    public void testBadRegisterModule() {
        final ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        ObjectMapperFactory.registerModule(
                objectMapper,
                "com.arpnetworking.commons.jackson.databind.ObjectMapperFactoryTest$BadModule");
        Mockito.verifyZeroInteractions(objectMapper);
    }

    @Test
    public void testRegisterAdditionalModulesOneModule() {
        final ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        ObjectMapperFactory.registerAdditionalModules(
                objectMapper,
                s -> "com.fasterxml.jackson.datatype.guava.GuavaModule");
        Mockito.verify(objectMapper).registerModule(Mockito.any(GuavaModule.class));
    }

    @Test
    public void testRegisterAdditionalModulesMultipleModules() {
        final ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        ObjectMapperFactory.registerAdditionalModules(
                objectMapper,
                s -> "com.fasterxml.jackson.datatype.guava.GuavaModule,com.fasterxml.jackson.datatype.joda.JodaModule");

        final ArgumentCaptor<Module> captor = ArgumentCaptor.forClass(Module.class);
        Mockito.verify(objectMapper, Mockito.times(2)).registerModule(captor.capture());
        Mockito.verifyNoMoreInteractions(objectMapper);
        final List<Module> registered = captor.getAllValues();
        Assert.assertEquals(2, registered.size());
        Assert.assertTrue(registered.get(0) instanceof GuavaModule);
        Assert.assertTrue(registered.get(1) instanceof JodaModule);
    }

    @Test
    public void testRegisterAdditionalModulesNotConfigured() {
        final ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        ObjectMapperFactory.registerAdditionalModules(
                objectMapper,
                s -> null);
        Mockito.verifyZeroInteractions(objectMapper);
    }

    @Test
    public void testGetClass() {
        final Optional<Class<? extends String>> classA = ObjectMapperFactory.getClass("java.lang.String");
        Assert.assertTrue(classA.isPresent());
        Assert.assertEquals(java.lang.String.class, classA.get());

        final Optional<Class<? extends String>> classB = ObjectMapperFactory.getClass("foo.bar");
        Assert.assertFalse(classB.isPresent());
    }

    @Test
    public void testConstructorIsPrivate() throws Exception {
        final Constructor<ObjectMapperFactory> constructor = ObjectMapperFactory.class.getDeclaredConstructor();
        Assert.assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    public void testJavaOptionalSerialization() throws JsonProcessingException {
        final ObjectMapper objectMapper = ObjectMapperFactory.getInstance();
        final String actualPresentValue = objectMapper.writeValueAsString(java.util.Optional.of(Boolean.TRUE));
        Assert.assertEquals("true", actualPresentValue);
        final String actualAbsentValue = objectMapper.writeValueAsString(java.util.Optional.empty());
        Assert.assertEquals("null", actualAbsentValue);
    }

    @Test
    public void testGauvaOptionalSerialization() throws JsonProcessingException {
        final ObjectMapper objectMapper = ObjectMapperFactory.getInstance();
        final String actualPresentValue = objectMapper.writeValueAsString(com.google.common.base.Optional.of(Boolean.TRUE));
        Assert.assertEquals("true", actualPresentValue);
        final String actualAbsentValue = objectMapper.writeValueAsString(com.google.common.base.Optional.absent());
        Assert.assertEquals("null", actualAbsentValue);
    }

    @Test
    public void testDurationSerialization() throws JsonProcessingException {
        final ObjectMapper objectMapper = ObjectMapperFactory.getInstance();
        final String actualPresentValue = objectMapper.writeValueAsString(Duration.standardSeconds(10));
        Assert.assertEquals("\"PT10S\"", actualPresentValue);
    }

    @Test
    public void testZonedDateTimeSerialization() throws IOException {
        final ObjectMapper objectMapper = ObjectMapperFactory.getInstance();
        final String actualValue = objectMapper.writeValueAsString(ZonedDateTime.parse("2014-03-24T12:15:40.008Z"));
        Assert.assertEquals("\"2014-03-24T12:15:40.008Z\"", actualValue);
    }

    @Test
    public void testZonedDateTimeDeserialization() throws IOException {
        final ObjectMapper objectMapper = ObjectMapperFactory.getInstance();
        final ZonedDateTime actualValue = objectMapper.readValue("\"2014-03-24T12:15:40.008Z\"", ZonedDateTime.class);
        Assert.assertEquals(actualValue, ZonedDateTime.parse("2014-03-24T12:15:40.008Z"));
    }

    /**
     * Jackson Module which fails under construction.
     */
    public static class BadModule extends Module {

        public BadModule() {
            throw new IllegalStateException("Bad module constructor always throws");
        }

        @Override
        public String getModuleName() {
            return null;
        }

        @Override
        public Version version() {
            return null;
        }

        @Override
        public void setupModule(final SetupContext setupContext) {}
    }
}

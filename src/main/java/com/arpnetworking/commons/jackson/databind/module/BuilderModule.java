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
package com.arpnetworking.commons.jackson.databind.module;

import com.arpnetworking.commons.jackson.databind.deser.ThreadLocalBuilderBeanDeserializerModifier;
import com.arpnetworking.commons.jackson.databind.introspect.BuilderAnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Jackson Module for injecting JsonDeserialize annotations configured to use a model
 * classes internal {@link com.arpnetworking.commons.builder.Builder} class.
 *
 * Dependencies:
 * <ul>
 *     <li>com.fasterxml.jackson.core:jackson-databind</li>
 * </ul>
 *
 * @author Gil Markham (gil at groupon dot com)
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
public class BuilderModule extends SimpleModule {

    @Override
    public void setupModule(final SetupContext context) {
        super.setupModule(context);
        context.addBeanDeserializerModifier(new ThreadLocalBuilderBeanDeserializerModifier());
        context.insertAnnotationIntrospector(
                new AnnotationIntrospectorPair(
                        new JacksonAnnotationIntrospector(),
                        new BuilderAnnotationIntrospector()));
    }

    private static final long serialVersionUID = 3669894622411029788L;
}

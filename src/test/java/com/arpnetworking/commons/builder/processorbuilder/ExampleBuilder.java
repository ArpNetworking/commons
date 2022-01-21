/*
 * Copyright 2022 Inscope Metrics
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
package com.arpnetworking.commons.builder.processorbuilder;

import com.arpnetworking.commons.builder.OvalBuilder;
import com.arpnetworking.commons.builder.annotations.SkipValidationProcessor;
import net.sf.oval.constraint.NotNull;

import java.util.function.Function;

/**
 * Test class for bean validators.
 *
 * @param <T> builder type
 *
 * @author Brandon Arp (brandon dot arp at inscopemetrics dot io)
 */
@SkipValidationProcessor
public class ExampleBuilder<T> extends OvalBuilder<T> {

    /**
     * Constructor.
     */
    ExampleBuilder(final Function<ExampleBuilder<T>, T> targetConstructor) {
        super(targetConstructor);
    }

    /**
     * Sets the value.
     *
     * @param value the value
     * @return this builder
     */
    public ExampleBuilder<T> setValue(final Object value) {
        _value = value;
        return this;
    }

    @NotNull
    private Object _value;
}

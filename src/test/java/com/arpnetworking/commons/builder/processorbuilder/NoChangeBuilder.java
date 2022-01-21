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

import javax.annotation.Nullable;

/**
 * Test class for bean validators.
 *
 * @author Brandon Arp (brandon dot arp at inscopemetrics dot io)
 */
@SkipValidationProcessor
public class NoChangeBuilder extends OvalBuilder<ExamplePojo> {

    /**
     * Constructor.
     */
    public NoChangeBuilder() {
        super(ExamplePojo::new);
    }

    /**
     * Sets the value.
     *
     * @param value the value
     * @return this builder
     */
    // NOTE: Marked as nullable to faciliate null testing; normally
    // setters for fields marked @NonNull should not be marked
    // @Nullable.
    public NoChangeBuilder setValue(@Nullable final Object value) {
        _value = value;
        return this;
    }

    public Object getValue() {
        return _value;
    }

    @NotNull
    private Object _value;
}

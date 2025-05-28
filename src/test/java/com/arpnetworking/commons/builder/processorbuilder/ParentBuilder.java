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
 * @param <B> type of builder
 *
 * @author Brandon Arp (brandon dot arp at inscopemetrics dot io)
 */
@SkipValidationProcessor
public abstract class ParentBuilder<B> extends OvalBuilder<ExamplePojo> {

    /**
     * Constructor.
     */
    ParentBuilder() {
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
    public B setValue(@Nullable final Object value) {
        _value = value;
        return self();
    }

    public Object getValue() {
        return _value;
    }

    /**
     * Gets the type of `this`.
     *
     * @return this
     */
    protected abstract B self();

    @NotNull
    private Object _value;
}

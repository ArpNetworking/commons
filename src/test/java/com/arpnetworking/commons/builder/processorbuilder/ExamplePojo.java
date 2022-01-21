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

import com.arpnetworking.commons.builder.Builder;

/**
 * There are two strange things going on here.
 * <p>
 * 1) The builders are not nested inside pojo classes. When this is done
 * and the builder is renamed after processing, which happens only in these
 * tests, it breaks the the static parent class link.
 * <p>
 * See:
 * https://issues.jboss.org/browse/JASSIST-136
 * <p>
 * Consequently, the builders are all nested direclty under the test to
 * which they do not use their parent reference.
 * <p>
 * 2) There is a single pojo class for simplicity. However, it must use
 * reflection to retrieve the value since the builder class is one of
 * six; three concrete builders and a second processed variant of each.
 *
 * @author Brandon Arp (brandon dot arp at inscopemetrics dot io)
 */
public class ExamplePojo {

    /**
     * Consructor.
     *
     * @param builder the builder
     */
    public ExamplePojo(final Builder<ExamplePojo> builder) {
        try {
            _value = builder.getClass().getMethod("getValue").invoke(builder);
            // CHECKSTYLE.OFF: IllegalCatch - Constructor is not allowed to throw.
        } catch (final Exception e) {
            // CHECKSTYLE.ON: IllegalCatch
            throw new RuntimeException(e);
        }
    }

    public Object getValue() {
        return _value;
    }

    private final Object _value;
}

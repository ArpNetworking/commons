/*
 * Copyright 2020 Dropbox
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

package com.arpnetworking.commons.performance;

import com.arpnetworking.commons.math.Accumulator;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Base performance tests for the {@link Accumulator} implementations.
 *
 * @author Ville Koskela (ville at inscopemetrics dot io)
 */
@SuppressFBWarnings("DMI_RANDOM_USED_ONLY_ONCE")
public abstract class BaseAccumulatorTestPerf {

    private static final List<Double> RANDOM_VALUE_DATA_SET;
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseAccumulatorTestPerf.class);

    static {
        // Random values data set
        final List<Double> valueDataSet = new ArrayList<>();
        final Random valueRandom = new Random(564564359);
        final double max = 1.0e16;
        final double min = -1.0e16;
        for (int i = 0; i < 4000000; ++i) {
            valueDataSet.add(valueRandom.nextDouble() * (max - min) + min);
        }
        RANDOM_VALUE_DATA_SET = Collections.unmodifiableList(valueDataSet);
    }

    protected void runTest(final Accumulator accumulator) {
        for (final double value : RANDOM_VALUE_DATA_SET) {
            accumulator.accumulate(value);
        }

        LOGGER.info("Accumulator {} sum was: {}", accumulator.getClass().getSimpleName(), accumulator.getSum());
    }
}

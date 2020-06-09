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

package com.arpnetworking.commons.math;

import java.util.ArrayList;
import java.util.List;

/**
 * Accumulator implementation using pair-wise sum algorithm. This has O(log N) error.
 *
 * https://en.wikipedia.org/wiki/Pairwise_summation
 *
 * <b>WARNING</b>: Unlike the other implementations of {@link Accumulator} this
 * implementation must buffer the samples. Consider your memory constraints
 * before using.
 *
 * Dependencies:
 * <ul>
 *     <li><i>None</i></li>
 * </ul>
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
public final class PairwiseAccumulator implements Accumulator {

    private static final int INITIAL_SIZE = 64;
    private static final int BASE = 4;

    private List<Double> _buffer = new ArrayList<>(INITIAL_SIZE);

    @Override
    public void accumulate(final double value) {
        _buffer.add(value);
    }

    @Override
    public double getSum() {
        return recursiveSum(_buffer);
    }

    private static double recursiveSum(final List<Double> values) {
        final int range = values.size();

        // Base case
        if (range <= BASE) {
            double partialSum = 0.0;
            for (final double value : values) {
                partialSum += value;
            }
            return partialSum;
        }

        // Recursive split the array
        final int splitIndex = (int) Math.floor(range / 2.0);
        return recursiveSum(values.subList(0, splitIndex))
                + recursiveSum(values.subList(splitIndex, range));
    }
}

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

/**
 * Accumulator implementation using Neumaier version of the Kahan Sum algorithm.
 * This has effectively O(1) error.
 *
 * https://en.wikipedia.org/wiki/Kahan_summation_algorithm
 *
 * Dependencies:
 * <ul>
 *     <li><i>None</i></li>
 * </ul>
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
public final class NeumaierAccumulator implements Accumulator {

    private double _sum = 0.0;
    private double _compensation = 0.0;

    @Override
    public void accumulate(final double value) {
        final double total = _sum + value;
        if (Math.abs(_sum) >= Math.abs(value)) {
            _compensation += (_sum - total) + value;
        } else {
            _compensation += (value - total) + _sum;
        }
        _sum = total;
    }

    @Override
    public double getSum() {
        return _sum + _compensation;
    }
}

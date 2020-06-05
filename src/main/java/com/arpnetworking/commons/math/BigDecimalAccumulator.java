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

import java.math.BigDecimal;

/**
 * Accumulator implementation using {@link BigDecimal}. This has no error.
 * Although the precision of the result is still subject to constraints of
 * representation as a {@code double}.
 *
 * Dependencies:
 * <ul>
 *     <li><i>None</i></li>
 * </ul>
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
public final class BigDecimalAccumulator implements Accumulator {

    private BigDecimal _sum = new BigDecimal(0.0);

    @Override
    public void accumulate(final double value) {
        _sum = _sum.add(new BigDecimal(value));
    }

    @Override
    public double getSum() {
        return _sum.doubleValue();
    }
}

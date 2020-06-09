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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * Tests for the {@link Accumulator} implementations.
 *
 * @author Ville Koskela (ville at koskilabs dot com)
 */
@RunWith(Parameterized.class)
public final class AccumulatorTest {

    private static final List<Double> RANDOM_VALUE_DATA_SET;
    private static final List<Double> RANDOM_CANCEL_DATA_SET;
    public static final int SAMPLE_COUNT = 4000000;
    private static final Logger LOGGER = LoggerFactory.getLogger(AccumulatorTest.class);

    static {
        // Random values data set
        final List<Double> valueDataSet = new ArrayList<>();
        final Random valueRandom = new Random(564564359);
        final double max = 1.0e16;
        final double min = -1.0e16;
        RANDOM_VALUE_DATA_SET = Collections.unmodifiableList(
                valueRandom.doubles(SAMPLE_COUNT, min, max).boxed().collect(Collectors.toList()));

        // Random cancellations data set
        final List<Double> cancelDataSet = new ArrayList<>();
        final Random cancelRandom = new Random(1591215254);
        for (int i = 0; i < SAMPLE_COUNT; ++i) {
            final int group = cancelRandom.nextInt(4);
            switch (group) {
                case 0:
                    cancelDataSet.add(1.0);
                    break;
                case 1:
                    cancelDataSet.add(1.0e16);
                    break;
                case 2:
                    cancelDataSet.add(-1.0);
                    break;
                case 3:
                    cancelDataSet.add(-1.0e16);
                    break;
                default:
                    throw new IllegalStateException("This should never happen");
            }
        }
        RANDOM_CANCEL_DATA_SET = Collections.unmodifiableList(cancelDataSet);
    }

    private final Supplier<Accumulator> _accumulatorSupplier;
    private final double _differentMagnitudesMargin;
    private final double _differentSignsMargin;
    private final double _randomCancellationMargin;
    private final double _randomValuesMargin;

    public AccumulatorTest(
            final Class<? extends Accumulator> accumulatorClass,
            final Double differentMagnitudesMargin,
            final Double differentSignsMargin,
            final Double randomCancellationMargin,
            final Double randomValuesMargin) {
        _accumulatorSupplier = () -> {
            try {
                return accumulatorClass.newInstance();
            } catch (final IllegalAccessException | InstantiationException e) {
                throw new RuntimeException(e);
            }
        };
        _differentMagnitudesMargin = differentMagnitudesMargin;
        _differentSignsMargin = differentSignsMargin;
        _randomCancellationMargin = randomCancellationMargin;
        _randomValuesMargin = randomValuesMargin;
    }

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> parameters() {
        return Arrays.asList(
                new Object[]{
                        NaiveAccumulator.class,
                        1.0E10,     // magnitudes
                        0.0,        // signs
                        1.1e3,      // random cancel
                        3.9e6},     // random values
                new Object[]{
                        BigDecimalAccumulator.class,
                        0.0,        // magnitudes
                        0.0,        // signs
                        0.0,        // random cancel
                        0.0},       // random values
                new Object[]{
                        KahanAccumulator.class,
                        0.0,        // magnitudes
                        0.0,        // signs
                        0.0,        // random cancel
                        0.5e4},     // random values
                new Object[]{
                        NeumaierAccumulator.class,
                        0.0,        // magnitudes
                        0.0,        // signs
                        0.0,        // random cancel
                        0.0},       // random values
                new Object[]{
                        PairwiseAccumulator.class,
                        8.0e9,      // magnitudes
                        3.0e16,     // signs
                        1.1e3,      // random cancel
                        2.0e4});    // random values
    }

    @Test
    public void testDifferentMagnitudes() {
        final Accumulator accumulator = _accumulatorSupplier.get();
        accumulator.accumulate(1.0e16);
        for (int i = 0; i < 1000000; ++i) {
            accumulator.accumulate(1.0);
        }
        assertEquals(1.0000000001E16, accumulator.getSum(), _differentMagnitudesMargin);
    }

    @Test
    public void testDifferentSigns() {
        final Accumulator accumulator = _accumulatorSupplier.get();
        for (int i = 0; i < SAMPLE_COUNT; ++i) {
            final int group = i % 4;
            switch (group) {
                case 0:
                    accumulator.accumulate(1.0);
                    break;
                case 1:
                    accumulator.accumulate(1.0e16);
                    break;
                case 2:
                    accumulator.accumulate(-1.0);
                    break;
                case 3:
                    accumulator.accumulate(-1.0e16);
                    break;
                default:
                    throw new IllegalStateException("This should never happen");
            }
        }
        assertEquals(0.0, accumulator.getSum(), _differentSignsMargin);
    }

    @Test
    public void testRandomValues() {
        final Accumulator accumulator = _accumulatorSupplier.get();
        for (final double value : RANDOM_VALUE_DATA_SET) {
            accumulator.accumulate(value);
        }

        // The actual value is below and is approximately -2.71e18; all the
        // algorithms did surprising well including Naive which did better than
        // Pairwise. Other accumulator sums are:
        //
        // Naive:  -2.7135163974452634E18
        // BigDe:  -2.7135163974456428E18
        // Kahan:  -2.7135163974456433E18
        // Neuma:  -2.7135163974456428E18
        // Pairw:  -2.7135163974456448E18

        LOGGER.info("Accumulator {} sum was: {}", accumulator.getClass().getSimpleName(), accumulator.getSum());

        assertEquals(-2.7135163974456428E18, accumulator.getSum(), _randomValuesMargin);
    }

    @Test
    public void testRandomCancellation() {
        final Accumulator accumulator = _accumulatorSupplier.get();
        for (final double value : RANDOM_CANCEL_DATA_SET) {
            accumulator.accumulate(value);
        }

        // The actual value is below and is approximately 3.31e18; all the
        // algorithms did surprising well including Naive which did better than
        // Pairwise. Other accumulator sums are:
        //
        // Naive:  3310000000000000000.000000
        // BigDe:  3310000000000001000.000000
        // Kahan:  3310000000000001000.000000
        // Neuma:  3310000000000001000.000000
        // Pairw:  3310000000000000000.000000

        LOGGER.info("Accumulator {} sum was: {}", accumulator.getClass().getSimpleName(), accumulator.getSum());

        assertEquals(3310000000000001000.000000, accumulator.getSum(), _randomCancellationMargin);
    }
}

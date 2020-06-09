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

import com.arpnetworking.commons.math.PairwiseAccumulator;
import com.arpnetworking.test.junitbenchmarks.JsonBenchmarkConsumer;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Performance test for {@link PairwiseAccumulator}.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
@BenchmarkOptions(callgc = true, benchmarkRounds = 10, warmupRounds = 5)
public final class PairwiseAccumulatorTestPerf extends BaseAccumulatorTestPerf {

    private static final JsonBenchmarkConsumer JSON_BENCHMARK_CONSUMER = new JsonBenchmarkConsumer(
            Paths.get("target/perf/pairwise-accumulator-performance-test.json"));
    @Rule
    @SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    public final TestRule _benchMarkRule = new BenchmarkRule(JSON_BENCHMARK_CONSUMER);

    @BeforeClass
    public static void setUp() {
        JSON_BENCHMARK_CONSUMER.prepareClass();
    }

    @Test
    public void serializeSize() throws IOException {
        runTest(new PairwiseAccumulator());
    }
}

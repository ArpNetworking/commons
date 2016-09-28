/**
 * Copyright 2016 Inscope Metrics Inc.
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
package com.arpnetworking.commons.builder;

import com.arpnetworking.commons.builder.annotations.SkipValidationProcessor;
import net.sf.oval.ConstraintViolation;

import java.util.List;
import java.util.function.Function;

/**
 * Intermediate builder between {@link OvalBuilder} and the builder to be
 * transformed by {@link ValidationProcessor}. This class prevents calls to
 * {@code validate} from propagating to {@link OvalBuilder} thus ensuring
 * that any validation exceptions arise from transformation and not reflection.
 *
 * @param <T> The type to be built.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 */
@SkipValidationProcessor
public abstract class NonValidatingBaseBuilder<T> extends OvalBuilder<T> {

    /**
     * Protected constructor.
     *
     * @param targetConstructor The pojo constructor to invoke on build.
     * @param <B> The builder type.
     */
    protected <B extends Builder<T>> NonValidatingBaseBuilder(final Function<B, T> targetConstructor) {
        super(targetConstructor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final List<ConstraintViolation> violations) {
        // Do nothing. This terminates super validate call chain before
        // reaching OvalBuilder thus ensuring that any constraint
        // violations were discovered by transformed child classes.
    }
}

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
package com.arpnetworking.commons.builder.annotations;

import com.arpnetworking.commons.builder.ValidationProcessor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that skips validation transformation using {@link ValidationProcessor}
 * on {@link com.arpnetworking.commons.builder.OvalBuilder} subclasses. This may be used if validation rules are not
 * supported by the transformer or for testing.
 *
 * <b>Important:</b> The annotation must be on the builder class and not its target pojo.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface SkipValidationProcessor {
}


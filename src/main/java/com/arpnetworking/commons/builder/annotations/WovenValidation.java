/*
 * Copyright 2017 Inscope Metrics Inc.
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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that indicates that the {@link com.arpnetworking.commons.builder.Builder} class
 * has the validations woven at compile time and therefore does not need reflective validation.
 *
 * <b>Note</b>: This class is automatically applied by the weaver and should not be added manually to any class.
 * <b>Important:</b> The annotation must be on the builder class and not its target pojo.
 *
 * @author Brandon Arp (brandon dot arp at inscopemetrics dot com)
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface WovenValidation {
}


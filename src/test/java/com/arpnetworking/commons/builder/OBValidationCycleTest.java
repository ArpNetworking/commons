/*
 * Copyright 2022 Brandon Arp
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

import com.google.common.collect.Lists;
import net.sf.oval.ConstraintViolation;
import net.sf.oval.constraint.AssertNullCheck;
import net.sf.oval.context.MapValueContext;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the {@link OBValidationCycle} class.
 *
 * @author Brandon Arp (brandon dot arp at inscopemetrics dot io)
 */
public class OBValidationCycleTest  {

    @Test
    public void testAddConstraintViolation() {
        final OBValidationCycle cycle = new OBValidationCycle(this);
        final ConstraintViolation constraint = new ConstraintViolation(
                new AssertNullCheck(),
                "the thing wasn't null",
                this,
                5,
                Lists.newArrayList(new MapValueContext(Integer.class, "timeout")));
        cycle.addConstraintViolation(constraint);
    }

    @Test
    public void testAddConstraintViolationVariation() {
        final OBValidationCycle cycle = new OBValidationCycle(this);
        cycle.addContext(new MapValueContext(Integer.class, "timeout"));
        cycle.addConstraintViolation(new AssertNullCheck(), "the thing wasn't null", 5);
        final Object rootObject = cycle.getRootObject();
        Assert.assertSame(this, rootObject);
    }

    @Test
    public void testMultipleAddConstraintViolation() {
        final OBValidationCycle cycle = new OBValidationCycle(this);
        final ConstraintViolation constraint1 = new ConstraintViolation(
                new AssertNullCheck(),
                "the thing wasn't null",
                this,
                5,
                Lists.newArrayList(new MapValueContext(Integer.class, "timeout")));
        final ConstraintViolation constraint2 = new ConstraintViolation(
                new AssertNullCheck(),
                "the thing wasn't null",
                this,
                10,
                Lists.newArrayList(new MapValueContext(Integer.class, "timeout")));
        cycle.addConstraintViolation(constraint1);
        cycle.addConstraintViolation(constraint2);
    }
}

/**
 * Copyright 2016 Groupon.com
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
package com.arpnetworking.commons.jackson.databind.module;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Test for the BuilderModule class.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 */
public class BuilderModuleTest {

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void test() {
        final BuilderModule module = new BuilderModule();
        module.setupModule(_context);
        Mockito.verify(_context).insertAnnotationIntrospector(Mockito.any(AnnotationIntrospectorPair.class));
    }

    @Mock
    private Module.SetupContext _context;
}

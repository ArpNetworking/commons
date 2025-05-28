/*
 * Copyright 2022 Inscope Metrics
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
import net.sf.oval.Check;
import net.sf.oval.ConstraintViolation;
import net.sf.oval.Validator;
import net.sf.oval.context.OValContext;

import java.util.Collections;
import java.util.List;

/**
 * Implementation of a {@link net.sf.oval.ValidationCycle} to hold context information
 * during validation.
 *
 * @author Brandon Arp (brandon dot arp at inscopemetrics dot io)
 */
public final class OBValidationCycle implements net.sf.oval.ValidationCycle {
    private final Object _rootValidatedObject;
    private List<ConstraintViolation> _violations = Collections.emptyList();
    private final List<OValContext> _contextPath = Lists.newArrayListWithCapacity(4);
    private final List<OValContext> _contextPathImmutable = Collections.unmodifiableList(_contextPath);

    /**
     * Public constructor.
     *
     * @param rootValidatedObject the object to validate
     */
    public OBValidationCycle(final Object rootValidatedObject) {
        this._rootValidatedObject = rootValidatedObject;
    }

    /**
     * Add a context frame.
     *
     * @param context the context frame
     */
    public void addContext(final OValContext context) {
        _contextPath.add(context);
    }

    @Override
    public void addConstraintViolation(final Check check, final String message, final Object invalidValue) {
        addConstraintViolation(new ConstraintViolation(check, message, _rootValidatedObject, invalidValue, _contextPathImmutable));
    }

    @Override
    public void addConstraintViolation(final ConstraintViolation violation) {
        if (_violations.isEmpty()) {
            _violations = Lists.newArrayList();
        }
        _violations.add(violation);
    }

    @Override
    public List<OValContext> getContextPath() {
        return _contextPathImmutable;
    }

    @Override
    public Object getRootObject() {
        return _rootValidatedObject;
    }

    @Override
    public Validator getValidator() {
        return null;
    }
}

/*
 * Copyright 2017 Inscope Metrics, Inc.
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

package com.arpnetworking.commons.java.util.function;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Implementation of {@code Supplier} that provides the same single instance
 * each time using double-check locking. This means that it is open to
 * reflective and serialization attacks in that it does not guarantee that only
 * a single instance of {@code <T>} exists in the entire application. However,
 * it does guarantee that ony a single instance is returned from this
 * {@code Supplier} for the life of the application.
 *
 * Dependencies:
 * <i>None</i>
 *
 * @param <T> the type of object supplied by this {@code Supplier}
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
public final class SingletonSupplier<T> implements Supplier<T> {

    /**
     * Public constructor.
     *
     * @param supplier the underlying {@code Supplier}
     */
    public SingletonSupplier(final Supplier<T> supplier) {
        _supplier = supplier;
        _lock = new ReentrantLock();
    }

    /* package private */ SingletonSupplier(final Supplier<T> supplier, final Lock lock) {
        _supplier = supplier;
        _lock = lock;
    }

    @Override
    public T get() {
        if (_reference == null) {
            try {
                _lock.lock();
                if (_reference == null) {
                    _reference = _supplier.get();
                }
            } finally {
                _lock.unlock();
            }
        }
        return _reference;
    }

    private final Supplier<T> _supplier;
    private final Lock _lock;
    private volatile T _reference;
}

/**
 * Copyright 2014 Groupon.com
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
package com.arpnetworking.commons.akka;

import akka.actor.Actor;
import akka.actor.IndirectActorProducer;
import akka.actor.Props;
import com.arpnetworking.logback.annotations.LogValue;
import com.arpnetworking.steno.LogValueMapFactory;
import com.google.inject.Injector;

/**
 * A Guice-based factory for Akka actors.
 *
 * Dependencies:
 * <ul>
 *     <li>com.google.inject:guice</li>
 *     <li>com.typesafe.akka.core:akka-actor_2.11</li>
 *     <li>org.scala-lang:scala-library</li>
 * </ul>
 *
 * @author Brandon Arp (barp at groupon dot com)
 */
public class GuiceActorCreator implements IndirectActorProducer {

    /**
     * Public constructor.
     *
     * @param injector the Guice injector to create actors from.
     * @param clazz the <code>Actor</code> class to instantiate.
     */
    public GuiceActorCreator(final Injector injector, final Class<? extends Actor> clazz) {
        _injector = injector;
        _clazz = clazz;
    }

    /**
     * Creates a <code>Props</code> for the <code>GuiceActorCreator</code>.
     *
     * @param injector the Guice injector to create actors from.
     * @param clazz the <code>Actor</code> class to instantiate.
     * @return a new <code>Props</code> instance.
     */
    public static Props props(final Injector injector, final Class<? extends Actor> clazz) {
        return Props.create(GuiceActorCreator.class, injector, clazz);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Actor produce() {
        return _injector.getInstance(_clazz);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<? extends Actor> actorClass() {
        return _clazz;
    }

    /**
     * Generate a Steno log compatible representation.
     *
     * @return Steno log compatible representation.
     */
    @LogValue
    public Object toLogValue() {
        return LogValueMapFactory.builder(this)
                .put("injector", _injector)
                .put("class", _clazz)
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toLogValue().toString();
    }

    private final Injector _injector;
    private final Class<? extends Actor> _clazz;

}

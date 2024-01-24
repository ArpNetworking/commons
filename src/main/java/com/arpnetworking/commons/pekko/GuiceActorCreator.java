/*
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
package com.arpnetworking.commons.pekko;

import com.google.inject.Injector;
import org.apache.pekko.actor.Actor;
import org.apache.pekko.actor.IndirectActorProducer;
import org.apache.pekko.actor.Props;

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
 * @author Brandon Arp (brandon dot arp at inscopemetrics dot io)
 */
public class GuiceActorCreator implements IndirectActorProducer {

    /**
     * Public constructor.
     *
     * @param injector the Guice injector to create actors from.
     * @param clazz the {@link Actor} class to instantiate.
     */
    public GuiceActorCreator(final Injector injector, final Class<? extends Actor> clazz) {
        _injector = injector;
        _clazz = clazz;
    }

    /**
     * Creates a {@link Props} for the {@link GuiceActorCreator}.
     *
     * @param injector the Guice injector to create actors from.
     * @param clazz the {@link Actor} class to instantiate.
     * @return a new {@link Props} instance.
     */
    public static Props props(final Injector injector, final Class<? extends Actor> clazz) {
        return Props.create(GuiceActorCreator.class, injector, clazz);
    }

    @Override
    public Actor produce() {
        return _injector.getInstance(_clazz);
    }

    @Override
    public Class<? extends Actor> actorClass() {
        return _clazz;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("injector=").append(_injector)
                .append(", class=").append(_clazz)
                .toString();
    }

    private final Injector _injector;
    private final Class<? extends Actor> _clazz;

}

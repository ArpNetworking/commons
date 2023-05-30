/*
 * Copyright 2018 Inscope Metrics, Inc
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
import akka.actor.ActorRef;
import akka.actor.ActorRefFactory;
import com.google.inject.Injector;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

/**
 * A dependency injection {@link Provider} to assist in the creation of Akka actors.
 *
 * @author Brandon Arp (brandon dot arp at inscopemetrics dot io)
 */
public final class ActorProvider implements Provider<ActorRef> {
    @Override
    public ActorRef get() {
        return _actorRefFactory.actorOf(GuiceActorCreator.props(_injector, _clazz), _name);
    }

    /**
     * Public constructor.
     *
     * @param name name of the actor to create
     * @param clazz class of the actor
     * @param actorRefFactory factory to build the actor as a child of
     */
    public ActorProvider(final String name, final Class<? extends Actor> clazz, final ActorRefFactory actorRefFactory) {
        _name = name;
        _clazz = clazz;
        _actorRefFactory = actorRefFactory;
    }

    /** package private for test */ Injector getInjector() {
        return _injector;
    }

    @Inject
    private Injector _injector;

    private final String _name;
    private final Class<? extends Actor> _clazz;
    private final ActorRefFactory _actorRefFactory;
}

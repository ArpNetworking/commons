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
package com.arpnetworking.commons.pekko;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorRefFactory;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for the ActorProvider class.
 *
 * @author Brandon Arp (brandon dot arp at inscopemetrics dot io)
 */
public final class ActorProviderTest {

    @Test
    public void testBuildsInRefFactory() {
        final String path = "testChild";
        final ActorRefFactory refFactory = Mockito.mock(ActorRefFactory.class);
        final ActorProvider provider = new ActorProvider(path, TestActor.class, refFactory);
        provider.get();
        Mockito.verify(refFactory).actorOf(Mockito.any(), Mockito.eq(path));
    }

    @Test
    public void testInjectorIsInjectedWithBinding() {
        final String path = "testChild";
        final ActorRefFactory refFactory = Mockito.mock(ActorRefFactory.class);
        final ActorProvider actorProvider = new ActorProvider(path, TestActor.class, refFactory);
        final AbstractModule mod = new TestActorBuilderModule(actorProvider);
        final Injector injector = Guice.createInjector(mod);
        injector.getInstance(ActorRef.class);
        Assert.assertSame(injector, actorProvider.getInjector());
    }

    private static final class TestActor extends AbstractActor {

        @Override
        public Receive createReceive() {
            return receiveBuilder().build();
        }
    }

    private static class TestActorBuilderModule extends AbstractModule {

        TestActorBuilderModule(final ActorProvider actorProvider) {
            _actorProvider = actorProvider;
        }

        @Override
        protected void configure() {
            bind(ActorRef.class).toProvider(_actorProvider);
        }

        private final ActorProvider _actorProvider;
    }
}

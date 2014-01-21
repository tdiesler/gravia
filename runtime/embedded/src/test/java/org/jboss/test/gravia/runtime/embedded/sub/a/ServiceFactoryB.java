/*
 * #%L
 * Gravia :: Runtime :: Embedded
 * %%
 * Copyright (C) 2013 - 2014 JBoss by Red Hat
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.jboss.test.gravia.runtime.embedded.sub.a;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Component(factory = ServiceFactoryB.FACTORY_ID, service = ServiceB.class)
public class ServiceFactoryB implements ServiceB {

    public static final String FACTORY_ID = "service.factory.b";

    static AtomicInteger INSTANCE_COUNT = new AtomicInteger();
    final String name = getClass().getSimpleName() + "#" + INSTANCE_COUNT.incrementAndGet();

    private Map<String, ?> configuration;

    @Activate
    void activate(ComponentContext context, Map<String, ?> configuration) {
        this.configuration = configuration;
    }

    @Deactivate
    void deactivate() {
    }

    @Override
    public String doStuff() {
        return name + ":" + configuration.get("key");
    }

    @Override
    public String toString() {
        return name;
    }
}

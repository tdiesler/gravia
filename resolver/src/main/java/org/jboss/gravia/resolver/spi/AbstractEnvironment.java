/*
 * #%L
 * Gravia :: Resolver
 * %%
 * Copyright (C) 2010 - 2014 JBoss by Red Hat
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
package org.jboss.gravia.resolver.spi;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.gravia.resolver.Environment;
import org.jboss.gravia.resource.MatchPolicy;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.spi.AbstractResourceStore;
import org.jboss.gravia.runtime.Wiring;

/**
 * An abstract {@link Environment}
 *
 * @author thomas.diesler@jboss.com
 * @since 06-May-2013
 */
public abstract class AbstractEnvironment extends AbstractResourceStore implements Environment {

    private final Map<Resource, Wiring> wirings;

    public AbstractEnvironment(String storeName, MatchPolicy matchPolicy) {
        super(storeName, matchPolicy);
        this.wirings = new ConcurrentHashMap<Resource, Wiring>();
    }

    public static AbstractEnvironment assertAbstractEnvironment(Environment env) {
        if (!(env instanceof AbstractEnvironment))
            throw new IllegalArgumentException("Not an AbstractEnvironment: " + env);
        return (AbstractEnvironment) env;
    }

    @Override
    public Map<Resource, Wiring> getWirings() {
        return Collections.unmodifiableMap(wirings);
    }

    public void putWiring(Resource resource, Wiring wiring) {
        wirings.put(resource, wiring);
    }
}

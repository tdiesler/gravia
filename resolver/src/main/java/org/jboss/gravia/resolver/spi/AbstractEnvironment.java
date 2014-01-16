/*
 * #%L
 * JBossOSGi Provision: Core
 * %%
 * Copyright (C) 2013 JBoss by Red Hat
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.jboss.gravia.resolver.spi;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.gravia.resolver.Environment;
import org.jboss.gravia.resource.MatchPolicy;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.Wiring;
import org.jboss.gravia.resource.spi.AbstractResourceStore;

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

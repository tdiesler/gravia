/*
 * #%L
 * JBossOSGi Resolver API
 * %%
 * Copyright (C) 2010 - 2013 JBoss by Red Hat
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.gravia.resolver.PreferencePolicy;
import org.jboss.gravia.resolver.ResolveContext;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.Namespace;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceStore;
import org.jboss.gravia.resource.Wiring;

/**
 * The abstract implementation of a {@link ResolveContext}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Apr-2012
 */
public abstract class AbstractResolveContext implements ResolveContext {

    private final ResourceStore resourceStore;
    private final List<Resource> mandatory;
    private final List<Resource> optional;
    private PreferencePolicy preferencePolicy;

    public AbstractResolveContext(ResourceStore resourceStore, Set<Resource> manres, Set<Resource> optres) {
        if (resourceStore == null)
            throw new IllegalArgumentException("Null resourceStore");

        this.resourceStore = resourceStore;
        this.mandatory = new ArrayList<Resource>(manres != null ? manres : Collections.<Resource> emptyList());
        this.optional = new ArrayList<Resource>(optres != null ? optres : Collections.<Resource> emptyList());

        // Verify that all resources are in the store
        for (Resource res : mandatory) {
            if (resourceStore.getResource(res.getIdentity()) == null)
                throw new IllegalArgumentException("Resource not in provided store: " + res);
        }
        for (Resource res : optional) {
            if (resourceStore.getResource(res.getIdentity()) == null)
                throw new IllegalArgumentException("Resource not in provided store: " + res);
        }

        // Remove already wired resources
        Map<Resource, Wiring> wirings = getWirings();
        Iterator<Resource> itres = mandatory.iterator();
        while (itres.hasNext()) {
            Resource res = itres.next();
            if (wirings.get(res) != null) {
                itres.remove();
            }
        }
        itres = optional.iterator();
        while (itres.hasNext()) {
            Resource res = itres.next();
            if (wirings.get(res) != null) {
                itres.remove();
            }
        }
    }

    protected abstract PreferencePolicy createPreferencePolicy();

    private PreferencePolicy getPreferencePolicyInternal() {
        if (preferencePolicy == null) {
            preferencePolicy = createPreferencePolicy();
        }
        return preferencePolicy;
    }

    @Override
    public Collection<Resource> getMandatoryResources() {
        return Collections.unmodifiableList(mandatory);
    }

    @Override
    public Collection<Resource> getOptionalResources() {
        return Collections.unmodifiableList(optional);
    }

    @Override
    public boolean isEffective(Requirement req) {
        // Ignore reqs that are not effective:=resolve
        String effective = req.getDirectives().get(Namespace.REQUIREMENT_EFFECTIVE_DIRECTIVE);
        return effective == null || effective.equals(Namespace.EFFECTIVE_RESOLVE);
    }

    @Override
    public Map<Resource, Wiring> getWirings() {
        Map<Resource, Wiring> wirings = new HashMap<Resource, Wiring>();
        Iterator<Resource> itres = resourceStore.getResources();
        while (itres.hasNext()) {
            Resource res = itres.next();
            Wiring wiring = res.getWiring();
            if (wiring != null) {
                wirings.put(res, wiring);
            }
        }
        return Collections.unmodifiableMap(wirings);
    }

    @Override
    public List<Capability> findProviders(Requirement req) {
        List<Capability> result = new ArrayList<Capability>();
        result.addAll(resourceStore.findProviders(req));
        getPreferencePolicyInternal().sort(result);
        return result;
    }
}

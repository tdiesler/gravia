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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.gravia.resolver.Environment;
import org.jboss.gravia.resolver.PreferencePolicy;
import org.jboss.gravia.resolver.ResolveContext;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.Namespace;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.runtime.Wiring;
import org.jboss.gravia.utils.IllegalArgumentAssertion;

/**
 * The abstract implementation of a {@link ResolveContext}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Apr-2012
 */
public abstract class AbstractResolveContext implements ResolveContext {

    private final Environment environment;
    private final List<Resource> mandatory;
    private final List<Resource> optional;
    private PreferencePolicy preferencePolicy;

    public AbstractResolveContext(Environment environment, Set<Resource> manres, Set<Resource> optres) {
        IllegalArgumentAssertion.assertNotNull(environment, "environment");

        this.environment = environment;
        this.mandatory = new ArrayList<Resource>(manres != null ? manres : Collections.<Resource> emptyList());
        this.optional = new ArrayList<Resource>(optres != null ? optres : Collections.<Resource> emptyList());

        // Verify that all resources are in the store
        for (Resource res : mandatory) {
            if (environment.getResource(res.getIdentity()) == null)
                throw new IllegalArgumentException("Resource not in provided store: " + res);
        }
        for (Resource res : optional) {
            if (environment.getResource(res.getIdentity()) == null)
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
        return environment.getWirings();
    }

    void putWiring(Resource resource, Wiring wiring) {
        AbstractEnvironment absenv = AbstractEnvironment.assertAbstractEnvironment(environment);
        absenv.putWiring(resource, wiring);
    }

    @Override
    public List<Capability> findProviders(Requirement req) {
        List<Capability> result = new ArrayList<Capability>();
        result.addAll(environment.findProviders(req));
        getPreferencePolicyInternal().sort(result);
        return result;
    }
}

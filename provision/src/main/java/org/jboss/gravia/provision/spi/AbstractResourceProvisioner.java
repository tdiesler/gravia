/*
 * #%L
 * JBossOSGi Provision: Core
 * %%
 * Copyright (C) 2013 JBoss by Red Hat
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
package org.jboss.gravia.provision.spi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jboss.gravia.provision.Environment;
import org.jboss.gravia.provision.ProvisionResult;
import org.jboss.gravia.provision.ResourceProvisioner;
import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.resolver.DefaultResolveContext;
import org.jboss.gravia.resolver.PreferencePolicy;
import org.jboss.gravia.resolver.ResolutionException;
import org.jboss.gravia.resolver.ResolveContext;
import org.jboss.gravia.resolver.Resolver;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.Wire;
import org.jboss.logging.Logger;

/**
 * An abstract {@link ResourceProvisioner}
 *
 * @author thomas.diesler@jboss.com
 * @since 06-May-2013
 */
public abstract class AbstractResourceProvisioner implements ResourceProvisioner {

    static final Logger LOGGER = Logger.getLogger(ResourceProvisioner.class.getPackage().getName());

    private final Resolver resolver;
    private final Repository repository;
    private PreferencePolicy preferencePolicy;

    public AbstractResourceProvisioner(Resolver resolver, Repository repository) {
        this.resolver = resolver;
        this.repository = repository;
    }

    protected abstract Environment cloneEnvironment(Environment env);

    @Override
    public final Resolver getResolver() {
        return resolver;
    }

    @Override
    public final Repository getRepository() {
        return repository;
    }

    protected abstract PreferencePolicy createPreferencePolicy();

    private PreferencePolicy getPreferencePolicyInternal() {
        if (preferencePolicy == null) {
            preferencePolicy = createPreferencePolicy();
        }
        return preferencePolicy;
    }

    @Override
    public final ProvisionResult findResources(Environment env, Set<Requirement> reqs) {
        if (env == null)
            throw new IllegalArgumentException("Null env");
        if (reqs == null)
            throw new IllegalArgumentException("Null reqs");

        LOGGER.debugf("START findResources: %s", reqs);

        // Install the unresolved resources into the cloned environment
        List<Resource> unresolved = new ArrayList<Resource>();
        Environment envclone = cloneEnvironment(env);
        for (Requirement req : reqs) {
            Resource res = req.getResource();
            if (env.getResource(res.getIdentity()) == null) {
                envclone.addResource(res);
                unresolved.add(res);
            }
        }

        // Find the resources in the cloned environment
        List<Resource> resources = new ArrayList<Resource>();
        Set<Requirement> unstatisfied = new HashSet<Requirement>(reqs);
        Map<Requirement, Resource> mapping = new HashMap<Requirement, Resource>();
        findResources(envclone, unresolved, mapping, unstatisfied, resources);

        // Remove abstract resources
        Iterator<Resource> itres = resources.iterator();
        while (itres.hasNext()) {
            Resource res = itres.next();
            if (res.isAbstract()) {
                itres.remove();
            }
        }

        AbstractProvisionResult result = new AbstractProvisionResult(mapping, unstatisfied, resources);
        LOGGER.debugf("END findResources");
        LOGGER.debugf("  resources: %s", result.getResources());
        LOGGER.debugf("  unsatisfied: %s", result.getUnsatisfiedRequirements());

        // Sanity check that we can resolve all result resources
        Set<Resource> mandatory = new LinkedHashSet<Resource>();
        mandatory.addAll(resources);
        try {
            ResolveContext context = new DefaultResolveContext(envclone, mandatory, null);
            resolver.resolve(context).entrySet();
        } catch (ResolutionException ex) {
            LOGGER.warnf(ex, "Cannot resolve provisioner result");
        }

        return result;
    }

    private void findResources(Environment env, List<Resource> unresolved, Map<Requirement, Resource> mapping, Set<Requirement> unstatisfied,
            List<Resource> resources) {

        // Resolve the unsatisfied reqs in the environment
        resolveInEnvironment(env, unresolved, mapping, unstatisfied, resources);
        if (unstatisfied.isEmpty())
            return;

        boolean envModified = false;
        Set<Resource> installable = new HashSet<Resource>();

        LOGGER.debugf("Finding unsatisfied reqs");

        Iterator<Requirement> itun = unstatisfied.iterator();
        while (itun.hasNext()) {
            Requirement req = itun.next();

            // Ignore requirements that are already in the environment
            if (!env.findProviders(req).isEmpty()) {
                continue;
            }

            // Continue if we cannot find a provider for a given requirement
            Capability cap = findProviderInRepository(req);
            if (cap == null) {
                continue;
            }

            installable.add(cap.getResource());
        }

        // Install the resources that match the unsatisfied reqs
        for (Resource res : installable) {
            if (!resources.contains(res)) {
                Collection<Requirement> reqs = getRequirements(res, null);
                Iterator<Requirement> itreqs = reqs.iterator();
                while (itreqs.hasNext()) {
                    Requirement req = itreqs.next();
                    if (req.isOptional() || env.findProviders(req).size() > 0) {
                        itreqs.remove();
                    }
                }
                LOGGER.debugf("Adding %d unsatisfied reqs", reqs.size());
                unstatisfied.addAll(reqs);
                env.addResource(res);
                resources.add(res);
                envModified = true;
            }
        }

        // Recursivly find the missing resources
        if (envModified) {
            findResources(env, unresolved, mapping, unstatisfied, resources);
        }
    }

    private Collection<Requirement> getRequirements(Resource res, String[] namespaces) {
        Set<Requirement> reqs = new HashSet<Requirement>();
        if (namespaces != null) {
            for (String ns : namespaces) {
                for (Requirement req : res.getRequirements(ns)) {
                    reqs.add(req);
                }
            }
        } else {
            for (Requirement req : res.getRequirements(null)) {
                reqs.add(req);
            }
        }
        return reqs;
    }

    private Capability findProviderInRepository(Requirement req) {

        // Find the providers in the repository
        LOGGER.debugf("Find in repository: %s", req);
        Collection<Capability> providers = repository.findProviders(req);

        // Remove abstract resources
        if (providers.size() > 1) {
            providers = new ArrayList<Capability>(providers);
            Iterator<Capability> itcap = providers.iterator();
            while (itcap.hasNext()) {
                Capability cap = itcap.next();
                if (cap.getResource().isAbstract()) {
                    itcap.remove();
                }
            }
        }

        Capability cap = null;
        if (providers.size() == 1) {
            cap = providers.iterator().next();
            LOGGER.debugf(" Found one: %s", cap);
        } else if (providers.size() > 1) {
            List<Capability> sorted = new ArrayList<Capability>(providers);
            getPreferencePolicyInternal().sort(sorted);
            LOGGER.debugf(" Found multiple: %s", sorted);
            cap = sorted.get(0);
        } else {
            LOGGER.debugf(" Not found: %s", req);
        }
        return cap;
    }

    private void resolveInEnvironment(Environment env, List<Resource> unresolved, Map<Requirement, Resource> mapping, Set<Requirement> unstatisfied, List<Resource> resources) {
        Set<Resource> mandatory = new LinkedHashSet<Resource>();
        mandatory.addAll(unresolved);
        mandatory.addAll(resources);
        try {
            ResolveContext context = new DefaultResolveContext(env, mandatory, null);
            Set<Entry<Resource, List<Wire>>> wiremap = resolver.resolve(context).entrySet();
            for (Entry<Resource, List<Wire>> entry : wiremap) {
                Iterator<Requirement> itunsat = unstatisfied.iterator();
                while (itunsat.hasNext()) {
                    Requirement req = itunsat.next();
                    for (Wire wire : entry.getValue()) {
                        if (wire.getRequirement() == req) {
                            Resource provider = wire.getProvider();
                            mapping.put(req, provider);
                        }
                    }
                }
            }
            unstatisfied.clear();
        } catch (ResolutionException ex) {
            for (Requirement req : ex.getUnresolvedRequirements()) {
                LOGGER.debugf(" unresolved: %s", req);
            }
        }
    }

    static class AbstractProvisionResult implements ProvisionResult {

        private final Map<Requirement, Resource> mapping;
        private final Set<Requirement> unsatisfied;
        private final List<Resource> resources;

        public AbstractProvisionResult(Map<Requirement, Resource> mapping, Set<Requirement> unstatisfied, List<Resource> resources) {
            this.mapping = mapping;
            this.unsatisfied = unstatisfied;
            this.resources = resources;
        }

        @Override
        public Map<Requirement, Resource> getRequirementMapping() {
            return Collections.unmodifiableMap(mapping);
        }

        @Override
        public List<Resource> getResources() {
            return Collections.unmodifiableList(resources);
        }

        @Override
        public Set<Requirement> getUnsatisfiedRequirements() {
            return Collections.unmodifiableSet(unsatisfied);
        }
    }
}

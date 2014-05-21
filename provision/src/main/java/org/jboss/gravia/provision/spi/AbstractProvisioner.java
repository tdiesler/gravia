/*
 * #%L
 * Gravia :: Provision
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
package org.jboss.gravia.provision.spi;

import static org.jboss.gravia.provision.spi.ProvisionLogger.LOGGER;

import java.io.InputStream;
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

import org.jboss.gravia.provision.ProvisionException;
import org.jboss.gravia.provision.ProvisionResult;
import org.jboss.gravia.provision.Provisioner;
import org.jboss.gravia.provision.ResourceHandle;
import org.jboss.gravia.provision.ResourceInstaller;
import org.jboss.gravia.provision.ResourceInstaller.Context;
import org.jboss.gravia.repository.MavenIdentityRequirementBuilder;
import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.resolver.DefaultPreferencePolicy;
import org.jboss.gravia.resolver.DefaultResolveContext;
import org.jboss.gravia.resolver.Environment;
import org.jboss.gravia.resolver.PreferencePolicy;
import org.jboss.gravia.resolver.ResolutionException;
import org.jboss.gravia.resolver.ResolveContext;
import org.jboss.gravia.resolver.Resolver;
import org.jboss.gravia.resolver.spi.AbstractEnvironment;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.ContentNamespace;
import org.jboss.gravia.resource.DefaultResourceBuilder;
import org.jboss.gravia.resource.IdentityNamespace;
import org.jboss.gravia.resource.MavenCoordinates;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceBuilder;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.runtime.DefaultWire;
import org.jboss.gravia.runtime.DefaultWiring;
import org.jboss.gravia.runtime.Wire;
import org.jboss.gravia.runtime.Wiring;
import org.jboss.gravia.utils.IllegalArgumentAssertion;
import org.jboss.gravia.utils.IllegalStateAssertion;
import org.jboss.gravia.utils.ResourceUtils;

/**
 * An abstract {@link Provisioner}
 *
 * @author thomas.diesler@jboss.com
 * @since 06-May-2013
 */
public abstract class AbstractProvisioner implements Provisioner {

    private final Resolver resolver;
    private final Repository repository;
    private final Environment environment;
    private final ResourceInstaller installer;
    private final PreferencePolicy preferencePolicy;

    public AbstractProvisioner(Environment environment, Resolver resolver, Repository repository, ResourceInstaller installer) {
        this(environment, resolver, repository, installer, new DefaultPreferencePolicy(null));
    }

    public AbstractProvisioner(Environment environment, Resolver resolver, Repository repository, ResourceInstaller installer, PreferencePolicy policy) {
        IllegalArgumentAssertion.assertNotNull(environment, "environment");
        IllegalArgumentAssertion.assertNotNull(resolver, "resolver");
        IllegalArgumentAssertion.assertNotNull(repository, "repository");
        IllegalArgumentAssertion.assertNotNull(installer, "installer");
        IllegalArgumentAssertion.assertNotNull(policy, "policy");
        this.environment = environment;
        this.resolver = resolver;
        this.repository = repository;
        this.installer = installer;
        this.preferencePolicy = policy;
    }

    @Override
    public Environment getEnvironment() {
        return environment;
    }

    @Override
    public final Resolver getResolver() {
        return resolver;
    }

    @Override
    public final Repository getRepository() {
        return repository;
    }

    @Override
    public ResourceInstaller getResourceInstaller() {
        return installer;
    }

    @Override
    public ProvisionResult findResources(Set<Requirement> reqs) {
        return findResources(getEnvironment().cloneEnvironment(), reqs);
    }

    @Override
    public ProvisionResult findResources(Environment env, Set<Requirement> reqs) {
        IllegalArgumentAssertion.assertNotNull(env, "env");
        IllegalArgumentAssertion.assertNotNull(reqs, "reqs");

        LOGGER.debug("START findResources: {}", reqs);

        // Install the unresolved resources into the cloned environment
        List<Resource> unresolved = new ArrayList<Resource>();
        for (Requirement req : reqs) {
            Resource res = req.getResource();
            if (env.getResource(res.getIdentity()) == null) {
                env.addResource(res);
                unresolved.add(res);
            }
        }

        // Find the resources in the cloned environment
        List<Resource> resources = new ArrayList<Resource>();
        Set<Requirement> unstatisfied = new HashSet<Requirement>(reqs);
        Map<Requirement, Resource> mapping = new HashMap<Requirement, Resource>();
        findResources(env, unresolved, mapping, unstatisfied, resources);

        // Remove abstract resources
        Iterator<Resource> itres = resources.iterator();
        while (itres.hasNext()) {
            Resource res = itres.next();
            if (ResourceUtils.isAbstract(res)) {
                itres.remove();
            }
        }

        // Sort the provisioner result
        List<Resource> sorted = new ArrayList<Resource>();
        for (Resource res : resources) {
            sortResultResources(res, mapping, resources, sorted);
        }

        ProvisionResult result = new DefaultProvisionResult(sorted, mapping, env.getWirings(), unstatisfied);
        LOGGER.debug("END findResources");
        LOGGER.debug("  resources: {}", result.getResources());
        LOGGER.debug("  unsatisfied: {}", result.getUnsatisfiedRequirements());

        // Sanity check that we can resolve all result resources
        Set<Resource> mandatory = new LinkedHashSet<Resource>();
        mandatory.addAll(result.getResources());
        try {
            ResolveContext context = new DefaultResolveContext(env, mandatory, null);
            resolver.resolveAndApply(context).entrySet();
        } catch (ResolutionException ex) {
            LOGGER.warn("Cannot resolve provisioner result", ex);
        }

        return result;
    }

    @Override
    public Set<ResourceHandle> provisionResources(Set<Requirement> reqs) throws ProvisionException {

        // Find resources
        Environment env = getEnvironment();
        Environment envclone = env.cloneEnvironment();
        ProvisionResult result = findResources(envclone, reqs);

        Set<Requirement> unsatisfied = result.getUnsatisfiedRequirements();
        if (!unsatisfied.isEmpty()) {
            throw new ProvisionException("Cannot resolve unsatisfied requirements: " + unsatisfied);
        }

        // NOTE: installing resources and updating the wiring is not an atomic operation

        // Install resources
        List<Resource> resources = result.getResources();
        Map<Requirement, Resource> mapping = result.getMapping();
        DefaultInstallerContext context = new DefaultInstallerContext(resources, mapping);
        Set<ResourceHandle> handles = installResources(context);

        // Update the wirings
        updateResourceWiring(env, result);

        return handles;
    }

    @Override
    public void updateResourceWiring(Environment environment, ProvisionResult result) {
        Map<Resource, Wiring> auxwirings = result.getWirings();
        for (Entry<Resource, Wiring> entry : auxwirings.entrySet()) {
            Resource auxres = entry.getKey();
            Resource envres = environment.getResource(auxres.getIdentity());
            DefaultWiring envwiring = new DefaultWiring(envres, null, null);
            Wiring auxwiring = entry.getValue();
            for (Wire auxwire : auxwiring.getProvidedResourceWires(null)) {
                Capability auxcap = auxwire.getCapability();
                Capability envcap = findTargetCapability(auxcap);
                Requirement auxreq = auxwire.getRequirement();
                Requirement envreq = findTargetRequirement(auxreq);
                envwiring.addProvidedWire(new DefaultWire(envreq, envcap));
            }
            for (Wire auxwire : auxwiring.getRequiredResourceWires(null)) {
                Capability auxcap = auxwire.getCapability();
                Capability envcap = findTargetCapability(auxcap);
                Requirement auxreq = auxwire.getRequirement();
                Requirement envreq = findTargetRequirement(auxreq);
                envwiring.addRequiredWire(new DefaultWire(envreq, envcap));
            }
            AbstractEnvironment absenv = AbstractEnvironment.assertAbstractEnvironment(environment);
            absenv.putWiring(envres, envwiring);
        }
    }

    private Set<ResourceHandle> installResources(Context context) throws ProvisionException {
        IllegalArgumentAssertion.assertNotNull(context, "context");
        Set<ResourceHandle> handles = new HashSet<ResourceHandle>();
        for (Resource res : context.getResources()) {
            handles.add(installer.installResource(context, res));
        }
        return Collections.unmodifiableSet(handles);
    }

    @Override
    public ResourceBuilder getContentResourceBuilder(ResourceIdentity identity, InputStream inputStream) {
        IllegalArgumentAssertion.assertNotNull(identity, "identity");
        IllegalArgumentAssertion.assertNotNull(inputStream, "inputStream");
        ResourceBuilder builder = new DefaultResourceBuilder();
        builder.addIdentityCapability(identity);
        builder.addContentCapability(inputStream);
        return builder;
    }

    @Override
    public ResourceBuilder getMavenResourceBuilder(ResourceIdentity identity, MavenCoordinates mavenid) {
        IllegalArgumentAssertion.assertNotNull(mavenid, "mavenid");

        Requirement ireq = new MavenIdentityRequirementBuilder(mavenid).getRequirement();
        Collection<Capability> providers = getRepository().findProviders(ireq);
        IllegalStateAssertion.assertFalse(providers.isEmpty(), "Cannot find providers for requirement: " + ireq);

        Resource mvnres = providers.iterator().next().getResource();

        // Copy the mvn resource to another resource with the given identity
        DefaultResourceBuilder builder = new DefaultResourceBuilder();
        Capability icap = identity != null ? builder.addIdentityCapability(identity) : builder.addIdentityCapability(mavenid);
        icap.getAttributes().put(ContentNamespace.CAPABILITY_MAVEN_IDENTITY_ATTRIBUTE, mavenid);
        for (Capability cap : mvnres.getCapabilities(null)) {
            if (!IdentityNamespace.IDENTITY_NAMESPACE.equals(cap.getNamespace())) {
                builder.addCapability(cap.getNamespace(), cap.getAttributes(), cap.getDirectives());
            }
        }
        for (Requirement req : mvnres.getRequirements(null)) {
            builder.addRequirement(req.getNamespace(), req.getAttributes(), req.getDirectives());
        }

        return builder;
    }

    @Override
    public ResourceHandle installResource(Resource resource) throws ProvisionException {
        return installResourceInternal(resource);
    }

    @Override
    public ResourceHandle installSharedResource(Resource resource) throws ProvisionException {
        if (!ResourceUtils.isShared(resource)) {
            ResourceBuilder builder = new DefaultResourceBuilder().fromResource(resource);
            Capability icap = builder.getMutableResource().getIdentityCapability();
            icap.getAttributes().put(IdentityNamespace.CAPABILITY_SHARED_ATTRIBUTE, Boolean.TRUE);
            resource = builder.getResource();
        }
        return installResourceInternal(resource);
    }

    private synchronized ResourceHandle installResourceInternal(Resource resource) throws ProvisionException {
        IllegalArgumentAssertion.assertNotNull(resource, "resource");
        Context context = new DefaultInstallerContext(resource);
        return installer.installResource(context, resource);
    }

    private Capability findTargetCapability(Capability auxcap) {
        Resource auxres = auxcap.getResource();
        Resource envres = environment.getResource(auxres.getIdentity());
        if (auxres == envres)
            return auxcap;
        for (Capability cap : envres.getCapabilities(null)) {
            boolean nsmatch = cap.getNamespace().equals(auxcap.getNamespace());
            boolean attsmatch = cap.getAttributes().equals(auxcap.getAttributes());
            boolean dirsmatch = cap.getDirectives().equals(auxcap.getDirectives());
            if (nsmatch && attsmatch && dirsmatch)
                return cap;
        }
        throw new IllegalStateException("Cannot find target capability for: " + auxcap);
    }

    private Requirement findTargetRequirement(Requirement auxreq) {
        Resource auxres = auxreq.getResource();
        Resource envres = environment.getResource(auxres.getIdentity());
        if (auxres == envres)
            return auxreq;
        for (Requirement req : envres.getRequirements(null)) {
            boolean nsmatch = req.getNamespace().equals(auxreq.getNamespace());
            boolean attsmatch = req.getAttributes().equals(auxreq.getAttributes());
            boolean dirsmatch = req.getDirectives().equals(auxreq.getDirectives());
            if (nsmatch && attsmatch && dirsmatch)
                return req;
        }
        throw new IllegalStateException("Cannot find target requirement for: " + auxreq);
    }

    // Sort mapping targets higher in the list. This should result in resource installations
    // without dependencies on resources from the same provioner result set.
    private void sortResultResources(Resource res, Map<Requirement, Resource> mapping, List<Resource> resources, List<Resource> result) {
        if (!result.contains(res)) {
            for (Requirement req : res.getRequirements(null)) {
                Resource target = mapping.get(req);
                if (target != null && resources.contains(target)) {
                    sortResultResources(target, mapping, resources, result);
                }
            }
            result.add(res);
        }
    }

    private void findResources(Environment env, List<Resource> unresolved, Map<Requirement, Resource> mapping, Set<Requirement> unstatisfied, List<Resource> resources) {

        // Resolve the unsatisfied reqs in the environment
        resolveInEnvironment(env, unresolved, mapping, unstatisfied, resources);
        if (unstatisfied.isEmpty())
            return;

        boolean envModified = false;
        Set<Resource> installable = new HashSet<Resource>();

        LOGGER.debug("Finding unsatisfied reqs");

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
                Collection<Requirement> reqs = res.getRequirements(null);
                LOGGER.debug("Adding %d unsatisfied reqs", reqs.size());
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

    private Capability findProviderInRepository(Requirement req) {

        // Find the providers in the repository
        LOGGER.debug("Find in repository: {}", req);
        Collection<Capability> providers = repository.findProviders(req);

        // Remove abstract resources
        if (providers.size() > 1) {
            providers = new ArrayList<Capability>(providers);
            Iterator<Capability> itcap = providers.iterator();
            while (itcap.hasNext()) {
                Capability cap = itcap.next();
                if (ResourceUtils.isAbstract(cap.getResource())) {
                    itcap.remove();
                }
            }
        }

        Capability cap = null;
        if (providers.size() == 1) {
            cap = providers.iterator().next();
            LOGGER.debug(" Found one: {}", cap);
        } else if (providers.size() > 1) {
            List<Capability> sorted = new ArrayList<Capability>(providers);
            preferencePolicy.sort(sorted);
            LOGGER.debug(" Found multiple: {}", sorted);
            cap = sorted.get(0);
        } else {
            LOGGER.debug(" Not found: {}", req);
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
                for (Wire wire : entry.getValue()) {
                    Requirement req = wire.getRequirement();
                    Resource provider = wire.getProvider();
                    mapping.put(req, provider);
                }
            }
            unstatisfied.clear();
        } catch (ResolutionException ex) {
            for (Requirement req : ex.getUnresolvedRequirements()) {
                LOGGER.debug(" unresolved: {}", req);
            }
        }
    }

    static class DefaultProvisionResult implements ProvisionResult {

        private final Map<Resource, Wiring> wirings;
        private final Map<Requirement, Resource> mapping;
        private final Set<Requirement> unsatisfied;
        private final List<Resource> resources;

        DefaultProvisionResult(List<Resource> resources, Map<Requirement, Resource> mapping, Map<Resource, Wiring> wirings, Set<Requirement> unstatisfied) {
            this.resources = resources;
            this.mapping = mapping;
            this.wirings = wirings;
            this.unsatisfied = unstatisfied;
        }

        @Override
        public List<Resource> getResources() {
            return Collections.unmodifiableList(resources);
        }

        @Override
        public Map<Requirement, Resource> getMapping() {
            return Collections.unmodifiableMap(mapping);
        }

        @Override
        public Map<Resource, Wiring> getWirings() {
            return Collections.unmodifiableMap(wirings);
        }

        @Override
        public Set<Requirement> getUnsatisfiedRequirements() {
            return Collections.unmodifiableSet(unsatisfied);
        }
    }
}

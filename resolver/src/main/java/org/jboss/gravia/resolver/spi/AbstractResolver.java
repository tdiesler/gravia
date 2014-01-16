/*
 * #%L
 * JBossOSGi Resolver Felix
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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import org.jboss.gravia.resolver.ResolutionException;
import org.jboss.gravia.resolver.ResolveContext;
import org.jboss.gravia.resolver.Resolver;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.Wire;
import org.jboss.gravia.resource.Wiring;
import org.jboss.gravia.resource.spi.AbstractResource;
import org.jboss.gravia.resource.spi.AbstractWire;
import org.jboss.gravia.resource.spi.AbstractWiring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract resolver {@link Resolver}.
 *
 * The resolver maintains order on all levels.
 * This should guarantee reproducable results.
 *
 * @author thomas.diesler@jboss.com
 * @since 31-May-2010
 */
public abstract class AbstractResolver implements Resolver {

    static final Logger LOGGER = LoggerFactory.getLogger(Resolver.class.getPackage().getName());

    protected abstract AbstractWire createWire(Requirement req, Capability cap);

    protected abstract AbstractWiring createWiring(Resource resource, List<Wire> reqwires, List<Wire> provwires);

    @Override
    public Map<Resource, List<Wire>> resolve(ResolveContext context) throws ResolutionException {
        return resolveInternal((AbstractResolveContext) context, false);
    }

    @Override
    public Map<Resource, List<Wire>> resolveAndApply(ResolveContext context) throws ResolutionException {
        return resolveInternal((AbstractResolveContext) context, true);
    }

    ResourceSpaces createResourceSpaces(ResolveContext context) {
        return new ResourceSpaces(context);
    }

    ResourceCandidates createResourceCandidates(Resource res) {
        return new ResourceCandidates(res);
    }

    private Map<Resource, List<Wire>> resolveInternal(AbstractResolveContext context, boolean apply) throws ResolutionException {

        LOGGER.debug("Resolve: mandatory{} optional{}", context.getMandatoryResources(), context.getOptionalResources());

        // Get the combined set of resources in the context
        Set<Resource> combined = new LinkedHashSet<Resource>();
        combined.addAll(context.getMandatoryResources());
        combined.addAll(context.getOptionalResources());

        // Resolve combined resources
        ResolverState state = new ResolverState(context);
        for (Resource res : combined) {
            resolveResource(state, res);
        }

        // Log resolver result
        Map<Resource, List<Wire>> resourceWires = state.getResult();
        if (LOGGER.isDebugEnabled()) {
            for (Entry<Resource, List<Wire>> entry : resourceWires.entrySet()) {
                LOGGER.debug("Resolved: {}", entry.getKey());
                for (Wire wire : entry.getValue()) {
                    LOGGER.debug("   {}", wire);
                }
            }
        }

        // Apply resolver results
        if (apply) {
            Map<Resource, Wiring> wirings = context.getWirings();
            for (Entry<Resource, List<Wire>> entry : resourceWires.entrySet()) {
                AbstractResource requirer = (AbstractResource) entry.getKey();
                List<Wire> reqwires = entry.getValue();
                AbstractWiring reqwiring = (AbstractWiring) wirings.get(requirer);
                if (reqwiring == null) {
                    reqwiring = createWiring(requirer, reqwires, null);
                    context.putWiring(requirer, reqwiring);
                } else {
                    for (Wire wire : reqwires) {
                        reqwiring.addRequiredWire(wire);
                    }
                }
                for (Wire wire : reqwires) {
                    AbstractResource provider = (AbstractResource) wire.getProvider();
                    AbstractWiring provwiring = (AbstractWiring) wirings.get(provider);
                    if (provwiring == null) {
                        provwiring = createWiring(provider, null, null);
                        context.putWiring(provider, provwiring);
                    }
                    provwiring.addProvidedWire(wire);
                }
            }
        }

        return resourceWires;
    }

    private ResourceSpace resolveResource(ResolverState state, Resource res) throws ResolutionException {

        // Check if we already have a resolved space for resource
        ResourceSpaces spaces = state.getResourceSpaces();
        ResourceSpace resspace = spaces.getResourceSpace(res);
        if (resspace != null)
            return resspace;

        // A resource can resolve when the spaces of all its immediate dependencies can be added
        ResolveContext context = state.getResolveContext();
        ResourceCandidates rescan = new ResourceCandidates(res);
        Iterator<List<Wire>> itres = rescan.iterator(context);
        while (itres.hasNext()) {
            Wiring wiring = context.getWirings().get(res);
            ResourceSpace space = new ResourceSpace(res, wiring);
            List<Wire> wires = itres.next();
            boolean allgood = true;
            for (Wire wire : wires) {
                Resource provider = wire.getProvider();
                ResourceSpace provspace = resolveResource(state, provider);
                if (!space.addDependencySpace(provspace)) {
                    allgood = false;
                    break;
                }
            }
            if (allgood) {
                spaces.addResourceSpace(space);
                state.getResult().put(res, wires);
                return space;
            }
        }

        ResolutionException resex = rescan.getResolutionException();
        if (resex != null) {
            throw resex;
        }

        if (spaces.getResourceSpace(res) == null) {
            List<Requirement> manreqs = res.getRequirements(null);
            Iterator<Requirement> itreqs = manreqs.iterator();
            while (itreqs.hasNext()) {
                if (itreqs.next().isOptional()) {
                    itreqs.remove();
                }
            }
            throw new ResolutionException("Requirements map to candidates in disconnetced spaces", null, manreqs);
        }
        return null;
    }

    private class ResolverState {

        private final ResourceSpaces spaces;
        private final ResolveContext context;
        private final Map<Resource, List<Wire>> wiremap = new LinkedHashMap<Resource, List<Wire>>();

        ResolverState(ResolveContext context) {
            this.context = context;
            this.spaces = new ResourceSpaces(context);
        }

        ResourceSpaces getResourceSpaces() {
            return spaces;
        }

        ResolveContext getResolveContext() {
            return context;
        }

        Map<Resource, List<Wire>> getResult() {
            return wiremap;
        }
    }

    class ResourceSpaces {

        private final Map<Resource, ResourceSpace> spacemap = new LinkedHashMap<Resource, ResourceSpace>();

        // Initially contain spaces for all wired resources
        ResourceSpaces(ResolveContext context) {
            Map<Resource, Wiring> wirings = context.getWirings();
            for (Resource res : wirings.keySet()) {
                Wiring wiring = wirings.get(res);
                spacemap.put(res, new ResourceSpace(res, wiring));
            }
        }

        ResourceSpaces(ResourceSpaces parent) {
            spacemap.putAll(parent.spacemap);
        }

        void addResourceSpace(ResourceSpace space) {
            Resource primary = space.getPrimary();
            assert !spacemap.containsKey(primary) : "spaces does not contain: " + primary;
            spacemap.put(primary, space);
        }

        Map<Resource, ResourceSpace> getResourceSpaces() {
            return Collections.unmodifiableMap(spacemap);
        }

        ResourceSpace getResourceSpace(Resource res) {
            return spacemap.get(res);
        }
    }

    /**
     * A {@link ResourceSpace} is that of a primary {@link Resource} and its immediate dependencies.
     * A space cannot contain two versions of the same {@link Resource}.
     */
    class ResourceSpace {

        private final Resource primary;
        private final Map<String, Resource> resources = new LinkedHashMap<String, Resource>();

        ResourceSpace(Resource primary, Wiring wiring) {
            this.primary = primary;

            String uniquekey = primary.getIdentity().getSymbolicName();
            resources.put(uniquekey, primary);

            if (wiring != null) {
                for (Wire wire : wiring.getRequiredResourceWires(null)) {
                    Resource provider = wire.getProvider();
                    uniquekey = provider.getIdentity().getSymbolicName();
                    resources.put(uniquekey, provider);
                }
            }
        }

        Resource getPrimary() {
            return primary;
        }

        Collection<Resource> getResources() {
            return Collections.unmodifiableCollection(resources.values());
        }

        boolean addDependencySpace(ResourceSpace dependency) {
            if (dependency == null)
                return false;

            for (Resource aux : dependency.getResources()) {
                String uniquekey = aux.getIdentity().getSymbolicName();
                Resource other = resources.get(uniquekey);
                if (other != null && other != aux) {
                    return false;
                }
            }
            for (Resource aux : dependency.getResources()) {
                String uniquekey = aux.getIdentity().getSymbolicName();
                resources.put(uniquekey, aux);
            }
            return true;
        }

        @Override
        public String toString() {
            return "ResourceSpace[" + primary.getIdentity() + "]";
        }
    }

    /**
     * Provides an iterator over all possible wires for a given resource
     */
    class ResourceCandidates {
        private final Resource res;
        private ResolutionException resolutionException;

        ResourceCandidates(Resource res) {
            this.res = res;
        }

        ResolutionException getResolutionException() {
            return resolutionException;
        }

        Iterator<List<Wire>> iterator(final ResolveContext context) throws ResolutionException {
            return new Iterator<List<Wire>>() {
                private List<Iterator<Wire>> candidates = new ArrayList<Iterator<Wire>>();
                private List<Requirement> reqs = res.getRequirements(null);
                private Map<Requirement, Wire> wires;
                private boolean hasNext;

                @Override
                public boolean hasNext() {
                    try {
                        if (wires == null) {
                            wires = new LinkedHashMap<Requirement, Wire>();
                            initWiremap(context, 0);
                            hasNext = true;
                            return true;
                        }
                        int index = reqs.size() - 1;
                        while (index >= 0) {
                            if (candidates.get(index).hasNext()) {
                                Wire wire = candidates.get(index).next();
                                Requirement req = wire.getRequirement();
                                wires.put(req, wire);
                                hasNext = true;
                                return true;
                            } else {
                                if (index > 0 && candidates.get(index - 1).hasNext()) {
                                    initWiremap(context, index);
                                }
                                index--;
                            }
                        }
                    } catch (ResolutionException ex) {
                        resolutionException = ex;
                    }
                    hasNext = false;
                    return false;
                }

                private void initWiremap(final ResolveContext context, int start) throws ResolutionException {
                    for (int i = start; i < reqs.size(); i++) {
                        Requirement req = reqs.get(i);
                        RequirementCandidates reqcan = new RequirementCandidates(req);
                        Iterator<Wire> itcan = reqcan.iterator(context);
                        if (start == 0) {
                            candidates.add(itcan);
                        } else {
                            candidates.set(i, itcan);
                        }
                        if (itcan.hasNext()) {
                            wires.put(req, itcan.next());
                        }
                    }
                }

                @Override
                public List<Wire> next() {
                    if (!hasNext)
                        throw new NoSuchElementException();
                    ArrayList<Wire> next = new ArrayList<Wire>(wires.values());
                    return Collections.unmodifiableList(next);
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    /**
     * Provides an iterator over all possible wires for a given requirement
     */
    class RequirementCandidates {
        private final Requirement req;

        RequirementCandidates(Requirement req) {
            this.req = req;
        }

        Iterator<Wire> iterator(ResolveContext context) throws ResolutionException {
            final Collection<Resource> optres = context.getOptionalResources();
            final List<Capability> providers = context.findProviders(req);

            // Fail early if there are no providers for a non-optional requirement
            if (!optres.contains(req.getResource()) && !req.isOptional() && providers.isEmpty()) {
                Set<Requirement> unresolved = Collections.singleton(req);
                throw new ResolutionException("Cannot find provider for: " + req, null, unresolved);
            }

            return new Iterator<Wire>() {
                Iterator<Capability> delagate = providers.iterator();

                @Override
                public boolean hasNext() {
                    return delagate.hasNext();
                }

                @Override
                public Wire next() {
                    Capability cap = delagate.next();
                    return createWire(req, cap);
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }
}

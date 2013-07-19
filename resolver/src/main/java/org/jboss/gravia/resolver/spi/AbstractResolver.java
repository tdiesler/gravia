/*
 * #%L
 * JBossOSGi Resolver Felix
 * %%
 * Copyright (C) 2010 - 2012 JBoss by Red Hat
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jboss.gravia.resolver.DefaultPreferencePolicy;
import org.jboss.gravia.resolver.PreferencePolicy;
import org.jboss.gravia.resolver.ResolutionException;
import org.jboss.gravia.resolver.ResolveContext;
import org.jboss.gravia.resolver.Resolver;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.DefaultResourceStore;
import org.jboss.gravia.resource.DefaultWire;
import org.jboss.gravia.resource.DefaultWiring;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceStore;
import org.jboss.gravia.resource.Wire;
import org.jboss.gravia.resource.Wiring;
import org.jboss.gravia.resource.spi.AbstractResource;
import org.jboss.gravia.resource.spi.AbstractWire;
import org.jboss.gravia.resource.spi.AbstractWiring;
import org.jboss.logging.Logger;

/**
 * An abstract resolver {@link Resolver}.
 *
 * @author thomas.diesler@jboss.com
 * @since 31-May-2010
 */
public class AbstractResolver implements Resolver {

    static final Logger LOGGER = Logger.getLogger(Resolver.class.getPackage().getName());
    
    private PreferencePolicy preferencePolicy;

    protected AbstractWire createWire(Requirement req, Capability cap) {
        return new DefaultWire(req, cap);
    }
    
    protected AbstractWiring createWiring(Resource resource, List<Wire> reqwires, List<Wire> provwires) {
        return new DefaultWiring(resource, reqwires, provwires);
    }
    
    protected PreferencePolicy createPreferencePolicy() {
        return new DefaultPreferencePolicy();
    }

    @Override
    public Map<Resource, List<Wire>> resolve(ResolveContext context) throws ResolutionException {
        return resolveInternal(context, false);
    }
    
    @Override
    public Map<Resource, List<Wire>> resolveAndApply(ResolveContext context) throws ResolutionException {
        return resolveInternal(context, true);
    }
    
    private Map<Resource, List<Wire>> resolveInternal(ResolveContext context, boolean apply) throws ResolutionException {
        
        LOGGER.debugf("Resolve: mandatory%s optional%s", context.getMandatoryResources(), context.getOptionalResources());
        
        // Get the combined set of resources in the context
        Set<Resource> combined = new HashSet<Resource>();
        combined.addAll(context.getMandatoryResources());
        combined.addAll(context.getOptionalResources());
        
        // Resolve combined resources
        ResolverState state = new ResolverState(context);
        for (Resource res : combined) {
            resolveResource(state, res);
        }

        // Log resolver result
        Map<Resource, List<Wire>> resourceWires = state.getResourceWires();
        if (LOGGER.isDebugEnabled()) {
            for (Entry<Resource, List<Wire>> entry : resourceWires.entrySet()) {
                LOGGER.debugf("Resolved: %s", entry.getKey());
                for (Wire wire : entry.getValue()) {
                    LOGGER.debugf("   %s", wire);
                }
            }
        }
        
        // Apply resolver results
        if (apply) {
            for (Entry<Resource, List<Wire>> entry : resourceWires.entrySet()) {
                AbstractResource requirer = (AbstractResource) entry.getKey();
                List<Wire> reqwires = entry.getValue();
                AbstractWiring reqwiring = (AbstractWiring) requirer.getWiring();
                if (reqwiring == null) {
                    reqwiring = createWiring(requirer, reqwires, null);
                    requirer.setWiring(reqwiring);
                } else {
                    for (Wire wire : reqwires) {
                        reqwiring.addRequiredWire(wire);
                    }
                }
                for (Wire wire : reqwires) {
                    AbstractResource provider = (AbstractResource) wire.getProvider();
                    AbstractWiring provwiring = (AbstractWiring) provider.getWiring();
                    if (provwiring == null) {
                        provwiring = createWiring(provider, null, null);
                        provider.setWiring(provwiring);
                    }
                    provwiring.addProvidedWire(wire);
                }
            }
        }
        
        return resourceWires;
    }

    private void resolveResource(ResolverState state, Resource res) throws ResolutionException {
        
        // We already have a result for the given resource
        if (state.hasWiring(res) || state.getResourceWires().get(res) != null)
            return;
        
        Map<Requirement, List<Capability>> candidates = new HashMap<Requirement, List<Capability>>();
        for (Requirement req : res.getRequirements(null)) {
            
            List<Capability> providers = state.getResolveContext().findProviders(req);
            getPreferencePolicyInternal().sort(providers);
            
            verifyTopLevelProviders(state, req, providers.iterator(), false);
            
            // Fail early if we don't have providers for non-optional requirements
            if (providers.isEmpty() && !(req.isOptional() || isOptionalResource(state, res))) {
                Set<Requirement> unresolved = Collections.singleton(req);
                throw new ResolutionException("Cannot find provider for: " + req, null, unresolved);
            }
            
            if (!providers.isEmpty()) {
                candidates.put(req, providers);
            }
        }
        
        // Reduce the candidates to a consistent set of wires
        List<Wire> wires = reduceCandidatesToResourceWires(state, res, candidates);
        if (wires != null) {
            state.getResourceWires().put(res, wires);
            for (Wire wire : wires) {
                Resource provider = wire.getProvider();
                resolveResource(state, provider);
            }
        }
    }


    private PreferencePolicy getPreferencePolicyInternal() {
        if (preferencePolicy == null) {
            preferencePolicy = createPreferencePolicy();
        }
        return preferencePolicy;
    }

    // Reduce the set of given capabilities by the ones that cannot transitively resolve in the same space
    private void verifyTopLevelProviders(ResolverState state, Requirement req, Iterator<Capability> itcap, boolean optional) throws ResolutionException {
        
        ResourceSpaces spaces = state.getResourceSpaces();
        while (itcap.hasNext()) {
            Capability cap = itcap.next();
            Resource capres = cap.getResource();
            ResourceSpace space = spaces.getResourceSpace(capres);
            if (space == null) {
                space = spaces.getDefaultSpace();
                spaces.addResource(space, capres);
            }
            if (!transitivelyVerifyResourceInSpace(state, space, capres)) {
                itcap.remove();
            }
        }
    }

    // True if the given resource transitively resloves in the given space
    private boolean transitivelyVerifyResourceInSpace(ResolverState state, ResourceSpace target, Resource res) {
        assert target != null : "Null target";
        assert res != null : "Null res";
        
        if (state.isWhitelisted(res, target))
            return true;
        
        if (state.isBlacklisted(res, target))
            return false;
        
        if (state.hasWiring(res)) {
            state.whitelist(res, target);
            return true;
        }
        
        ResourceSpaces spaces = state.getResourceSpaces();
        ResolveContext context = state.getResolveContext();
        for (Requirement req : res.getRequirements(null)) {
            List<Capability> providers = context.findProviders(req);
            for (Capability cap : providers) {
                Resource capres = cap.getResource();
                if (!transitivelyVerifyResourceInSpace(state, target, capres)) {
                    state.blacklist(res, target);
                    return false;
                }
                ResourceSpace space = spaces.getResourceSpace(capres);
                if (space == null) {
                    spaces.addResource(target, capres);
                }
            }
        }
        
        state.whitelist(res, target);
        return true;
    }

    private List<Wire> reduceCandidatesToResourceWires(ResolverState state, Resource res, Map<Requirement, List<Capability>> candidates) throws ResolutionException {
        
        // Check non-optional requirements on optional resources
        if (isOptionalResource(state, res)) {
            for (Requirement req : res.getRequirements(null)) {
                if (!req.isOptional() && candidates.get(req) == null)
                    return null;
            }
        }
        
        if (candidates.isEmpty())
            return Collections.emptyList();
        
        // Remove candidates that are not wired if we have a wired candidate
        for (Entry<Requirement, List<Capability>> entry : candidates.entrySet()) {
            List<Capability> caps = entry.getValue();
            if (caps.size() > 1 && state.hasWiring(caps.get(0).getResource())) {
                Iterator<Capability> itcaps = caps.iterator();
                while(itcaps.hasNext()) {
                    Resource capres = itcaps.next().getResource();
                    if (!state.hasWiring(capres)) {
                        itcaps.remove();
                    }
                }
            }
        }
        
        Map<ResourceSpace, Map<Requirement, Capability>> spacemap = new HashMap<ResourceSpace, Map<Requirement, Capability>>();
        ResourceSpaces spaces = state.getResourceSpaces();
        
        // Separate the wiring candidates into their respective spaces
        for (Entry<Requirement, List<Capability>> entry : candidates.entrySet()) {
            Requirement req = entry.getKey();
            for (Capability cap : entry.getValue()) {
                Resource capres = cap.getResource();
                ResourceSpace space = spaces.getResourceSpace(capres);
                assert space != null : "No resource space for: " + capres;
                
                Map<Requirement, Capability> map = spacemap.get(space);
                if (map == null) {
                    map = new HashMap<Requirement, Capability>();
                    spacemap.put(space, map);
                }
                Capability prefcap = map.get(req);
                Comparator<Capability> comp = getPreferencePolicyInternal().getComparator();
                if (prefcap == null || comp.compare(prefcap, cap) > 0) {
                    map.put(req, cap);
                }
            }
        }
        
        // Remove spaces that do not contain all requirements
        Set<Requirement> allreqs = candidates.keySet();
        Iterator<ResourceSpace> itspace = spacemap.keySet().iterator();
        while (itspace.hasNext()) {
            ResourceSpace space = itspace.next();
            Map<Requirement, Capability> map = spacemap.get(space);
            Set<Requirement> spacereqs = map.keySet();
            if (!spacereqs.equals(allreqs)) {
                itspace.remove();
            }
        }
        
        List<Wire> wires = new ArrayList<Wire>();
        Map<Requirement, Capability> mapping = selectSingleResourceSpace(allreqs, spacemap);
        for (Entry<Requirement, Capability> entry : mapping.entrySet()) {
            wires.add(createWire(entry.getKey(), entry.getValue()));
        }
        return wires;
    }

    private boolean isOptionalResource(ResolverState state, Resource res) {
        return state.getResolveContext().getOptionalResources().contains(res);
    }

    private Map<Requirement, Capability> selectSingleResourceSpace(Set<Requirement> allreqs, Map<ResourceSpace, Map<Requirement, Capability>> spacemap) throws ResolutionException {
        if (spacemap.isEmpty())
            throw new ResolutionException("Requirements map to candidates in disconnetced spaces", null, allreqs);
        
        if (spacemap.size() > 1)
            throw new ResolutionException("Requirements map to candidates in multiple spaces", null, allreqs);
        
        return spacemap.values().iterator().next();
    }

    private static class ResolverState {

        private final ResolveContext resolveContext;
        private final Map<Resource, Wiring> wirings;
        private final ResourceSpaces spaces = new ResourceSpaces();
        private final Map<Resource, Set<ResourceSpace>> blacklist = new HashMap<Resource, Set<ResourceSpace>>();
        private final Map<Resource, Set<ResourceSpace>> whitelist = new HashMap<Resource, Set<ResourceSpace>>();
        private final Map<Resource, List<Wire>> resourceWires = new HashMap<Resource, List<Wire>>();

        ResolverState(ResolveContext context) {
            this.resolveContext = context;
            this.wirings = context.getWirings();
            
            // Populate the spaces for already wired resources
            for (Entry<Resource, Wiring> entry : wirings.entrySet()) {
                Resource res = entry.getKey();
                Wiring wiring = entry.getValue();
                assignResourceToSpace(res, wiring, null);
            }
        }

        private void assignResourceToSpace(Resource res, Wiring wiring, ResourceSpace target) {
            if (spaces.getResourceSpace(res) == null) {
                if (target == null) {
                    target = spaces.addResourceSpace();
                }
                spaces.addResource(target, res);
                for (Wire wire : wiring.getRequiredResourceWires(null)) {
                    Resource provider = wire.getProvider();
                    assignResourceToSpace(provider, wiring, target);
                }
            }
        }

        void whitelist(Resource res, ResourceSpace space) {
            Set<ResourceSpace> spaces = whitelist.get(res);
            if (spaces == null) {
                spaces = new HashSet<ResourceSpace>();
                whitelist.put(res, spaces);
            }
            spaces.add(space);
        }

        void blacklist(Resource res, ResourceSpace space) {
            Set<ResourceSpace> spaces = blacklist.get(res);
            if (spaces == null) {
                spaces = new HashSet<ResourceSpace>();
                blacklist.put(res, spaces);
            }
            spaces.add(space);
        }
        
        boolean isBlacklisted(Resource res, ResourceSpace space) {
            Set<ResourceSpace> spaces = blacklist.get(res);
            return spaces != null && spaces.contains(space);
        }

        boolean isWhitelisted(Resource res, ResourceSpace space) {
            Set<ResourceSpace> spaces = whitelist.get(res);
            return spaces != null && spaces.contains(space);
        }

        ResourceSpaces getResourceSpaces() {
            return spaces;
        }

        ResolveContext getResolveContext() {
            return resolveContext;
        }
        
        boolean hasWiring(Resource res) {
            return wirings.get(res) != null;
        }
        
        Map<Resource, List<Wire>> getResourceWires() {
            return resourceWires;
        }
    }
    
    private static class ResourceSpace {
        private ResourceStore delegate;
        
        ResourceSpace(String spaceName) {
            delegate = new DefaultResourceStore(spaceName);
        }

        void addResource(Resource res) {
            delegate.addResource(res);
        }

        @Override
        public String toString() {
            return delegate.toString();
        }
    }
    
    private static class ResourceSpaces {

        private List<ResourceSpace> spaces = new ArrayList<ResourceSpace>();
        private Map<Resource, ResourceSpace> spacemap = new HashMap<Resource, ResourceSpace>();

        ResourceSpaces() {
            spaces.add(new ResourceSpace("#0"));
        }

        ResourceSpace getDefaultSpace() {
            return spaces.get(0);
        }
        
        ResourceSpace addResourceSpace() {
            ResourceSpace space = new ResourceSpace("#" + spaces.size());
            spaces.add(space);
            return space;
        }

        void addResource(ResourceSpace space, Resource res) {
            space.addResource(res);
            spacemap.put(res, space);
        }
        
        ResourceSpace getResourceSpace(Resource res) {
            return spacemap.get(res);
        }
    }
}

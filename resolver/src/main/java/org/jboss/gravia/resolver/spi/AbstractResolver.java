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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jboss.gravia.resolver.ResolutionException;
import org.jboss.gravia.resolver.ResolveContext;
import org.jboss.gravia.resolver.Resolver;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.DefaultResourceStore;
import org.jboss.gravia.resource.DefaultWire;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceStore;
import org.jboss.gravia.resource.Wire;
import org.jboss.gravia.resource.Wiring;
import org.jboss.logging.Logger;

/**
 * An abstract resolver {@link Resolver}.
 *
 * @author thomas.diesler@jboss.com
 * @since 31-May-2010
 */
public class AbstractResolver implements Resolver {

    static final Logger LOGGER = Logger.getLogger(Resolver.class.getPackage().getName());

    protected Wire createWire(Requirement req, Capability cap) {
        return new DefaultWire(req, cap);
    }
    
    @Override
    public Map<Resource, List<Wire>> resolve(ResolveContext context) throws ResolutionException {
        
        Map<Resource, List<Wire>> result = new HashMap<Resource, List<Wire>>();
        ResolverState state = new ResolverState(context);
        
        for (Resource res : context.getMandatoryResources()) {
            Map<Requirement, List<Capability>> candidates = new HashMap<Requirement, List<Capability>>();
            for (Requirement req : res.getRequirements(null)) {
                List<Capability> providers = context.findProviders(req);
                verifyTopLevelProviders(state, req, providers.iterator(), req.isOptional());
                if (providers.isEmpty() && !req.isOptional()) {
                    Set<Requirement> unresolved = Collections.singleton(req);
                    throw new ResolutionException("Cannot find provider for: " + req, null, unresolved);
                }
                candidates.put(req, providers);
            }
            List<Wire> wires = getResourceWires(state, candidates);
            result.put(res, wires);
        }
        return result;
    }

    // This reduces the set of given capabilities by the ones that cannot transitively resolve in the same space
    private void verifyTopLevelProviders(ResolverState state, Requirement req, Iterator<Capability> itcap, boolean optional) throws ResolutionException {
        while (itcap.hasNext()) {
            Capability cap = itcap.next();
            Resource capres = cap.getResource();
            ResourceSpace space = state.getResourceSpaces().getResourceSpace(capres);
            assert space != null : "No resource space for: " + capres;
            
            if (!transitivelyVerifyResourceInSpace(state, space, capres)) {
                itcap.remove();
            }
        }
    }

    // True if the given resource transitively resloves in the given space
    private boolean transitivelyVerifyResourceInSpace(ResolverState state, ResourceSpace space, Resource res) {
        
        if (state.isWhitelisted(res, space))
            return true;
        
        if (state.isBlacklisted(res, space))
            return false;
        
        if (state.getWirings().containsKey(res)) {
            state.whitelist(res, space);
            return true;
        }
        
        for (Requirement req : res.getRequirements(null)) {
            for (Capability cap : state.getResolveContext().findProviders(req)) {
                Resource capres = cap.getResource();
                if (!transitivelyVerifyResourceInSpace(state, space, capres)) {
                    state.blacklist(res, space);
                    return false;
                }
            }
        }
        
        state.whitelist(res, space);
        return true;
    }

    private List<Wire> getResourceWires(ResolverState state, Map<Requirement, List<Capability>> candidates) throws ResolutionException {
        
        if (candidates.isEmpty())
            return Collections.emptyList();
        
        // Separate the wiring candidates into their respective spaces
        Map<ResourceSpace, Map<Requirement, Capability>> spacemap = new HashMap<ResourceSpace, Map<Requirement, Capability>>();
        for (Entry<Requirement, List<Capability>> entry : candidates.entrySet()) {
            Requirement req = entry.getKey();
            for (Capability cap : entry.getValue()) {
                Resource capres = cap.getResource();
                ResourceSpace space = state.getResourceSpaces().getResourceSpace(capres);
                assert space != null : "No resource space for: " + capres;
                
                Map<Requirement, Capability> map = spacemap.get(space);
                if (map == null) {
                    map = new HashMap<Requirement, Capability>();
                    spacemap.put(space, map);
                }
                map.put(req, cap);
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
        Map<Requirement, Capability> mapping = selectSingleResourceSpace(spacemap);
        for (Entry<Requirement, Capability> entry : mapping.entrySet()) {
            wires.add(createWire(entry.getKey(), entry.getValue()));
        }
        return wires;
    }

    private Map<Requirement, Capability> selectSingleResourceSpace(Map<ResourceSpace, Map<Requirement, Capability>> spacemap) throws ResolutionException {
        if (spacemap.isEmpty())
            throw new ResolutionException("Requirements map to candidates in disconnetced spaces");
        
        if (spacemap.size() > 1)
            throw new ResolutionException("Requirements map to candidates in multiple spaces");
        
        return spacemap.values().iterator().next();
    }

    private static class ResolverState {

        private final ResolveContext resolveContext;
        private final Map<Resource, Wiring> wirings;
        private final ResourceSpaces spaces = new ResourceSpaces();
        private Map<Resource, Set<ResourceSpace>> blacklist = new HashMap<Resource, Set<ResourceSpace>>();
        private Map<Resource, Set<ResourceSpace>> whitelist = new HashMap<Resource, Set<ResourceSpace>>();

        ResolverState(ResolveContext context) {
            this.resolveContext = context;
            this.wirings = context.getWirings();
            
            // Populate the spaces for already wired resources
            for (Entry<Resource, Wiring> entry : wirings.entrySet()) {
                Resource res = entry.getKey();
                ResourceSpace space = spaces.getResourceSpace(res);
                if (space == null) {
                    space = spaces.addResourceSpace();
                    spaces.addResource(space, res);
                }
                Wiring wiring = entry.getValue();
                for (Wire wire : wiring.getRequiredResourceWires(null)) {
                    Resource provider = wire.getProvider();
                    spaces.addResource(space, provider);
                }
            }
            
            // Add an additional resource space for unwired resources
            ResourceSpace extraspace = spaces.addResourceSpace();
            for (Resource res : context.getMandatoryResources()) {
                spaces.addResource(extraspace, res);
            }
            for (Resource res : context.getOptionalResources()) {
                spaces.addResource(extraspace, res);
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
        
        Map<Resource, Wiring> getWirings() {
            return wirings;
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

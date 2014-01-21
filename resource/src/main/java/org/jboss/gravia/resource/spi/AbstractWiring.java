/*
 * #%L
 * Gravia :: Resource
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
package org.jboss.gravia.resource.spi;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.Wire;
import org.jboss.gravia.resource.Wiring;


/**
 * An abstract implementation of {@link Wiring}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AbstractWiring implements Wiring {

    private final Resource resource;
    private final List<Wire> required = new ArrayList<Wire>();
    private final Map<String, List<Wire>> provided = new HashMap<String, List<Wire>>();

    public AbstractWiring(Resource resource, List<Wire> reqwires, List<Wire> provwires) {
        if (resource == null)
            throw new IllegalArgumentException("Null resource");
        this.resource = resource;
        if (reqwires != null) {
            for (Wire wire : reqwires) {
                addRequiredWire(wire);
            }
        }
        if (provwires != null) {
            for (Wire wire : provwires) {
                addProvidedWire(wire);
            }
        }
    }

    @Override
    public Resource getResource() {
        return resource;
    }

    public void addRequiredWire(Wire wire) {
        required.add(wire);
    }

    public void addProvidedWire(Wire wire) {

        Capability cap = wire.getCapability();
        List<Wire> nswires = provided.get(cap.getNamespace());
        if (nswires == null) {
            nswires = new ArrayList<Wire>();
            provided.put(cap.getNamespace(), nswires);
        }

        // Ensures an implementation delivers a bundle wiring's provided wires in
        // the proper order. The ordering rules are as follows.
        //
        // (1) For a given name space, the list contains the wires in the order the
        // capabilities were specified in the manifests of the bundle revision and
        // the attached fragments of this bundle wiring.
        //
        // (2) There is no ordering defined between wires in different namespaces.
        //
        // (3) There is no ordering defined between multiple wires for the same
        // capability, but the wires must be contiguous, and the group must be
        // ordered as in (1).

        int index = 0;
        if (nswires.size() > 0) {
            int capindex = getCapabilityIndex(cap);
            for (Wire aux : nswires) {
                int auxindex = getCapabilityIndex(aux.getCapability());
                if (auxindex < capindex) {
                    index++;
                }
            }
        }
        nswires.add(index, wire);
    }

    private int getCapabilityIndex(Capability cap) {
        return getResource().getCapabilities(cap.getNamespace()).indexOf(cap);
    }

    @Override
    public List<Capability> getResourceCapabilities(String namespace) {
        List<Capability> result = new ArrayList<Capability>();
        for (Wire wire : getProvidedResourceWires(namespace)) {
            Capability req = wire.getCapability();
            result.add(req);
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public List<Requirement> getResourceRequirements(String namespace) {
        List<Requirement> result = new ArrayList<Requirement>();
        for (Wire wire : getRequiredResourceWires(namespace)) {
            Requirement req = wire.getRequirement();
            result.add(req);
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public List<Wire> getProvidedResourceWires(String namespace) {
        List<Wire> result = new ArrayList<Wire>();
        if (namespace != null) {
            List<Wire> nswires = provided.get(namespace);
            if (nswires != null) {
                result.addAll(nswires);
            }
        } else {
            for (List<Wire> wire : provided.values()) {
                result.addAll(wire);
            }
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public List<Wire> getRequiredResourceWires(String namespace) {
        List<Wire> result = new ArrayList<Wire>();
        for (Wire wire : required) {
            Requirement req = wire.getRequirement();
            if (namespace == null || namespace.equals(req.getNamespace())) {
                result.add(wire);
            }
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public String toString() {
        return "Wiring[" + resource + "]";
    }
}

/*
 * #%L
 * JBossOSGi Resolver API
 * %%
 * Copyright (C) 2010 - 2012 JBoss by Red Hat
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

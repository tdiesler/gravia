/*
 * #%L
 * Gravia Resource
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
package org.jboss.gravia.resource;

import java.util.List;

/**
 * A wiring for a resource. A wiring is associated with a resource and
 * represents the dependencies with other wirings.
 *
 * @author thomas.diesler@jboss.com
 * @since 18-Feb-2013
 */
public interface Wiring {

    /**
     * Returns the capabilities provided by this wiring.
     *
     * <p>
     * Only capabilities considered by the resolver are returned.
     *
     * <p>
     * A capability may not be required by any wiring and thus there may be no
     * {@link #getProvidedResourceWires(String) wires} for the capability.
     *
     *
     * @param namespace The namespace of the capabilities to return or
     *        {@code null} to return the capabilities from all namespaces.
     * @return A list containing a snapshot of the {@link Capability}s, or an
     *         empty list if this wiring provides no capabilities in the
     *         specified namespace.
     */
    List<Capability> getResourceCapabilities(String namespace);

    /**
     * Returns the requirements of this wiring.
     *
     * <p>
     * Only requirements considered by the resolver are returned. For example,
     * requirements with {@link Namespace#REQUIREMENT_EFFECTIVE_DIRECTIVE
     * effective} directive not equal to {@link Namespace#EFFECTIVE_RESOLVE
     * resolve} are not returned.
     *
     * <p>
     * A wiring for a non-fragment resource has a subset of the declared
     * requirements from the resource and all attached fragment resources. Not
     * all declared requirements may be present since some may be discarded. For
     * example, if a package is declared to be optionally imported and is not
     * actually imported, the requirement must be discarded.
     *
     * @param namespace The namespace of the requirements to return or
     *        {@code null} to return the requirements from all namespaces.
     * @return A list containing a snapshot of the {@link Requirement}s, or an
     *         empty list if this wiring uses no requirements in the specified
     *         namespace.
     */
    List<Requirement> getResourceRequirements(String namespace);

    /**
     * Returns the {@link Wire}s to the provided {@link Capability capabilities}
     * of this wiring.
     *
     * @param namespace The namespace of the capabilities for which to return
     *        wires or {@code null} to return the wires for the capabilities in
     *        all namespaces.
     * @return A list containing a snapshot of the {@link Wire}s for the
     *         {@link Capability capabilities} of this wiring, or an empty list
     *         if this wiring has no capabilities in the specified namespace.
     */
    List<Wire> getProvidedResourceWires(String namespace);

    /**
     * Returns the {@link Wire}s to the {@link Requirement requirements} in use
     * by this wiring.
     *
     * @param namespace The namespace of the requirements for which to return
     *        wires or {@code null} to return the wires for the requirements in
     *        all namespaces.
     * @return A list containing a snapshot of the {@link Wire}s for the
     *         {@link Requirement requirements} of this wiring, or an empty list
     *         if this wiring has no requirements in the specified namespace.
     */
    List<Wire> getRequiredResourceWires(String namespace);

    /**
     * Returns the resource associated with this wiring.
     * @return The resource associated with this wiring.
     */
    Resource getResource();
}

/*
 * #%L
 * Gravia Resolver
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
package org.jboss.gravia.resolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.jboss.gravia.resource.Requirement;

/**
 * Indicates failure to resolve a set of requirements.
 *
 * <p>
 * If a resolution failure is caused by a missing mandatory dependency a
 * resolver may include any requirements it has considered in the resolution
 * exception. Clients may access this set of dependencies via the
 * {@link #getUnresolvedRequirements()} method.
 *
 * <p>
 * Resolver implementations may extend this class to provide extra state
 * information about the reason for the resolution failure.
 *
 * @author thomas.diesler@jboss.com
 * @since 18-Jul-2013
 */
public class ResolutionException extends Exception {

    private static final long serialVersionUID = 1L;

    private transient final Collection<Requirement> unresolvedRequirements;

    /**
     * Create a {@code ResolutionException} with the specified message, cause
     * and unresolved requirements.
     *
     * @param message The message.
     * @param cause The cause of this exception.
     * @param unresolvedRequirements The unresolved mandatory requirements from
     *        mandatory resources or {@code null} if no unresolved requirements
     *        information is provided.
     */
    public ResolutionException(String message, Throwable cause, Collection<Requirement> unresolvedRequirements) {
        super(message, cause);
        if ((unresolvedRequirements == null) || unresolvedRequirements.isEmpty()) {
            this.unresolvedRequirements = Collections.emptyList();
        } else {
            this.unresolvedRequirements = Collections.unmodifiableCollection(new ArrayList<Requirement>(unresolvedRequirements));
        }
    }

    /**
     * Create a {@code ResolutionException} with the specified message.
     *
     * @param message The message.
     */
    public ResolutionException(String message) {
        super(message);
        unresolvedRequirements = Collections.emptyList();
    }

    /**
     * Create a {@code ResolutionException} with the specified cause.
     *
     * @param cause The cause of this exception.
     */
    public ResolutionException(Throwable cause) {
        super(cause);
        unresolvedRequirements = Collections.emptyList();
    }

    /**
     * Return the unresolved requirements, if any, for this exception.
     *
     * <p>
     * The unresolved requirements are provided for informational purposes and
     * the specific set of unresolved requirements that are provided after a
     * resolve failure is not defined.
     *
     * @return A collection of the unresolved requirements for this exception.
     *         The returned collection may be empty if no unresolved
     *         requirements information is provided.
     */
    public Collection<Requirement> getUnresolvedRequirements() {
        return unresolvedRequirements;
    }
}

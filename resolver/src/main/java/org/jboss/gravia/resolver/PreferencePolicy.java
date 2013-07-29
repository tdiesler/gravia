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

import java.util.Comparator;
import java.util.List;

import org.jboss.gravia.resource.Capability;


/**
 * A preference policy for providers
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public interface PreferencePolicy {

    /**
     * Sort the given providers according to their preference
     */
    void sort(List<Capability> providers);
    
    /**
     * Get the capability comparator
     */
    Comparator<Capability> getComparator();
}

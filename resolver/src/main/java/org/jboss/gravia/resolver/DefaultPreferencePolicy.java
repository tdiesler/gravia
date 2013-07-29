/*
 * #%L
 * JBossOSGi Resolver API
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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.IdentityNamespace;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.Version;
import org.jboss.gravia.resource.Wiring;
import org.jboss.gravia.resource.spi.AbstractCapability;

/**
 * The default {@link PreferencePolicy}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Apr-2012
 */
public class DefaultPreferencePolicy implements PreferencePolicy {

    private final Comparator<Capability> comparator;

    public DefaultPreferencePolicy(final Map<Resource, Wiring> wirings) {
        comparator = new Comparator<Capability>() {
            @Override
            public int compare(Capability cap1, Capability cap2) {

                // Prefer already wired
                if (wirings != null) {
                    Wiring w1 = wirings.get(cap1.getResource());
                    Wiring w2 = wirings.get(cap2.getResource());
                    if (w1 != null && w2 == null)
                        return -1;
                    if (w1 == null && w2 != null)
                        return +1;
                }

                // Prefer higher version
                Version v1 = AbstractCapability.getVersion(cap1, IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE);
                Version v2 = AbstractCapability.getVersion(cap2, IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE);
                return v2.compareTo(v1);
            }
        };    }

    @Override
    public void sort(List<Capability> providers) {
        Collections.sort(providers, comparator);
    }

    @Override
    public Comparator<Capability> getComparator() {
        return comparator;
    }
}

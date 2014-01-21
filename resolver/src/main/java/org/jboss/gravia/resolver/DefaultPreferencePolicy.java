/*
 * #%L
 * Gravia :: Resolver
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

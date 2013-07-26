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

package org.jboss.gravia.resource;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.gravia.resource.spi.AbstractCapability;
import org.jboss.gravia.resource.spi.AbstractRequirement;

/**
 * The default {@link MatchPolicy}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Apr-2012
 */
public class DefaultMatchPolicy implements MatchPolicy {
    
    @Override
    public boolean match(Capability cap, Requirement req) {
        Map<String, Object> reqatts = req.getAttributes();
        Map<String, Object> capatts = cap.getAttributes();

        // The requirement matches the capability if their namespaces match
        boolean matches = req.getNamespace().equals(cap.getNamespace());
        
        // Match the version range
        VersionRange range = matches ? AbstractRequirement.getVersionRange(req, IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE) : null;
        if (range != null) {
            Version version = AbstractCapability.getVersion(cap, IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE);
            matches = range.includes(version);
            reqatts = new HashMap<String, Object>(req.getAttributes());
            reqatts.remove(IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE);
            capatts = new HashMap<String, Object>(cap.getAttributes());
            capatts.remove(IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE);
        }
        
        // Match the remaining attributes
        if (matches) {
            for (Entry<String, Object> entry : reqatts.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (!value.equals(capatts.get(key))) {
                    matches = false;
                    break;
                }
            }
        }
        
        return matches;
    }
}

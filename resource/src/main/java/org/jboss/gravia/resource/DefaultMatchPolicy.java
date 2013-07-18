/*
 * #%L
 * JBossOSGi Resolver API
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
    
    private static MatchPolicy DEFAULT_POLICY = new DefaultMatchPolicy();
    
    public static MatchPolicy getDefault() {
        return DEFAULT_POLICY;
    }

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

/*
 * #%L
 * JBossOSGi Resolver API
 * %%
 * Copyright (C) 2010 - 2012 JBoss by Red Hat
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file ecept in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either epress or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.jboss.gravia.resource.spi;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.IdentityNamespace;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.Version;
import org.jboss.gravia.resource.VersionRange;

/**
 * The abstract implementation of a {@link Requirement}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AbstractRequirement implements Requirement {

    private final AbstractResource resource;
    private final String namespace;
    private Map<String, Object> attributes;
    private Map<String, String> directives;
    private boolean optional;

    public AbstractRequirement(AbstractResource resource, String namespace, Map<String, Object> atts, Map<String, String> dirs) {
        if (resource == null)
            throw new IllegalArgumentException("Null resource");
        if (namespace == null)
            throw new IllegalArgumentException("Null namespace");

        this.resource = resource;
        this.namespace = namespace;
        if (atts != null) {
            getAttributes().putAll(atts);
        }
        if (dirs != null) {
            getDirectives().putAll(dirs);
        }
    }

    @Override
    public Resource getResource() {
        return resource;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public boolean isOptional() {
        return optional;
    }

    @Override
    public Map<String, String> getDirectives() {
        if (directives == null) {
            directives = new LinkedHashMap<String, String>();
        }
        return resource.isMutable() ? directives : Collections.unmodifiableMap(directives);
    }

    @Override
    public String getDirective(String key) {
        return directives != null ? directives.get(key) : null;
    }

    @Override
    public Map<String, Object> getAttributes() {
        if (attributes == null) {
            attributes = new LinkedHashMap<String, Object>();
        }
        return resource.isMutable() ? attributes : Collections.unmodifiableMap(attributes);
    }

    @Override
    public Object getAttribute(String key) {
        return attributes != null ? attributes.get(key) : null;
    }


    @Override
    public <T> T adapt(Class<T> clazz) {
        T result = null;
        return result;
    }

    public boolean matches(Capability cap) {
        resource.assertImmutable();
        Map<String, Object> reqatts = getAttributes();
        Map<String, Object> capatts = cap.getAttributes();

        // The requirement matches the capability if their namespaces match
        boolean matches = namespace.equals(cap.getNamespace());
        
        // Match the version range
        VersionRange range = matches ? getVersionRange(this, IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE) : null;
        if (range != null) {
            Version version = AbstractCapability.getVersion(cap, IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE);
            matches = range.includes(version);
            reqatts = new HashMap<String, Object>(getAttributes());
            reqatts.remove(IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE);
            capatts = new HashMap<String, Object>(cap.getAttributes());
            capatts.remove(IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE);
        }
        
        // Match the remaining attributes
        matches &= reqatts.equals(capatts);
        
        return matches;
    }

    protected void validate() {
    }
    
    static VersionRange getVersionRange(Requirement req, String attr) {
        Object value = req.getAttribute(attr);
        return (value instanceof String) ? new VersionRange((String) value) : (VersionRange) value;
    }
}

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

import static org.jboss.gravia.resource.ContentNamespace.CAPABILITY_MAVEN_IDENTITY_ATTRIBUTE;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.IdentityNamespace;
import org.jboss.gravia.resource.MavenCoordinates;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.resource.Version;
import org.jboss.gravia.utils.IllegalStateAssertion;

/**
 * The abstract implementation of a {@link Capability}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AbstractCapability implements Capability {

    private final AbstractResource resource;
    private final String namespace;
    private Map<String, Object> attributes;
    private Map<String, String> directives;
    private String canonicalName;

    public AbstractCapability(AbstractResource resource, String namespace, Map<String, Object> atts, Map<String, String> dirs) {
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
    @SuppressWarnings("unchecked")
    public <T> T adapt(Class<T> type) {
        T result = null;
        if (type.isAssignableFrom(getClass())) {
            result = (T) this;
        }
        return result;
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

    public static Version getVersion(Capability cap, String attname) {
        Object attval = cap.getAttributes().get(attname);
        if (attval != null && !(attval instanceof Version)) {
            attval = new Version(attval.toString());
            cap.getAttributes().put(attname, attval);
        }
        return attval != null ? (Version) attval : Version.emptyVersion;
    }

    protected void validate() {
        for (Entry<String, Object> entry : attributes.entrySet()) {
            String key = entry.getKey();
            Object val = entry.getValue();
            if (CAPABILITY_MAVEN_IDENTITY_ATTRIBUTE.equals(key)) {
                IllegalStateAssertion.assertTrue(val instanceof MavenCoordinates, "Illegal attribute type: " + val);
            }
        }
        canonicalName = toString();
    }

    @Override
    public String toString() {
        String result = canonicalName;
        if (result == null) {
            String type;
            String nsval = null;
            if (IdentityNamespace.IDENTITY_NAMESPACE.equals(getNamespace())) {
                type = "IdentityCapability";
            } else {
                type = getClass().getSimpleName();
                nsval = namespace;
            }
            StringBuffer buffer = new StringBuffer(type + "[");
            boolean addcomma = false;
            if (nsval != null) {
                buffer.append(nsval);
                addcomma = true;
            }
            if (!getAttributes().isEmpty()) {
                buffer.append(addcomma ? "," : "");
                buffer.append("atts=" + attributes);
                addcomma = true;
            }
            if (!getDirectives().isEmpty()) {
                buffer.append(addcomma ? "," : "");
                buffer.append("dirs=" + directives);
                addcomma = true;
            }
            ResourceIdentity icap = resource.getIdentity();
            if (icap != null) {
                buffer.append(addcomma ? "," : "");
                buffer.append("[" + icap.getSymbolicName() + ":" + icap.getVersion() + "]");
                addcomma = true;
            } else {
                buffer.append(addcomma ? "," : "");
                buffer.append("[anonymous]");
                addcomma = true;
            }
            buffer.append("]");
            result = buffer.toString();
        }
        return result;
    }
}

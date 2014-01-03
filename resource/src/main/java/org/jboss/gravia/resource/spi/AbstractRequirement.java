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
package org.jboss.gravia.resource.spi;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jboss.gravia.resource.IdentityNamespace;
import org.jboss.gravia.resource.Namespace;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.resource.VersionRange;

/**
 * The abstract implementation of a {@link Requirement}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AbstractRequirement implements Requirement, Serializable {

    private static final long serialVersionUID = 2075218264839332932L;

    private final AbstractResource resource;
    private final String namespace;
    private Map<String, Object> attributes;
    private Map<String, String> directives;
    private String canonicalName;
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

    public static VersionRange getVersionRange(Requirement req, String attr) {
        Object value = req.getAttribute(attr);
        return (value instanceof String) ? new VersionRange((String) value) : (VersionRange) value;
    }

    protected void validate() {
        Object attval = getAttribute(IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE);
        if (attval != null && !(attval instanceof VersionRange)) {
            getAttributes().put(IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE, new VersionRange(attval.toString()));
        }
        String resdir = getDirective(Namespace.REQUIREMENT_RESOLUTION_DIRECTIVE);
        optional = Namespace.RESOLUTION_OPTIONAL.equals(resdir);
        canonicalName = toString();
    }

    @Override
    public String toString() {
        String result = canonicalName;
        if (result == null) {
            String type;
            String nsval = null;
            if (IdentityNamespace.IDENTITY_NAMESPACE.equals(getNamespace())) {
                type = "IdentityRequirement";
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

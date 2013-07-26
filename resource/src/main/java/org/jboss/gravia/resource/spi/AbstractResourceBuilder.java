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
package org.jboss.gravia.resource.spi;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.IdentityNamespace;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceBuilder;
import org.jboss.gravia.resource.Version;
import org.jboss.gravia.resource.VersionRange;

/**
 * An abstract {@link Resource} builder.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public abstract class AbstractResourceBuilder implements ResourceBuilder {

    private AbstractResource resource;

    protected abstract AbstractResource createResource();

    protected abstract AbstractCapability createCapability(AbstractResource resource, String namespace, Map<String, Object> attributes, Map<String, String> directives);

    protected abstract AbstractRequirement createRequirement(AbstractResource resource, String namespace, Map<String, Object> attributes, Map<String, String> directives);

    @Override
    public Capability addIdentityCapability(String symbolicName, String version) {
        return addIdentityCapability(symbolicName, Version.parseVersion(version), null, null);
    }

    @Override
    public Capability addIdentityCapability(String symbolicName, Version version) {
        return addIdentityCapability(symbolicName, version, null, null);
    }

    @Override
    public Capability addIdentityCapability(String symbolicName, Version version, Map<String, Object> atts, Map<String, String> dirs) {
        Capability icap = addCapability(IdentityNamespace.IDENTITY_NAMESPACE, symbolicName);
        if (version != null) {
            icap.getAttributes().put(IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE, version);
        }
        if (atts != null) {
            icap.getAttributes().putAll(atts);
        }
        if (dirs != null) {
            icap.getDirectives().putAll(dirs);
        }
        return icap;
    }

    @Override
    public Capability addCapability(String namespace, String nsvalue) {
        return addCapability(namespace, Collections.singletonMap(namespace, (Object)nsvalue), null);
    }

    @Override
    public Capability addCapability(String namespace, Map<String, Object> atts, Map<String, String> dirs) {
        AbstractResource resource = getResourceInternal();
        AbstractCapability cap = createCapability(resource, namespace, mutableAttributes(atts), mutableDirectives(dirs));
        resource.addCapability(cap);
        return cap;
    }

    @Override
    public Requirement addIdentityRequirement(String symbolicName, String version) {
        return addIdentityRequirement(symbolicName, new VersionRange(version), null,  null);
    }

    @Override
    public Requirement addIdentityRequirement(String symbolicName, VersionRange version) {
        return addIdentityRequirement(symbolicName, version, null,  null);
    }

    @Override
    public Requirement addIdentityRequirement(String symbolicName, VersionRange version, Map<String, Object> atts, Map<String, String> dirs) {
        Requirement ireq = addRequirement(IdentityNamespace.IDENTITY_NAMESPACE, symbolicName);
        if (version != null) {
            ireq.getAttributes().put(IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE, version);
        }
        if (atts != null) {
            ireq.getAttributes().putAll(atts);
        }
        if (dirs != null) {
            ireq.getDirectives().putAll(dirs);
        }
        return ireq;
    }

    @Override
    public Requirement addRequirement(String namespace, String nsvalue) {
        return addRequirement(namespace, Collections.singletonMap(namespace, (Object)nsvalue), null);
    }

    @Override
    public Requirement addRequirement(String namespace, Map<String, Object> atts, Map<String, String> dirs) {
        AbstractResource resource = getResourceInternal();
        AbstractRequirement req = createRequirement(resource, namespace, mutableAttributes(atts), mutableDirectives(dirs));
        resource.addRequirement(req);
        return req;
    }

    @Override
    public Resource getResource() {
        AbstractResource resource = getResourceInternal();
        resource.validate();
        resource.setMutable(false);
        return resource;
    }

    private Map<String, Object> mutableAttributes(Map<String, Object> atts) {
        return new LinkedHashMap<String, Object>(atts != null ? atts : new LinkedHashMap<String, Object>());
    }

    private Map<String, String> mutableDirectives(Map<String, String> dirs) {
        return new LinkedHashMap<String, String>(dirs != null ? dirs : new LinkedHashMap<String, String>());
    }

    private AbstractResource getResourceInternal() {
        if (resource == null) {
            resource = createResource();
        }
        return resource;
    }
}

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
import java.util.LinkedHashMap;
import java.util.Map;

import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.IdentityNamespace;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceBuilder;
import org.jboss.gravia.resource.Version;

/**
 * A builder for resources
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AbstractResourceBuilder implements ResourceBuilder {

    private AbstractResource resource;

    protected AbstractResource createResource() {
        return new AbstractResource();
    }
    
    protected AbstractCapability createCapability(AbstractResource resource, String namespace, Map<String, Object> attributes, Map<String, String> directives) {
        return new AbstractCapability(resource, namespace, attributes, directives);
    }

    protected AbstractRequirement createRequirement(AbstractResource resource, String namespace, Map<String, Object> attributes, Map<String, String> directives) {
        return new AbstractRequirement(resource, namespace, attributes, directives);
    }

    @Override
    public Capability addIdentityCapability(String symbolicName, Version version) {
        Capability icap = addCapability(IdentityNamespace.IDENTITY_NAMESPACE, symbolicName);
        icap.getAttributes().put(IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE, version);
        return icap;
    }

    @Override
    public Capability addCapability(String namespace, String nsvalue) {
        return addCapability(namespace, Collections.singletonMap(namespace, (Object)nsvalue), null);
    }

    @Override
    public Capability addCapability(String namespace, Map<String, Object> atts, Map<String, String> dirs) {
        AbstractCapability cap = createCapability(getResourceInternal(), namespace, mutableAttributes(atts), mutableDirectives(dirs));
        getResourceInternal().addCapability(cap);
        return cap;
    }

    @Override
    public Requirement addRequirement(String namespace, String nsvalue) {
        return addRequirement(namespace, Collections.singletonMap(namespace, (Object)nsvalue), null);
    }

    @Override
    public Requirement addRequirement(String namespace, Map<String, Object> atts, Map<String, String> dirs) {
        AbstractRequirement req = createRequirement(getResourceInternal(), namespace, mutableAttributes(atts), mutableDirectives(dirs));
        getResourceInternal().addRequirement(req);
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

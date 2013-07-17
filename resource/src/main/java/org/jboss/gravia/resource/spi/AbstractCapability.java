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
package org.jboss.gravia.resource.spi;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.Version;

/**
 * The abstract implementation of a {@link XCapability}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AbstractCapability implements Capability {

    private final AbstractResource resource;
    private final String namespace;
    private Map<String, Object> attributes;
    private Map<String, String> directives;

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

    @Override
    public <T> T adapt(Class<T> clazz) {
        T result = null;
        return result;
    }

    static Version getVersion(Capability cap, String attname) {
        Object attval = cap.getAttributes().get(attname);
        if (attval != null && !(attval instanceof Version)) {
            attval = new Version(attval.toString());
            cap.getAttributes().put(attname, attval);
        }
        return attval != null ? (Version)attval : Version.emptyVersion;
    }
    
    protected void validate() {
    }
}

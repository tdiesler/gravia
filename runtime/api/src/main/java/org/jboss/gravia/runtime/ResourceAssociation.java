/*
 * #%L
 * Gravia :: Runtime :: API
 * %%
 * Copyright (C) 2013 - 2014 JBoss by Red Hat
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
package org.jboss.gravia.runtime;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceIdentity;

/**
 * A global {@link Resource} association.
 *
 * Use this when you need to transport a resource across an API
 * that does not support object association.
 * For example Tomcat/Wildfly installers.
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2014
 */
public final class ResourceAssociation {

    private static Map<ResourceIdentity, Resource> resourceAssociation = new ConcurrentHashMap<ResourceIdentity, Resource>();

    // Hide ctor
    private ResourceAssociation() {
    }

    public static Resource getResource(ResourceIdentity identity) {
        return resourceAssociation.get(identity);
    }

    public static Resource putResource(Resource resource) {
        return resourceAssociation.put(resource.getIdentity(), resource);
    }

    public static Resource removeResource(ResourceIdentity identity) {
        return resourceAssociation.remove(identity);
    }
}

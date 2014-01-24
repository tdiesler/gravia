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
package org.jboss.gravia.runtime.spi;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.gravia.resource.Resource;

/**
 * A {@link Resource} association.
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2014
 */
public final class NamedResourceAssociation {

    private static Map<String, Resource> resourceAssociation = new ConcurrentHashMap<String, Resource>();

    // Hide ctor
    private NamedResourceAssociation() {
    }

    public static Resource getResource(String resourceKey) {
        return resourceAssociation.get(resourceKey);
    }

    public static void putResource(String resourceKey, Resource resource) {
        resourceAssociation.put(resourceKey, resource);
    }

    public static void removeResource(String resourceKey) {
        resourceAssociation.remove(resourceKey);
    }
}

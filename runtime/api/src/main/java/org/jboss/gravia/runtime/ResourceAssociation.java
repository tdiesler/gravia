/*
 * #%L
 * Gravia :: Integration Tests :: Common
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

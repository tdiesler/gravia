/*
 * #%L
 * Gravia Repository
 * %%
 * Copyright (C) 2012 - 2013 JBoss by Red Hat
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
package org.jboss.gravia.repository;

import java.io.IOException;
import java.util.Map;

import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.utils.ObjectNameFactory;

/**
 * A {@link Repository} MBean.
 *
 * @author thomas.diesler@jboss.com
 * @since 23-Dec-2013
 */
public interface RepositoryMBean {

    ObjectName OBJECT_NAME = ObjectNameFactory.create("org.jboss.gravia:type=Repository");

    /**
     * Get the name for this repository
     */
    String getName();

    /**
     * Find the capabilities that match the specified requirement.
     *
     * @return A collection of matching capabilities for the specified requirements.
     *         If there are no matching capabilities an empty collection is returned.
     *         The returned collection is the property of the caller and can be modified by the caller.
     */
    TabularData findProviders(String namespace, String nsvalue, Map<String, Object> attributes, Map<String, String> directives);

    /**
     * Add a {@link Resource} to the {@link Repository}
     */
    CompositeData addResource(CompositeData resData) throws IOException;

    /**
     * Remove a {@link Resource} from the {@link Repository}
     */
    CompositeData removeResource(String identity);

    /**
     * Get a a {@link Resource} by {@link ResourceIdentity}
     */
    CompositeData getResource(String identity);
}

/*
 * #%L
 * Gravia :: Repository
 * %%
 * Copyright (C) 2012 - 2014 JBoss by Red Hat
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

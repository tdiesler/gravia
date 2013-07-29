
package org.jboss.gravia.repository;
/*
 * #%L
 * JBossOSGi Repository
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

import java.util.Map;

import org.jboss.gravia.repository.spi.AbstractRepositoryResource;
import org.jboss.gravia.resource.DefaultCapability;
import org.jboss.gravia.resource.DefaultRequirement;
import org.jboss.gravia.resource.spi.AbstractCapability;
import org.jboss.gravia.resource.spi.AbstractRequirement;
import org.jboss.gravia.resource.spi.AbstractResource;
import org.jboss.gravia.resource.spi.AbstractResourceBuilder;

/**
 * Create an URL based resource
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public class DefaultRepositoryResourceBuilder extends AbstractResourceBuilder {

    @Override
    protected AbstractRepositoryResource createResource() {
        return new DefaultRepositoryResource();
    }

    @Override
    protected AbstractCapability createCapability(AbstractResource resource, String namespace, Map<String, Object> atts, Map<String, String> dirs) {
        if (ContentNamespace.CONTENT_NAMESPACE.equals(namespace))
            return new DefaultContentCapability(resource, namespace, atts, dirs);
        else
            return new DefaultCapability(resource, namespace, atts, dirs);
    }

    @Override
    protected AbstractRequirement createRequirement(AbstractResource resource, String namespace, Map<String, Object> attributes, Map<String, String> directives) {
        return new DefaultRequirement(resource, namespace, attributes, directives);
    }
}

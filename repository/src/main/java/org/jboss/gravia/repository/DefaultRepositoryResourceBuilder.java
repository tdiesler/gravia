
package org.jboss.gravia.repository;
/*
 * #%L
 * JBossOSGi Repository
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

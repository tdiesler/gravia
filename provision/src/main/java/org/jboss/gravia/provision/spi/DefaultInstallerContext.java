/*
 * #%L
 * Gravia :: Provision
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

package org.jboss.gravia.provision.spi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.gravia.provision.ResourceInstaller.Context;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.utils.NotNullException;

/**
 * An abstract installer {@link Context}.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 13-Jan-2014
 */
public class DefaultInstallerContext implements Context {

    private final List<Resource> resources;
    private final Map<Requirement, Resource> resourceMapping;
    private final Map<String, Object> properties = new HashMap<String, Object>();

    public DefaultInstallerContext(Resource resource) {
        this(Collections.singletonList(resource), Collections.<Requirement, Resource>emptyMap());
    }

    public DefaultInstallerContext(List<Resource> resources, Map<Requirement, Resource> mapping) {
        NotNullException.assertValue(resources, "resources");
        NotNullException.assertValue(mapping, "mapping");
        this.resources = new ArrayList<Resource>(resources);
        this.resourceMapping = new HashMap<Requirement, Resource>(mapping);
    }

    @Override
    public List<Resource> getResources() {
        return Collections.unmodifiableList(resources);
    }

    @Override
    public Map<Requirement, Resource> getResourceMapping() {
        return Collections.unmodifiableMap(resourceMapping);
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

}

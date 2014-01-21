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
package org.jboss.gravia.provision;

import org.jboss.gravia.provision.ResourceHandle;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.utils.NotNullException;

/**
 * An default {@link ResourceHandle}
 *
 * @author thomas.diesler@jboss.com
 * @since 06-May-2013
 */
public class DefaultResourceHandle implements ResourceHandle {

    private final Resource resource;
    private final Module module;

    public DefaultResourceHandle(Resource resource, Module module) {
        NotNullException.assertValue(resource, "resource");
        this.resource = resource;
        this.module = module;
    }

    @Override
    public Module getModule() {
        return module;
    }

    @Override
    public Resource getResource() {
        return resource;
    }

    @Override
    public void uninstall() {
        // do nothing
    }
}

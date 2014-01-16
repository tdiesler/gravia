/*
 * #%L
 * JBossOSGi Provision: Core
 * %%
 * Copyright (C) 2013 JBoss by Red Hat
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

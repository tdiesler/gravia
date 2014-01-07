/*
 * #%L
 * JBossOSGi Framework
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
package org.jboss.gravia.provision.spi;

import org.jboss.gravia.provision.Environment;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.ModuleEvent;
import org.jboss.gravia.runtime.ModuleListener;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.SynchronousModuleListener;

/**
 * An {@link Environment} that delegates to the {@link Runtime}.
 *
 * @author thomas.diesler@jboss.com
 * @since 07-Jan-2014
 */
public class RuntimeEnvironment extends AbstractEnvironment {

    public RuntimeEnvironment(Runtime runtime) {
        super(RuntimeEnvironment.class.getSimpleName());

        // Add the initial set of modules
        for (Module module : runtime.getModules()) {
            addResourceInternal(module.adapt(Resource.class));
        }

        // Track installed/uninstalled modules
        ModuleListener listener = new SynchronousModuleListener() {
            @Override
            public void moduleChanged(ModuleEvent event) {
                Module module = event.getModule();
                if (event.getType() == ModuleEvent.INSTALLED) {
                    addResourceInternal(module.adapt(Resource.class));
                } else if (event.getType() == ModuleEvent.UNINSTALLED) {
                    removeResourceInternal(module.getIdentity());
                }
            }
        };
        ModuleContext syscontext = runtime.getModuleContext();
        syscontext.removeModuleListener(listener);
    }

    @Override
    public Resource addResource(Resource resource) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Resource removeResource(ResourceIdentity identity) {
        throw new UnsupportedOperationException();
    }

    private Resource addResourceInternal(Resource resource) {
        return super.addResource(resource);
    }

    private Resource removeResourceInternal(ResourceIdentity identity) {
        return super.removeResource(identity);
    }

}

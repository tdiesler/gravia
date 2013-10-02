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
package org.jboss.gravia.runtime.embedded;

import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.PropertiesProvider;
import org.jboss.gravia.runtime.spi.AbstractModule;
import org.jboss.gravia.runtime.spi.AbstractRuntime;
import org.jboss.gravia.runtime.spi.RuntimeEventsHandler;

/**
 * [TODO]
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
public final class EmbeddedRuntime extends AbstractRuntime {

    private final RuntimeServicesHandler serviceManager;
    private final RuntimeStorageHandler storageHandler;

    public EmbeddedRuntime(PropertiesProvider propertiesProvider) {
        super(propertiesProvider);
        serviceManager = new RuntimeServicesHandler(adapt(RuntimeEventsHandler.class));
        storageHandler = new RuntimeStorageHandler(propertiesProvider, true);
    }

    @Override
    protected AbstractModule createModule(ClassLoader classLoader, Resource resource) {
        return new EmbeddedModule(this, classLoader, resource);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A> A adapt(Class<A> type) {
        A result = super.adapt(type);
        if (result == null) {
            if (type.isAssignableFrom(RuntimeServicesHandler.class)) {
                result = (A) serviceManager;
            } else if (type.isAssignableFrom(RuntimeStorageHandler.class)) {
                result = (A) storageHandler;
            }
        }
        return result;
    }

    @Override
    protected void uninstallModule(Module module) {
        super.uninstallModule(module);
    }
}

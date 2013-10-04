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
package org.jboss.gravia.runtime.osgi.internal;

import java.util.Dictionary;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleException;
import org.jboss.gravia.runtime.PropertiesProvider;
import org.jboss.gravia.runtime.osgi.OSGiRuntime;
import org.jboss.gravia.runtime.spi.AbstractModule;
import org.jboss.gravia.runtime.spi.AbstractRuntime;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;

/**
 * [TODO]
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
public final class OSGiRuntimeImpl extends AbstractRuntime implements OSGiRuntime {

    public OSGiRuntimeImpl(PropertiesProvider propertiesProvider) {
        super(propertiesProvider);
    }

    @Override
    public void init() {
        // do nothing
    }

    @Override
    protected AbstractModule createModule(ClassLoader classLoader, Resource resource, Dictionary<String, String> headers) {
        return new ModuleAdaptor(this, classLoader, resource, headers);
    }

    @Override
    protected void uninstallModule(Module module) {
        super.uninstallModule(module);
    }

    @Override
    public Module installModule(Bundle bundle) throws ModuleException {
        Module module = getModule(bundle.getBundleId());
        if (module == null) {
            BundleWiring wiring = bundle.adapt(BundleWiring.class);
            ClassLoader classLoader = wiring != null ? wiring.getClassLoader() : null;
            if (classLoader == null)
                throw new ModuleException("Bundle has no class loader: " + bundle);
            if (bundle.getBundleId() == 0)
                throw new ModuleException("Cannot install system bundle: " + bundle);

            module = installModule(classLoader, bundle.getHeaders());
        }
        return module;
    }
}

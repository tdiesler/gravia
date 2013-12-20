/*
 * #%L
 * Gravia :: Runtime :: OSGi
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
package org.jboss.gravia.runtime;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * A Service Activator
 *
 * @author thomas.diesler@jboss.com
 * @since 24-Apr-2009
 */
public class DefaultBundleActivator implements BundleActivator, ModuleActivator {

    @Override
    public void start(BundleContext context) throws Exception {
        Module module = getModule(context);
        start(module.getModuleContext());
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        Module module = getModule(context);
        stop(module.getModuleContext());
    }

    @Override
    public void start(ModuleContext context) throws Exception {
    }

    @Override
    public void stop(ModuleContext context) throws Exception {
    }

    private Module getModule(BundleContext context) {
        Bundle bundle = context.getBundle();
        Runtime runtime = RuntimeLocator.getRequiredRuntime();
        Module module = runtime.getModule(bundle.getBundleId());
        if (module == null) {
            throw new IllegalStateException("Cannot obtain associated module for: " + bundle);
        }
        return module;
    }

}

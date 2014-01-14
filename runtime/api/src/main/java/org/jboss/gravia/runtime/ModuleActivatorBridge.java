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

import java.util.Dictionary;

import org.jboss.gravia.Constants;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * A simple {@link ModuleActivator} bridge
 *
 * @author thomas.diesler@jboss.com
 * @since 14-Jan-2014
 */
public class ModuleActivatorBridge implements BundleActivator {

    private ModuleActivator moduleActivator;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        ModuleContext moduleContext = getModuleContext(bundleContext);
        if (moduleContext != null) {
            Bundle bundle = bundleContext.getBundle();
            Dictionary<String, String> headers = bundle.getHeaders();
            String className = headers.get(Constants.MODULE_ACTIVATOR);
            if (className != null) {
                Object result = bundle.loadClass(className).newInstance();
                moduleActivator = (ModuleActivator) result;
                moduleActivator.start(moduleContext);
            }
        }
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        ModuleContext moduleContext = getModuleContext(bundleContext);
        if (moduleActivator != null && moduleContext != null) {
            moduleActivator.stop(moduleContext);
        }
    }

    private ModuleContext getModuleContext(BundleContext bundleContext) {
        Bundle bundle = bundleContext.getBundle();
        Runtime runtime = RuntimeLocator.getRequiredRuntime();
        Module module = runtime.getModule(bundle.getBundleId());
        return module != null ? module.getModuleContext() : null;
    }
}
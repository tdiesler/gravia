/*
 * #%L
 * Gravia :: Runtime :: API
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

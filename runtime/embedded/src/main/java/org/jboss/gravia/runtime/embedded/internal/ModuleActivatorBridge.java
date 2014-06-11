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
package org.jboss.gravia.runtime.embedded.internal;

import org.jboss.gravia.runtime.ModuleActivator;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.embedded.spi.BundleContextAdaptor;
import org.osgi.framework.BundleActivator;

/**
 * A simple {@link ModuleActivator} bridge
 *
 * @author thomas.diesler@jboss.com
 * @since 14-Jan-2014
 */
final class ModuleActivatorBridge implements ModuleActivator {

    private final BundleActivator bundleActivator;

    public ModuleActivatorBridge(BundleActivator activator) {
        this.bundleActivator = activator;
    }

    @Override
    public void start(ModuleContext context) throws Exception {
        bundleActivator.start(new BundleContextAdaptor(context));
    }

    @Override
    public void stop(ModuleContext context) throws Exception {
        if (bundleActivator != null) {
            bundleActivator.stop(new BundleContextAdaptor(context));
        }
    }
}

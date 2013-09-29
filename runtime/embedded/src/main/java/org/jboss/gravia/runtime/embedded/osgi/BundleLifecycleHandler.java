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
package org.jboss.gravia.runtime.embedded.osgi;

import org.jboss.gravia.resource.Attachable;
import org.jboss.gravia.resource.AttachmentKey;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.osgi.framework.BundleActivator;

/**
 * A handler for internal Bundle lifecycle.
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
public final class BundleLifecycleHandler {

    private static final String INTERNAL_BUNDLE_ACTIVATOR = "Internal-ModuleActivator";

    private static AttachmentKey<BundleActivator> BUNDLE_ACTIVATOR_KEY = AttachmentKey.create(BundleActivator.class);

    public static boolean isInternalBundle(Module module) {
        String className = (String) module.getProperties().get(INTERNAL_BUNDLE_ACTIVATOR);
        return className != null;
    }

    public static void start(Module module) throws Exception {
        String className = (String) module.getProperties().get(INTERNAL_BUNDLE_ACTIVATOR);
        if (className != null) {
            BundleActivator bundleActivator;
            synchronized (BUNDLE_ACTIVATOR_KEY) {
                Attachable attachable = (Attachable) module;
                bundleActivator = attachable.getAttachment(BUNDLE_ACTIVATOR_KEY);
                if (bundleActivator == null) {
                    Object result = module.loadClass(className).newInstance();
                    bundleActivator = (BundleActivator) result;
                    attachable.putAttachment(BUNDLE_ACTIVATOR_KEY, bundleActivator);
                }
            }
            if (bundleActivator != null) {
                ModuleContext context = module.getModuleContext();
                bundleActivator.start(new BundleContextAdaptor(context));
            }
        }
    }

    public static void stop(Module module) throws Exception {
        Attachable attachable = (Attachable) module;
        BundleActivator bundleActivator = attachable.getAttachment(BUNDLE_ACTIVATOR_KEY);
        ModuleContext context = module.getModuleContext();
        bundleActivator.stop(new BundleContextAdaptor(context));
    }
}

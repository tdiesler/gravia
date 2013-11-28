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
package org.jboss.gravia.runtime.embedded.spi;

import java.net.URL;
import java.util.Dictionary;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleActivator;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.ModuleException;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.spi.RuntimePlugin;
import org.jboss.gravia.runtime.util.ManifestHeadersProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.Constants;

/**
 * An abstract {@link RuntimePlugin}
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
public abstract class AbstractRuntimePlugin implements RuntimePlugin, ModuleActivator {

    private BundleActivator delegate;

    public abstract String getBundleActivator();

    @Override
    public Module installPluginModule(Runtime runtime, ClassLoader classLoader) throws ModuleException {
        String resourceName = getBundleActivator().replace('.', '/') + ".class";
        URL resurl = classLoader.getResource(resourceName);
        if (resurl == null)
            throw new ModuleException("Cannot load BundleActivator resource: " + resourceName);

        String urlpath = resurl.toExternalForm();
        urlpath = urlpath.substring(0, urlpath.indexOf(resourceName));
        urlpath = urlpath + JarFile.MANIFEST_NAME;

        Manifest manifest;
        try {
            manifest = new Manifest(new URL(urlpath).openStream());
        } catch (Exception ex) {
            throw new ModuleException("Cannot load plugin manifest: " + urlpath, ex);
        }
        Dictionary<String, String> headers = new ManifestHeadersProvider(manifest).getHeaders();
        String symbolicName = headers.get(Constants.BUNDLE_SYMBOLICNAME);
        String version = headers.get(Constants.BUNDLE_VERSION);
        headers.put(org.jboss.gravia.resource.Constants.GRAVIA_IDENTITY_CAPABILITY, symbolicName + ";version=" + version);
        headers.put(org.jboss.gravia.resource.Constants.MODULE_ACTIVATOR, getClass().getName());
        return runtime.installModule(classLoader, headers);
    }

    @Override
    public void start(ModuleContext context) throws Exception {
        delegate = (BundleActivator) context.getModule().loadClass(getBundleActivator()).newInstance();
        delegate.start(new BundleContextAdaptor(context));
    }

    @Override
    public void stop(ModuleContext context) throws Exception {
        if (delegate != null) {
            delegate.stop(new BundleContextAdaptor(context));
        }
    }
}

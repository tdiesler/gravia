/*
 * #%L
 * Gravia :: Runtime :: Embedded
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
import org.jboss.gravia.runtime.spi.ManifestHeadersProvider;
import org.jboss.gravia.runtime.spi.RuntimePlugin;
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
            throw new ModuleException("Cannot load BundleActivator resource '" + resourceName + "' from: " + classLoader);

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
        headers.put(org.jboss.gravia.Constants.GRAVIA_IDENTITY_CAPABILITY, symbolicName + ";version=" + version);
        headers.put(org.jboss.gravia.Constants.MODULE_ACTIVATOR, getClass().getName());
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

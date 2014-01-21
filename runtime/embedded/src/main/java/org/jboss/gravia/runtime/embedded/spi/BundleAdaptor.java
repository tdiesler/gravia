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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.spi.ModuleEntriesProvider;
import org.jboss.gravia.utils.CaseInsensitiveDictionary;
import org.jboss.gravia.utils.NotNullException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;

/**
 * Bundle implementation that delegates all functionality to
 * the underlying Module.
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
public final class BundleAdaptor implements Bundle {

    private final Module module;

    public BundleAdaptor(Module module) {
        NotNullException.assertValue(module, "module");
        this.module = module;
    }

    @Override
    public long getBundleId() {
        return module.getModuleId();
    }


    @Override
    public String getSymbolicName() {
        return module.getIdentity().getSymbolicName();
    }

    @Override
    public Version getVersion() {
        String version = module.getIdentity().getVersion().toString();
        return  Version.parseVersion(version);
    }

    @Override
    public String getLocation() {
        return module.getIdentity().toString();
    }

    @Override
    public int getState() {
        switch (module.getState()) {
            case INSTALLED:
                return Bundle.INSTALLED;
            case RESOLVED:
                return Bundle.RESOLVED;
            case STARTING:
                return Bundle.STARTING;
            case ACTIVE:
                return Bundle.ACTIVE;
            case STOPPING:
                return Bundle.STOPPING;
            case UNINSTALLED:
                return Bundle.UNINSTALLED;
        }
        return Bundle.UNINSTALLED;
    }

    @Override
    public BundleContext getBundleContext() {
        ModuleContext context = module.getModuleContext();
        return context != null ? new BundleContextAdaptor(context) : null;
    }

    @Override
    public <A> A adapt(Class<A> type) {
        return module.adapt(type);
    }

    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        return module.loadClass(className);
    }

    @Override
    public URL getResource(String name) {
        ClassLoader classLoader = module.adapt(ClassLoader.class);
        return classLoader.getResource(name);
    }

    @Override
    public Dictionary<String, String> getHeaders() {
        return getHeaders(null);
    }

    @Override
    public Dictionary<String, String> getHeaders(String locale) {

        // Get the raw (unlocalized) manifest headers
        Dictionary<String, String> rawHeaders = module.getHeaders();

        // If the specified locale is the empty string, this method will return the
        // raw (unlocalized) manifest headers including any leading "%"
        if ("".equals(locale))
            return rawHeaders;

        Dictionary<String, String> result = new Hashtable<String, String>();
        Enumeration<String> e = rawHeaders.keys();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            String value = rawHeaders.get(key);
            if (value.startsWith("%"))
                value = value.substring(1);

            result.put(key, value);
        }

        return new CaseInsensitiveDictionary<String>(result);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        ClassLoader classLoader = module.adapt(ClassLoader.class);
        return classLoader.getResources(name);
    }

    @Override
    public URL getEntry(String path) {
        ModuleEntriesProvider entriesProvider = module.adapt(ModuleEntriesProvider.class);
        return entriesProvider != null ? entriesProvider.getEntry(path) : null;
    }

    @Override
    public Enumeration<String> getEntryPaths(String path) {
        ModuleEntriesProvider entriesProvider = module.adapt(ModuleEntriesProvider.class);
        Enumeration<String> result = null;
        if (entriesProvider != null) {
            List<String> paths = entriesProvider.getEntryPaths(path);
            if (paths.size() > 0) {
                Vector<String> vector = new Vector<String>(paths);
                result = vector.elements();
            }
        }
        return result;
    }

    @Override
    public Enumeration<URL> findEntries(String path, String filePattern, boolean recurse) {
        ModuleEntriesProvider entriesProvider = module.adapt(ModuleEntriesProvider.class);
        Enumeration<URL> result = null;
        if (entriesProvider != null) {
            List<URL> paths = entriesProvider.findEntries(path, filePattern, recurse);
            if (paths.size() > 0) {
                Vector<URL> vector = new Vector<URL>(paths);
                result = vector.elements();
            }
        }
        return result;
    }

    @Override
    public int compareTo(Bundle bundle) {
        throw new UnsupportedOperationException("Bundle.compareTo(Bundle)");
    }

    @Override
    public void start(int options) throws BundleException {
        throw new UnsupportedOperationException("Bundle.start(int)");
    }

    @Override
    public void start() throws BundleException {
        throw new UnsupportedOperationException("Bundle.start()");
    }

    @Override
    public void stop(int options) throws BundleException {
        throw new UnsupportedOperationException("Bundle.stop(int)");
    }

    @Override
    public void stop() throws BundleException {
        throw new UnsupportedOperationException("Bundle.stop()");
    }

    @Override
    public void update(InputStream input) throws BundleException {
        throw new UnsupportedOperationException("Bundle.update(InputStream)");
    }

    @Override
    public void update() throws BundleException {
        throw new UnsupportedOperationException("Bundle.update()");
    }

    @Override
    public void uninstall() throws BundleException {
        throw new UnsupportedOperationException("Bundle.uninstall()");
    }

    @Override
    public ServiceReference<?>[] getRegisteredServices() {
        throw new UnsupportedOperationException("Bundle.getRegisteredServices()");
    }

    @Override
    public ServiceReference<?>[] getServicesInUse() {
        throw new UnsupportedOperationException("Bundle.getServicesInUse()");
    }

    @Override
    public long getLastModified() {
        throw new UnsupportedOperationException("Bundle.getLastModified()");
    }

    @Override
    public Map<X509Certificate, List<X509Certificate>> getSignerCertificates(int signersType) {
        throw new UnsupportedOperationException("Bundle.getSignerCertificates(int)");
    }

    @Override
    public File getDataFile(String filename) {
        return module.getDataFile(filename);
    }

    @Override
    public boolean hasPermission(Object permission) {
        throw new UnsupportedOperationException("Bundle.hasPermission(Object)");
    }

    @Override
    public String toString() {
        return "Bundle[" + module.getIdentity() + "]";
    }
}

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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.jar.Manifest;

import org.jboss.gravia.resource.Attachable;
import org.jboss.gravia.resource.AttachmentKey;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.osgi.metadata.CaseInsensitiveDictionary;
import org.jboss.osgi.metadata.OSGiMetaData;
import org.jboss.osgi.metadata.OSGiMetaDataBuilder;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
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

    static AttachmentKey<OSGiMetaData> OSGI_METADATA_KEY = AttachmentKey.create(OSGiMetaData.class);
    static AttachmentKey<BundleActivator> BUNDLE_ACTIVATOR_KEY = AttachmentKey.create(BundleActivator.class);

    private final Module module;

    public BundleAdaptor(Module module) {
        this.module = module;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A> A adapt(Class<A> type) {
        A result = null;
        if (type.isAssignableFrom(Module.class)) {
            result = (A) module;
        }
        return result;
    }

    // Bundle API

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
    public String getSymbolicName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Version getVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        return module.loadClass(className);
    }

    @Override
    public int compareTo(Bundle bundle) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void start(int options) throws BundleException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void start() throws BundleException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void stop(int options) throws BundleException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void stop() throws BundleException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void update(InputStream input) throws BundleException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void update() throws BundleException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void uninstall() throws BundleException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Dictionary<String, String> getHeaders() {
        // If the specified locale is null then the locale returned
        // by java.util.Locale.getDefault is used.
        return getHeaders(null);
    }

    @Override
    public long getBundleId() {
        return module.getModuleId();
    }

    @Override
    public String getLocation() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServiceReference<?>[] getRegisteredServices() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServiceReference<?>[] getServicesInUse() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasPermission(Object permission) {
        throw new UnsupportedOperationException();
    }

    @Override
    public URL getResource(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Dictionary<String, String> getHeaders(String locale) {

        // Get the raw (unlocalized) manifest headers
        Dictionary<String, String> rawHeaders = getOSGiMetaData().getHeaders();

        // If the specified locale is the empty string, this method will return the
        // raw (unlocalized) manifest headers including any leading "%"
        if ("".equals(locale))
            return rawHeaders;

        // If the specified locale is null then the locale
        // returned by java.util.Locale.getDefault is used
        if (locale == null)
            locale = Locale.getDefault().toString();

        // Get the localization base name
        String baseName = rawHeaders.get(Constants.BUNDLE_LOCALIZATION);
        if (baseName == null)
            baseName = Constants.BUNDLE_LOCALIZATION_DEFAULT_BASENAME;

        // Get the resource bundle URL for the given base and locale
        URL entryURL = null; //getLocalizationEntry(baseName, locale);

        // If the specified locale entry could not be found fall back to the default locale entry
        if (entryURL == null) {
            // String defaultLocale = Locale.getDefault().toString();
            // entryURL = getLocalizationEntry(baseName, defaultLocale);
        }

        // Read the resource bundle
        ResourceBundle resBundle = null;
        /*
        if (entryURL != null) {
            try {
                resBundle = new PropertyResourceBundle(entryURL.openStream());
            } catch (IOException ex) {
                throw new IllegalStateException("Cannot read resource bundle: " + entryURL, ex);
            }
        }
         */

        Dictionary<String, String> locHeaders = new Hashtable<String, String>();
        Enumeration<String> e = rawHeaders.keys();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            String value = rawHeaders.get(key);
            if (value.startsWith("%"))
                value = value.substring(1);

            if (resBundle != null) {
                try {
                    value = resBundle.getString(value);
                } catch (MissingResourceException ex) {
                    // ignore
                }
            }

            locHeaders.put(key, value);
        }

        return new CaseInsensitiveDictionary(locHeaders);
    }

    private OSGiMetaData getOSGiMetaData() {
        Attachable attachable = (Attachable) module;
        OSGiMetaData metadata  = attachable.getAttachment(OSGI_METADATA_KEY);
        if (metadata == null) {
            Manifest manifest = (Manifest) module.getProperty(org.jboss.gravia.runtime.Constants.MODULE_MANIFEST);
            if (manifest != null) {
                metadata = OSGiMetaDataBuilder.load(manifest);
            } else {
                String bsname = getSymbolicName();
                Version version = getVersion();
                OSGiMetaDataBuilder builder = OSGiMetaDataBuilder.createBuilder(bsname, version);
                metadata = builder.getOSGiMetaData();
            }
            attachable.putAttachment(OSGI_METADATA_KEY, metadata);
        }
        return metadata;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Enumeration<String> getEntryPaths(String path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public URL getEntry(String path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getLastModified() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Enumeration<URL> findEntries(String path, String filePattern, boolean recurse) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<X509Certificate, List<X509Certificate>> getSignerCertificates(int signersType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public File getDataFile(String filename) {
        throw new UnsupportedOperationException();
    }

}

/*
 * #%L
 * Fabric8 :: Testsuite :: Smoke :: Embedded
 * %%
 * Copyright (C) 2014 Red Hat
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
package org.jboss.gravia.arquillian.container.embedded;

import static org.jboss.gravia.runtime.spi.EnvPropertiesProvider.DEFAULT_ENV_PREFIX;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.jboss.gravia.resource.Attachable;
import org.jboss.gravia.resource.DefaultResourceBuilder;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleException;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.embedded.internal.EmbeddedRuntime;
import org.jboss.gravia.runtime.spi.ClassLoaderEntriesProvider;
import org.jboss.gravia.runtime.spi.DefaultPropertiesProvider;
import org.jboss.gravia.runtime.spi.ManifestHeadersProvider;
import org.jboss.gravia.runtime.spi.ModuleEntriesProvider;
import org.jboss.gravia.runtime.spi.PropertiesProvider;
import org.jboss.gravia.runtime.spi.RuntimeFactory;
import org.jboss.gravia.runtime.spi.URLStreamHandlerFactoryProxy;
import org.jboss.gravia.runtime.spi.URLStreamHandlerTracker;

/**
 * Utility for embedded runtime tests
 *
 * @author thomas.diesler@jboss.com
 * @since 18-Oct-2013
 */
public class EmbeddedUtils {

    // Hide ctor
    private EmbeddedUtils() {
    }

    public static Runtime getEmbeddedRuntime() {
        Runtime runtime;
        synchronized (RuntimeLocator.class) {
            runtime = RuntimeLocator.getRuntime();
            if (runtime == null) {
                RuntimeFactory factory = new RuntimeFactory() {
                    @Override
                    public Runtime createRuntime(PropertiesProvider propertiesProvider) {
                        return new EmbeddedRuntime(propertiesProvider, null) {

                            @Override
                            public void init() {
                                URLStreamHandlerTracker tracker = new URLStreamHandlerTracker(getModuleContext());
                                URLStreamHandlerFactoryProxy.setDelegate(tracker);
                                URLStreamHandlerFactoryProxy.register();
                                super.init();
                            }

                            @Override
                            protected ModuleEntriesProvider getDefaultEntriesProvider(Module module, Attachable context) {
                                return new ClassLoaderEntriesProvider(module);
                            }
                        };
                    }
                };

                runtime = RuntimeLocator.createRuntime(factory, new DefaultPropertiesProvider(new HashMap<String, Object>(), true, DEFAULT_ENV_PREFIX));
                runtime.init();
            }
        }
        return runtime;
    }

    public static Module installAndStartModule(ClassLoader classLoader, File location) throws ModuleException, IOException {
        return installAndStartModule(classLoader, location.toURI().toURL());
    }

    public static Module installAndStartModule(ClassLoader classLoader, URL location) throws ModuleException, IOException {
        JarInputStream input = new JarInputStream(location.openStream());
        try {
            Manifest manifest = input.getManifest();
            Dictionary<String, String> headers = new ManifestHeadersProvider(manifest).getHeaders();
            return installAndStartModule(classLoader, null, headers);
        } finally {
            input.close();
        }
    }

    public static Module installAndStartModule(ClassLoader classLoader, String symbolicName, String version) throws ModuleException {
        ResourceIdentity.create(symbolicName, version);
        Resource resource = new DefaultResourceBuilder().addIdentityCapability(symbolicName, version).getResource();
        return installAndStartModule(classLoader, resource);
    }

    public static Module installAndStartModule(ClassLoader classLoader, ResourceIdentity identity) throws ModuleException {
        Resource resource = new DefaultResourceBuilder().addIdentityCapability(identity).getResource();
        return installAndStartModule(classLoader, resource);
    }

    public static Module installAndStartModule(ClassLoader classLoader, Resource resource) throws ModuleException {
        return installAndStartModule(classLoader, resource, null);
    }

    public static Module installAndStartModule(ClassLoader classLoader, Resource resource, Dictionary<String, String> headers) throws ModuleException {
        Module module = getEmbeddedRuntime().installModule(classLoader, resource, headers);
        module.start();
        return module;
    }
}

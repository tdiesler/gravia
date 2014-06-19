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
import static org.jboss.gravia.runtime.spi.RuntimeLogger.LOGGER;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.rmi.registry.LocateRegistry;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.jboss.gravia.provision.ResourceHandle;
import org.jboss.gravia.provision.ResourceInstaller;
import org.jboss.gravia.provision.spi.AbstractResourceInstaller;
import org.jboss.gravia.provision.spi.RuntimeEnvironment;
import org.jboss.gravia.repository.DefaultRepositoryXMLReader;
import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.repository.RepositoryReader;
import org.jboss.gravia.resource.Attachable;
import org.jboss.gravia.resource.DefaultResourceBuilder;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceContent;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.ModuleException;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.ServiceLocator;
import org.jboss.gravia.runtime.embedded.internal.EmbeddedRuntime;
import org.jboss.gravia.runtime.spi.ClassLoaderEntriesProvider;
import org.jboss.gravia.runtime.spi.DefaultPropertiesProvider;
import org.jboss.gravia.runtime.spi.ManifestHeadersProvider;
import org.jboss.gravia.runtime.spi.ModuleEntriesProvider;
import org.jboss.gravia.runtime.spi.PropertiesProvider;
import org.jboss.gravia.runtime.spi.RuntimeFactory;
import org.jboss.gravia.runtime.spi.URLStreamHandlerFactoryProxy;
import org.jboss.gravia.runtime.spi.URLStreamHandlerTracker;
import org.jboss.gravia.utils.IOUtils;
import org.jboss.gravia.utils.IllegalArgumentAssertion;
import org.jboss.gravia.utils.IllegalStateAssertion;
import org.jboss.gravia.utils.ManifestUtils;

/**
 * Utility for embedded runtime tests
 *
 * @author thomas.diesler@jboss.com
 * @since 18-Oct-2013
 */
public class EmbeddedTestSupport {

    public Runtime getEmbeddedRuntime() {
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

                // Register the {@link RuntimeEnvironment} and {@link ResourceInstaller}
                ModuleContext syscontext = runtime.getModuleContext();
                RuntimeEnvironment environment = new RuntimeEnvironment(runtime);
                EmbeddedResourceInstaller resourceInstaller = new EmbeddedResourceInstaller(environment);
                syscontext.registerService(RuntimeEnvironment.class, environment, null);
                syscontext.registerService(ResourceInstaller.class, resourceInstaller, null);

                // Create the JMXConnectorServer
                try {
                    JMXConnectorServer connectorServer = createJMXConnectorServer();
                    connectorServer.start();
                    String jmxServerUrl = connectorServer.getAddress().toString();
                    LOGGER.info("JMX server URL: " + jmxServerUrl);
                } catch (IOException ex) {
                    throw new IllegalStateException(ex);
                }

                // Add initial runtime resources
                String resname = "environment.xml";
                URL resurl = EmbeddedTestSupport.class.getClassLoader().getResource(resname);
                if (resurl != null) {
                    InputStream input = null;
                    try {
                        input = resurl.openStream();
                        environment.initDefaultContent(input);
                    } catch (IOException ex) {
                        throw new IllegalStateException(ex);
                    } finally {
                        IOUtils.safeClose(input);
                    }
                }
            }
        }
        return runtime;
    }

    public JMXConnectorServer createJMXConnectorServer() throws IOException {
        ServerSocket server = new ServerSocket(0);
        int port = server.getLocalPort();
        server.close();
        LocateRegistry.createRegistry(port);
        JMXServiceURL serviceURL = new JMXServiceURL("service:jmx:rmi://localhost:" + port + "/jndi/rmi://localhost:" + port + "/jmxrmi");
        MBeanServer mbeanServer = ServiceLocator.getRequiredService(MBeanServer.class);
        return JMXConnectorServerFactory.newJMXConnectorServer(serviceURL, null, mbeanServer);
    }

    public Module installAndStartModule(ClassLoader classLoader, File location) throws ModuleException, IOException {
        return installAndStartModule(classLoader, location.toURI().toURL());
    }

    public Module installAndStartModule(ClassLoader classLoader, URL location) throws ModuleException, IOException {
        JarInputStream input = new JarInputStream(location.openStream());
        try {
            Manifest manifest = input.getManifest();
            Dictionary<String, String> headers = new ManifestHeadersProvider(manifest).getHeaders();
            return installAndStartModule(classLoader, null, headers);
        } finally {
            input.close();
        }
    }

    public Module installAndStartModule(ClassLoader classLoader, String symbolicName, String version) throws ModuleException {
        ResourceIdentity.create(symbolicName, version);
        Resource resource = new DefaultResourceBuilder().addIdentityCapability(symbolicName, version).getResource();
        return installAndStartModule(classLoader, resource);
    }

    public Module installAndStartModule(ClassLoader classLoader, ResourceIdentity identity) throws ModuleException {
        Resource resource = new DefaultResourceBuilder().addIdentityCapability(identity).getResource();
        return installAndStartModule(classLoader, resource);
    }

    public Module installAndStartModule(ClassLoader classLoader, Resource resource) throws ModuleException {
        return installAndStartModule(classLoader, resource, null);
    }

    public Module installAndStartModule(ClassLoader classLoader, Resource resource, Dictionary<String, String> headers) throws ModuleException {
        Module module = getEmbeddedRuntime().installModule(classLoader, resource, headers);
        module.start();
        return module;
    }

    public Set<ResourceIdentity> addRepositoryContent(URL resurl) throws IOException {
        IllegalArgumentAssertion.assertNotNull(resurl, "resurl");
        Repository repository = ServiceLocator.getRequiredService(Repository.class);

        Set<ResourceIdentity> result = new HashSet<>();
        InputStream input = resurl.openStream();
        try {
            RepositoryReader reader = new DefaultRepositoryXMLReader(input);
            Resource auxres = reader.nextResource();
            while (auxres != null) {
                ResourceIdentity identity = auxres.getIdentity();
                if (repository.getResource(identity) == null) {
                    repository.addResource(auxres);
                    result.add(identity);
                }
                auxres = reader.nextResource();
            }
        } finally {
            IOUtils.safeClose(input);
        }
        return Collections.unmodifiableSet(result);
    }

    public void removeRepositoryContent(Set<ResourceIdentity> identities) throws IOException {
        if (identities != null) {
            Repository repository = ServiceLocator.getRequiredService(Repository.class);
            for (ResourceIdentity resid : identities) {
                repository.removeResource(resid);
            }
        }
    }

    public void deleteDirectory(String directory) throws IOException {
        deleteDirectory(Paths.get(directory));
    }

    public void deleteDirectory(Path directory) throws IOException {
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    final class EmbeddedResourceInstaller extends AbstractResourceInstaller {

        private final RuntimeEnvironment environment;

        EmbeddedResourceInstaller(RuntimeEnvironment environment) {
            this.environment = environment;
        }

        @Override
        public RuntimeEnvironment getEnvironment() {
            return environment;
        }

        @Override
        public ResourceHandle installResourceProtected(Context context, final Resource resource) throws Exception {

            ResourceContent content = getFirstRelevantResourceContent(resource);
            Manifest manifest = ManifestUtils.getManifest(content.getContent());
            IllegalStateAssertion.assertNotNull(manifest, "Resource has no manifest: " + resource);
            Dictionary<String, String> headers = ManifestUtils.getManifestHeaders(manifest);

            // Install the module
            Runtime runtime = environment.getRuntime();
            ClassLoader classLoader = EmbeddedRuntime.class.getClassLoader();
            final Module module = runtime.installModule(classLoader, resource, headers);

            // Autostart the module
            module.start();

            return new ResourceHandle() {

                @Override
                public Resource getResource() {
                    return resource;
                }

                @Override
                public Module getModule() {
                    return module;
                }

                @Override
                public void uninstall() {
                    module.uninstall();
                }
            };
        }
    }
}

/*
 * #%L
 * Gravia :: Container :: WildFly :: Extension
 * %%
 * Copyright (C) 2010 - 2014 JBoss by Red Hat
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

package org.wildfly.extension.gravia.service;

import static org.wildfly.extension.gravia.GraviaExtensionLogger.LOGGER;

import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.jboss.gravia.resource.Attachable;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.spi.AbstractModule;
import org.jboss.gravia.runtime.spi.ClassLoaderEntriesProvider;
import org.jboss.gravia.runtime.spi.ManifestHeadersProvider;
import org.jboss.gravia.runtime.spi.ModuleEntriesProvider;
import org.jboss.gravia.utils.IllegalStateAssertion;
import org.jboss.modules.ModuleClassLoader;
import org.jboss.msc.service.AbstractService;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.wildfly.extension.gravia.GraviaConstants;

/**
 * Service responsible for creating and managing the life-cycle of the gravia subsystem.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 19-Apr-2013
 */
public class GraviaBootstrapService extends AbstractService<Void> {

    private final InjectedValue<Runtime> injectedRuntime = new InjectedValue<Runtime>();

    private Module module;

    public ServiceController<Void> install(ServiceTarget serviceTarget) {
        ServiceBuilder<Void> builder = serviceTarget.addService(GraviaConstants.GRAVIA_SUBSYSTEM_SERVICE_NAME, this);
        builder.addDependency(GraviaConstants.RUNTIME_SERVICE_NAME, Runtime.class, injectedRuntime);
        return builder.install();
    }

    @Override
    public void start(final StartContext startContext) throws StartException {
        LOGGER.info("Activating Gravia Subsystem");
        Runtime runtime = injectedRuntime.getValue();
        installExtensionModule(startContext, runtime);
    }

    @Override
    public void stop(StopContext context) {
        uninstallExtensionModule();
    }

    /**
     * Install and start the gravia extension as a {@link Module}
     */
    public void installExtensionModule(StartContext startContext, Runtime runtime) throws StartException {
        ModuleClassLoader classLoader = (ModuleClassLoader) getClass().getClassLoader();
        try {
            URL extensionURL = null;
            Enumeration<URL> resources = classLoader.getResources(JarFile.MANIFEST_NAME);
            while (resources.hasMoreElements()) {
                URL nextURL = resources.nextElement();
                if (nextURL.getPath().contains("gravia-container-wildfly-extension")) {
                    extensionURL = nextURL;
                    break;
                }
            }
            IllegalStateAssertion.assertNotNull(extensionURL, "Manifest for extension module not found");
            Manifest manifest = new Manifest(extensionURL.openStream());
            Dictionary<String, String> headers = new ManifestHeadersProvider(manifest).getHeaders();
            module = runtime.installModule(classLoader, headers);

            // Attach the {@link ModuleEntriesProvider} so
            ModuleEntriesProvider entriesProvider = new ClassLoaderEntriesProvider(module);
            Attachable attachable = AbstractModule.assertAbstractModule(module);
            attachable.putAttachment(AbstractModule.MODULE_ENTRIES_PROVIDER_KEY, entriesProvider);

            // Start the module
            module.start();

        } catch (RuntimeException rte) {
            throw rte;
        } catch (Exception ex) {
            throw new StartException(ex);
        }
    }

    /**
     * Uninstall the gravia extension module
     */
    public void uninstallExtensionModule() {
        if (module != null) {
            module.uninstall();
        }
    }
}

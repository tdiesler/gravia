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
package org.jboss.gravia.runtime.embedded.internal;

import static org.jboss.gravia.runtime.spi.RuntimeLogger.LOGGER;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import org.jboss.gravia.resource.Attachable;
import org.jboss.gravia.resource.DefaultResourceBuilder;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.ModuleException;
import org.jboss.gravia.runtime.ServiceRegistration;
import org.jboss.gravia.runtime.spi.AbstractModule;
import org.jboss.gravia.runtime.spi.AbstractRuntime;
import org.jboss.gravia.runtime.spi.ClassLoaderEntriesProvider;
import org.jboss.gravia.runtime.spi.ModuleEntriesProvider;
import org.jboss.gravia.runtime.spi.PropertiesProvider;
import org.jboss.gravia.runtime.spi.RuntimeEventsManager;
import org.jboss.gravia.runtime.spi.RuntimePlugin;
import org.osgi.service.log.LogService;

/**
 * The embedded runtome implemenation
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
public class EmbeddedRuntime extends AbstractRuntime {

    private final RuntimeServicesManager serviceManager;
    private final RuntimeStorageHandler storageHandler;
    private final List<ServiceRegistration<?>> systemServices = new ArrayList<ServiceRegistration<?>>();

    public EmbeddedRuntime(PropertiesProvider propertiesProvider, Attachable context) {
        super(propertiesProvider);
        serviceManager = new RuntimeServicesManager(adapt(RuntimeEventsManager.class));
        storageHandler = new RuntimeStorageHandler(propertiesProvider, true);

        // Install system module
        Resource resource = new DefaultResourceBuilder().addIdentityCapability(getSystemIdentity()).getResource();
        try {
            installModule(EmbeddedRuntime.class.getClassLoader(), resource, null, context);
        } catch (ModuleException ex) {
            throw new IllegalStateException("Cannot install system module", ex);
        }
    }

    @Override
    public void init() {
        assertNoShutdown();

        // Register the LogService
        ModuleContext syscontext = adapt(ModuleContext.class);
        systemServices.add(syscontext.registerService(LogService.class.getName(), new EmbeddedLogServiceFactory(), null));

        // Register the MBeanServer service
        MBeanServer mbeanServer = findOrCreateMBeanServer();
        systemServices.add(syscontext.registerService(MBeanServer.class, mbeanServer, null));

        // Install the plugin modules
        List<Module> pluginModules = new ArrayList<Module>();
        ClassLoader classLoader = getClass().getClassLoader();
        ServiceLoader<RuntimePlugin> services = ServiceLoader.load(RuntimePlugin.class, EmbeddedRuntime.class.getClassLoader());
        Iterator<RuntimePlugin> iterator = services.iterator();
        while (iterator.hasNext()) {
            RuntimePlugin plugin = iterator.next();
            try {
                Module module = plugin.installPluginModule(this, classLoader);
                if (module != null) {
                    pluginModules.add(module);
                }
            } catch (ModuleException ex) {
                LOGGER.error("Cannot load plugin: " + plugin.getClass().getName(), ex);
            }
        }

        // Start the plugin modules
        for (Module module : pluginModules) {
            try {
                module.start();
            } catch (ModuleException ex) {
                LOGGER.error("Cannot start plugin: " + module, ex);
            }
        }
    }

    @Override
    protected AbstractModule createModule(ClassLoader classLoader, Resource resource, Dictionary<String, String> headers, Attachable context) {
        assertNoShutdown();

        AbstractModule module;
        if (resource != null && resource.getIdentity().equals(getSystemIdentity())) {
            module = new SystemModule(this, classLoader, resource);
        } else {
            module = new EmbeddedModule(this, classLoader, resource, headers);
        }
        return module;
    }

    @Override
    protected ModuleEntriesProvider getDefaultEntriesProvider(Module module, Attachable context) {
        return new ClassLoaderEntriesProvider(module);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A> A adapt(Class<A> type) {
        A result = super.adapt(type);
        if (result == null) {
            if (type.isAssignableFrom(RuntimeServicesManager.class)) {
                result = (A) serviceManager;
            } else if (type.isAssignableFrom(RuntimeStorageHandler.class)) {
                result = (A) storageHandler;
            }
        }
        return result;
    }

    @Override
    protected void uninstallModule(Module module) {
        super.uninstallModule(module);
    }

    @Override
    protected void doShutdown() {
        super.doShutdown();
        for (ServiceRegistration<?> sreg : systemServices) {
            sreg.unregister();
        }
    }

    private MBeanServer findOrCreateMBeanServer() {

        ArrayList<MBeanServer> serverArr = MBeanServerFactory.findMBeanServer(null);
        if (serverArr.size() == 1) {
            MBeanServer mbeanServer = serverArr.get(0);
            LOGGER.debug("Found MBeanServer: {}", mbeanServer.getDefaultDomain());
            return mbeanServer;
        } else {
            if (serverArr.size() > 1) {
                LOGGER.info("Multiple MBeanServer instances: {}", serverArr);
            }
            LOGGER.debug("Using the platform MBeanServer");
            return ManagementFactory.getPlatformMBeanServer();
        }
    }
}

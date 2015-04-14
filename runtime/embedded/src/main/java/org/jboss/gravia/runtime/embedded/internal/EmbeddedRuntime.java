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

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.ServiceLoader;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import org.jboss.gravia.Constants;
import org.jboss.gravia.resource.Attachable;
import org.jboss.gravia.resource.DefaultResourceBuilder;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.ModuleException;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.ServiceLocator;
import org.jboss.gravia.runtime.ServiceRegistration;
import org.jboss.gravia.runtime.spi.AbstractModule;
import org.jboss.gravia.runtime.spi.AbstractRuntime;
import org.jboss.gravia.runtime.spi.ClassLoaderEntriesProvider;
import org.jboss.gravia.runtime.spi.ModuleEntriesProvider;
import org.jboss.gravia.runtime.spi.PropertiesProvider;
import org.jboss.gravia.runtime.spi.RuntimeEventsManager;
import org.jboss.gravia.runtime.spi.RuntimePlugin;
import org.jboss.gravia.utils.IllegalArgumentAssertion;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
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
        ResourceIdentity sysid = getSystemIdentity();
        Resource resource = new DefaultResourceBuilder().addIdentityCapability(sysid).getResource();
        try {
            Dictionary<String, String> headers = new Hashtable<>();
            headers.put("Bundle-SymbolicName", sysid.getSymbolicName());
            headers.put("Bundle-Version", sysid.getVersion().toString());
            installModule(EmbeddedRuntime.class.getClassLoader(), resource, headers, context);
        } catch (ModuleException ex) {
            throw new IllegalStateException("Cannot install system module", ex);
        }
    }

    @Override
    public void init() {
        assertNoShutdown();

        // Register the Runtime
        ModuleContext syscontext = adapt(ModuleContext.class);
        systemServices.add(registerRuntimeService(syscontext));

        // Register the LogService
        systemServices.add(registerLogService(syscontext));

        // Register the MBeanServer service
        systemServices.add(registerMBeanServer(syscontext));

        // Install the plugin modules
        List<Module> pluginModules = installPluginModules();

        // Start the plugin modules
        startPluginModules(pluginModules);

        // Load initial configurations
        loadInitialConfigurations(syscontext);
    }

    private ServiceRegistration<?> registerRuntimeService(ModuleContext syscontext) {
        return syscontext.registerService(Runtime.class, this, null);
    }

    protected ServiceRegistration<?> registerLogService(ModuleContext syscontext) {
        return syscontext.registerService(LogService.class.getName(), new EmbeddedLogServiceFactory(), null);
    }

    protected ServiceRegistration<MBeanServer> registerMBeanServer(ModuleContext syscontext) {
        MBeanServer mbeanServer = findOrCreateMBeanServer();
        return syscontext.registerService(MBeanServer.class, mbeanServer, null);
    }

    protected List<Module> installPluginModules() {
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
        return pluginModules;
    }

    protected void startPluginModules(List<Module> pluginModules) {
        for (Module module : pluginModules) {
            try {
                module.start();
            } catch (ModuleException ex) {
                LOGGER.error("Cannot start plugin: " + module, ex);
            }
        }
    }

    protected void loadInitialConfigurations(ModuleContext syscontext) {
        ConfigurationAdmin configAdmin = ServiceLocator.getService(ConfigurationAdmin.class);
        if (configAdmin != null) {
            String configs = (String) getProperty(Constants.RUNTIME_CONFIGURATIONS_DIR);
            if (configs != null) {
                File configsDir = Paths.get(configs).toFile();
                if (configsDir.isDirectory()) {
                    initConfigurationAdmin(syscontext, configsDir.getAbsoluteFile());
                } else {
                    LOGGER.warn("Invalid configuration directory: {}", configsDir);
                }
            }
        }
    }

    @Override
    protected AbstractModule createModule(ClassLoader classLoader, Resource resource, Dictionary<String, String> headers, Attachable context) {
        assertNoShutdown();

        AbstractModule module;
        if (resource != null && resource.getIdentity().equals(getSystemIdentity())) {
            module = new SystemModule(this, classLoader, resource, headers);
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void initConfigurationAdmin(ModuleContext syscontext, File configsDir) {
        IllegalArgumentAssertion.assertTrue(configsDir.isDirectory(), "Invalid configuration directory: " + configsDir);

        ConfigurationAdmin configAdmin = ServiceLocator.getRequiredService(ConfigurationAdmin.class);

        LOGGER.info("Loading ConfigurationAdmin content from: {}", configsDir);
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".cfg");
            }
        };
        for (String name : configsDir.list(filter)) {
            boolean factoryConfig = false;
            String pid = name.substring(0, name.length() - 4);
            if (pid.contains("-")) {
                factoryConfig = true;
                pid = pid.substring(0, pid.indexOf("-"));
                LOGGER.info("Loading factory configuration: {}", pid);
            } else {
                LOGGER.info("Loading configuration: {}", pid);
            }
            try {
                FileInputStream fis = new FileInputStream(new File(configsDir, name));
                Properties props = new Properties();
                props.load(fis);
                fis.close();
                Configuration config = factoryConfig ? configAdmin.createFactoryConfiguration(pid, null) : configAdmin.getConfiguration(pid, null);
                config.update((Hashtable) props);
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }
}

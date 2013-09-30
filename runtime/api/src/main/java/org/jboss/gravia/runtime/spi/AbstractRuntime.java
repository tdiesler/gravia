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
package org.jboss.gravia.runtime.spi;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.jar.Manifest;

import org.jboss.gravia.resource.DefaultResourceBuilder;
import org.jboss.gravia.resource.ManifestResourceBuilder;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.Module.State;
import org.jboss.gravia.runtime.ModuleEvent;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.logging.Logger;
import org.jboss.osgi.metadata.OSGiManifestBuilder;
import org.jboss.osgi.metadata.OSGiMetaData;
import org.jboss.osgi.metadata.OSGiMetaDataBuilder;

/**
 * [TODO]
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
public abstract class AbstractRuntime implements Runtime {

    public static Logger LOGGER = Logger.getLogger(Runtime.class.getPackage().getName());

    private final RuntimeEventsHandler runtimeEvents;

    private final Map<Long, Module> modules = new ConcurrentHashMap<Long, Module>();

    public AbstractRuntime() {
        runtimeEvents = new RuntimeEventsHandler(createExecutorService("RuntimeEvents"));
    }

    protected abstract AbstractModule createModule(ClassLoader classLoader, Resource resource);

    @Override
    @SuppressWarnings("unchecked")
    public <A> A adapt(Class<A> type) {
        A result = null;
        if (type.isAssignableFrom(RuntimeEventsHandler.class)) {
            result = (A) getRuntimeEventsHandler();
        }
        return result;
    }

    public RuntimeEventsHandler getRuntimeEventsHandler() {
        return runtimeEvents;
    }

    @Override
    public Module getModule(long id) {
        return modules.get(id);
    }

    @Override
    public Set<Module> getModules() {
        HashSet<Module> snapshot = new HashSet<Module>(modules.values());
        return Collections.unmodifiableSet(snapshot);
    }

    @Override
    public Module installModule(ClassLoader classLoader, Manifest manifest) {
        Resource resource;
        if (OSGiManifestBuilder.isValidBundleManifest(manifest)) {
            OSGiMetaData metaData = OSGiMetaDataBuilder.load(manifest);
            DefaultResourceBuilder builder = new DefaultResourceBuilder();
            builder.addIdentityCapability(metaData.getBundleSymbolicName(), metaData.getBundleVersion().toString());
            resource = builder.getResource();
        } else {
            resource = new ManifestResourceBuilder().load(manifest).getResource();
        }
        Module module = installModuleInternal(classLoader, resource);
        module.putAttachment(Module.MANIFEST_KEY, manifest);
        return module;
    }

    @Override
    public Module installModule(ClassLoader classLoader, Resource resource) {
        return installModuleInternal(classLoader, resource);
    }

    public void uninstallModule(Module module) {
        modules.remove(module.getModuleId());
        LOGGER.infof("Uninstalled: %s", module);
    }

    private Module installModuleInternal(ClassLoader classLoader, Resource resource) {

        AbstractModule module = createModule(classLoader, resource);
        modules.put(module.getModuleId(), module);

        // #1 The module's state is set to {@code INSTALLED}.
        module.setState(State.INSTALLED);

        // #2 A module event of type {@link ModuleEvent#INSTALLED} is fired.
        runtimeEvents.fireModuleEvent(module, ModuleEvent.INSTALLED);

        // #3 The module's state is set to {@code RESOLVED}.
        module.setState(State.RESOLVED);

        // #4 A module event of type {@link ModuleEvent#RESOLVED} is fired.
        runtimeEvents.fireModuleEvent(module, ModuleEvent.RESOLVED);

        LOGGER.infof("Installed: %s", module);
        return module;
    }

    private ExecutorService createExecutorService(final String threadName) {
        ExecutorService service = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable run) {
                Thread thread = new Thread(run);
                thread.setName(threadName);
                return thread;
            }
        });
        return service;
    }
}

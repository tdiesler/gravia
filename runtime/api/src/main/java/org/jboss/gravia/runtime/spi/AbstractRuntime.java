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

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.Module.State;
import org.jboss.gravia.runtime.ModuleEvent;
import org.jboss.gravia.runtime.ModuleException;
import org.jboss.gravia.runtime.PropertiesProvider;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.logging.Logger;

/**
 * [TODO]
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
public abstract class AbstractRuntime implements Runtime {

    public static Logger LOGGER = Logger.getLogger(Runtime.class.getPackage().getName());

    private final Map<Long, Module> modules = new ConcurrentHashMap<Long, Module>();
    private final RuntimeEventsHandler runtimeEvents;
    private final PropertiesProvider properties;

    protected AbstractRuntime(PropertiesProvider propertiesProvider) {
        runtimeEvents = new RuntimeEventsHandler(createExecutorService("RuntimeEvents"));
        properties = propertiesProvider;
    }

    public abstract AbstractModule createModule(ClassLoader classLoader, Resource resource, Dictionary<String, String> headers);

    public abstract ModuleEntriesProvider getModuleEntriesProvider(Module module);

    @Override
    public final Object getProperty(String key) {
        return properties.getProperty(key);
    }

    @Override
    public final Object getProperty(String key, Object defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A> A adapt(Class<A> type) {
        A result = null;
        if (type.isAssignableFrom(RuntimeEventsHandler.class)) {
            result = (A) runtimeEvents;
        }
        return result;
    }

    @Override
    public final Module getModule(long id) {
        return modules.get(id);
    }

    @Override
    public final Module getModule(ResourceIdentity identity) {
        for (Module module : modules.values()) {
            if (module.getIdentity().equals(identity))
                return module;
        }
        return null;
    }

    @Override
    public final Module getModule(ClassLoader classLoader) {
        Set<Module> modules = getModules(classLoader);
        return modules.size() == 1 ? modules.iterator().next() : null;
    }

    @Override
    public final Set<Module> getModules() {
        return new HashSet<Module>(modules.values());
    }

    @Override
    public final Set<Module> getModules(ClassLoader classLoader) {
        Set<Module> result = getModules();
        Iterator<Module> iterator = result.iterator();
        while(iterator.hasNext()) {
            Module module = iterator.next();
            if (!module.adapt(ClassLoader.class).equals(classLoader)) {
                iterator.remove();
            }
        }
        return result;
    }

    @Override
    public final Module installModule(ClassLoader classLoader, Dictionary<String, String> headers) throws ModuleException {
        return installModule(classLoader, null, headers);
    }

    @Override
    public final Module installModule(ClassLoader classLoader, Resource resource, Dictionary<String, String> headers) throws ModuleException {

        AbstractModule module = createModule(classLoader, resource, headers);
        if (getModule(module.getIdentity()) != null) {
            throw new ModuleException("ModuleAlready installed: " + module);
        }
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

    protected void uninstallModule(Module module) {
        modules.remove(module.getModuleId());
        LOGGER.infof("Uninstalled: %s", module);
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

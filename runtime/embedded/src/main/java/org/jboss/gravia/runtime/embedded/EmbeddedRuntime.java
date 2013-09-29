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
package org.jboss.gravia.runtime.embedded;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.Module.State;
import org.jboss.gravia.runtime.Runtime;

/**
 * [TODO]
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
public final class EmbeddedRuntime implements Runtime {

    private final EmbeddedRuntimeEventsHandler runtimeEvents;
    private final EmbeddedRuntimeServicesHandler serviceManager;

    private final Map<Long, Module> modules = new ConcurrentHashMap<Long, Module>();
    private final Map<String, Object> properties;

    public EmbeddedRuntime(Map<String, Object> props) {
        runtimeEvents = new EmbeddedRuntimeEventsHandler(createExecutorService("RuntimeEvents"));
        serviceManager = new EmbeddedRuntimeServicesHandler(runtimeEvents);
        Map<String, Object> auxprops = new ConcurrentHashMap<String, Object>();
        if (props != null) {
            auxprops.putAll(props);
        }
        this.properties = Collections.unmodifiableMap(auxprops);
    }

    @Override
    public Object getProperty(String key) {
        return properties.get(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A> A adapt(Class<A> type) {
        A result = null;
        if (type.isAssignableFrom(EmbeddedRuntimeEventsHandler.class)) {
            result = (A) runtimeEvents;
        } else if (type.isAssignableFrom(EmbeddedRuntimeServicesHandler.class)) {
            result = (A) serviceManager;
        }
        return result;
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
    public Module installModule(ClassLoader classLoader, Map<String, Object> properties) {
        ModuleImpl module = new ModuleImpl(this, classLoader, properties);
        module.setState(State.RESOLVED);
        return module;
    }

    void uninstallModule(Module module) {
        modules.remove(module.getModuleId());
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

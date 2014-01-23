/*
 * #%L
 * Gravia :: Runtime :: API
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
package org.jboss.gravia.runtime.spi;

import static org.jboss.gravia.runtime.spi.RuntimeLogger.LOGGER;

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.gravia.resource.Attachable;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.resource.Version;
import org.jboss.gravia.resource.VersionRange;
import org.jboss.gravia.resource.spi.AttachableSupport;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.Module.State;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.ModuleEvent;
import org.jboss.gravia.runtime.ModuleException;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.utils.NotNullException;

/**
 * The abstract base implementation for a {@link Runtime}
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
public abstract class AbstractRuntime implements Runtime {

    private final ResourceIdentity systemIdentity = ResourceIdentity.create("gravia-system", Version.emptyVersion);
    private final Map<Long, Module> modules = new ConcurrentHashMap<Long, Module>();
    private final RuntimeEventsManager runtimeEvents;
    private final PropertiesProvider properties;

    protected AbstractRuntime(PropertiesProvider propertiesProvider) {
        NotNullException.assertValue(propertiesProvider, "propertiesProvider");
        runtimeEvents = new RuntimeEventsManager();
        properties = propertiesProvider;
    }

    protected abstract AbstractModule createModule(ClassLoader classLoader, Resource resource, Dictionary<String, String> headers, Attachable context);

    protected abstract ModuleEntriesProvider getDefaultEntriesProvider(Module module, Attachable context);

    @Override
    public final Object getProperty(String key) {
        return properties.getProperty(key);
    }

    @Override
    public final Object getProperty(String key, Object defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    @SuppressWarnings("unchecked")
    public <A> A adapt(Class<A> type) {
        A result = null;
        if (type.isAssignableFrom(RuntimeEventsManager.class)) {
            result = (A) runtimeEvents;
        } else if (type.isAssignableFrom(ModuleContext.class)) {
            result = (A) getModuleContext();
        }
        return result;
    }

    protected final ResourceIdentity getSystemIdentity() {
        return systemIdentity;
    }

    @Override
    public ModuleContext getModuleContext() {
        Module sysmodule = getModule(0);
        return sysmodule != null ? sysmodule.getModuleContext() : null;
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
        return modules.size() > 0 ? modules.iterator().next() : null;
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
    public Set<Module> getModules(String symbolicName, VersionRange range) {
        Set<Module> result = getModules();
        Iterator<Module> iterator = result.iterator();
        while(iterator.hasNext()) {
            ResourceIdentity modid = iterator.next().getIdentity();
            if (symbolicName != null && !symbolicName.equals(modid.getSymbolicName())) {
                iterator.remove();
            }
            if (range != null && !range.includes(modid.getVersion())) {
                iterator.remove();
            }
        }
        return result;
    }

    @Override
    public final Module installModule(ClassLoader classLoader, Dictionary<String, String> headers) throws ModuleException {
        return installModule(classLoader, null, headers, null);
    }

    @Override
    public final Module installModule(ClassLoader classLoader, Resource resource, Dictionary<String, String> headers) throws ModuleException {
        return installModule(classLoader, resource, headers, null);
    }

    @Override
    public final Module installModule(ClassLoader classLoader, Resource resource, Dictionary<String, String> headers, Attachable context) throws ModuleException {

        context = context != null ? context : new AttachableSupport();
        AbstractModule module = createModule(classLoader, resource, headers, context);

        // Attach the {@link ModuleEntriesProvider}
        ModuleEntriesProvider entriesProvider = context.getAttachment(AbstractModule.MODULE_ENTRIES_PROVIDER_KEY);
        entriesProvider = entriesProvider != null ? entriesProvider : getDefaultEntriesProvider(module, context);
        if (entriesProvider != null) {
            module.putAttachment(AbstractModule.MODULE_ENTRIES_PROVIDER_KEY, entriesProvider);
        }

        if (getModule(module.getIdentity()) != null)
            throw new ModuleException("Module already installed: " + module);

        modules.put(module.getModuleId(), module);

        // #1 The module's state is set to {@code INSTALLED}.
        module.setState(State.INSTALLED);

        // #2 A module event of type {@link ModuleEvent#INSTALLED} is fired.
        runtimeEvents.fireModuleEvent(module, ModuleEvent.INSTALLED);

        LOGGER.info("Installed: {}", module);
        return module;
    }

    protected void uninstallModule(Module module) {
        modules.remove(module.getModuleId());
        LOGGER.info("Uninstalled: {}", module);
    }
}

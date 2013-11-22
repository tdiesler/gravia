/*
 * #%L
 * JBossOSGi Runtime
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
package org.jboss.gravia.runtime;

import java.util.Dictionary;
import java.util.Set;

import org.jboss.gravia.resource.Attachable;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.resource.VersionRange;

/**
 * The Gravia runtime.
 *
 * <p>
 * It is used to install and maintain the set of installed {@link Module}s.
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
public interface Runtime {

    /**
     * Initialize this runtime instance. After calling this method, this Runtime
     * must:
     * <ul>
     * <li>Have event handling enabled.</li>
     * <li>Have reified Module objects for all installed modules.</li>
     * <li>Have registered any framework services.</li>
     * </ul>
     */
    void init();

    /**
     * Returns the value of the specified property. If the key is not found in
     * the Runtime properties, the system properties are then searched. The
     * method returns {@code null} if the property is not found.
     *
     * @param key The name of the requested property.
     * @return The value of the requested property, or {@code null} if the
     *         property is undefined.
     */
    Object getProperty(String key);

    /**
     * Returns the value of the specified property. If the key is not found in
     * the Runtime properties, the system properties are then searched. The
     * method returns provided default value if the property is not found.
     *
     * @param key The name of the requested property.
     * @return The value of the requested property, or the provided default value if the
     *         property is undefined.
     */
    Object getProperty(String key, Object defaultValue);

    /**
     * Returns the module with the specified identifier.
     *
     * @param id The identifier of the module to retrieve.
     * @return A {@code Module} object or {@code null} if the identifier does
     *         not match any installed module.
     */
    Module getModule(long id);

    /**
     * Returns the module with the specified resource identity.
     *
     * @param identity The identifier of the module to retrieve.
     * @return A {@code Module} object or {@code null} if the resource identity does
     *         not match any installed module.
     */
    Module getModule(ResourceIdentity identity);

    /**
     * Returns a module that is associated with the specified class loader.
     *
     * <p>
     * If multiple modules are associated with the same class loader it returns
     * the first in the natural module order (i.e. the one with the lowest module identifier)
     *
     * @param classLoader The class loader of the module to retrieve.
     * @return A {@code Module} object or {@code null} if the class loader does
     *         not match any installed module.
     */
    Module getModule(ClassLoader classLoader);

    /**
     * Returns the set of all installed modules.
     * <p>
     * This method returns a list of all modules installed in the Runtime
     * at the time of the call to this method. However, since the
     * Runtime is a very dynamic environment, modules can be installed or
     * uninstalled at anytime.
     *
     * @return The set of {@code Module}s.
     */
    Set<Module> getModules();

    /**
     * Returns the set of installed modules associated with the given class loader.
     *
     * @return The set of {@code Module}s.
     */
    Set<Module> getModules(ClassLoader classLoader);

    /**
     * Returns the set of installed modules with a given symbolic name or version.
     * <p>
     * Both parameters are optional. If a parameter is null it matches all.
     *
     * @return The set of {@code Module}s that match.
     */
    Set<Module> getModules(String symbolicName, VersionRange range);

    /**
     * Installs a module with the given ClassLoader and headers dictionary.
     * <p>
     * The module's {@link ResourceIdentity} and possible other
     * capabilities/requirements are generated from the headers.
     * <p>
     * @see Runtime#installModule(ClassLoader, Resource, Dictionary)
     */
    Module installModule(ClassLoader classLoader, Dictionary<String, String> headers) throws ModuleException;

    /**
     * Installs a module with the given ClassLoader.
     *
     * The Resource as well as the Dictionary parameter are optional, but
     * one of them must be given to determine the modules's identity.
     * <p>
     * @see Runtime#installModule(ClassLoader, Resource, Dictionary, Attachable)
     */
    Module installModule(ClassLoader classLoader, Resource resource, Dictionary<String, String> headers) throws ModuleException;

    /**
     * Installs a module with the given ClassLoader.
     *
     * The Resource as well as the Dictionary parameter are optional, but
     * one of them must be given to determine the modules's identity.
     * <p>
     * An explicit {@link Resource} parameter takes priority.
     * <p>
     * An optional application context can be given
     * <p>
     * The following steps are required to install a module:
     * <ol>
     * <li>The module's state is set to {@code INSTALLED}.
     * <li>A module event of type {@link ModuleEvent#INSTALLED} is fired.
     * <li>The module's state is set to {@code RESOLVED}.
     * <li>A module event of type {@link ModuleEvent#RESOLVED} is fired.
     * <li>The {@code Module} object for the newly or previously installed module is returned.
     * </ol>
     */
    Module installModule(ClassLoader classLoader, Resource resource, Dictionary<String, String> headers, Attachable context) throws ModuleException;
}

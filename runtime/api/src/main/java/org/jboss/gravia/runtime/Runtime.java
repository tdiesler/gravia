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

import java.util.Set;
import java.util.jar.Manifest;

import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceIdentity;

/**
 * [TODO]
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
public interface Runtime extends PropertiesProvider {

    <A> A adapt(Class<A> type);

    Module getModule(long id);

    Set<Module> getModules();

    /**
     * Installs a module with the given ClassLoader and Manifest.
     * <p>
     * The module's {@link ResourceIdentity} and possible other
     * capabilities/requirements are generated from the manifest.
     * <p>
     * @see Runtime#installModule(ClassLoader, Resource, Manifest)
     */
    Module installModule(ClassLoader classloader, Manifest manifest);

    /**
     * Installs a module with the given ClassLoader.
     *
     * The Resource as well as the Manifest parameter are optional, but
     * one of them must be given to determine the modules's identity.
     * <p>
     * An explicit {@link Resource} parameter takes priority.
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
    Module installModule(ClassLoader classloader, Resource resource, Manifest manifest);
}

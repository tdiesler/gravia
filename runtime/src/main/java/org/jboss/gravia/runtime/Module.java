/*
 * #%L
 * JBossOSGi Framework
 * %%
 * Copyright (C) 2010 - 2012 JBoss by Red Hat
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

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

/**
 * Integration point for {@link Bundle} management.
 *
 * @author thomas.diesler@jboss.com
 * @since 24-Mar-2011
 */
public interface Module {

    enum State {
        INSTALLED,
        RESOLVED,
        STARTING,
        ACTIVE,
        STOPPING,
        UNINSTALLED
    }

    enum Type {
        /** OSGi Bundle */
        BUNDLE,
        /** JBoss Module */
        MODULE,
        /** Other */
        OTHER
    }

    Runtime getRuntime();

    long getModuleId();

    State getState();

    <A> A adapt(Class<A> type);

    ModuleContext getModuleContext();

    void start() throws ModuleException;

    void stop() throws ModuleException;

    void uninstall() throws ModuleException;

    ServiceRegistration<?> registerService(String clazz, Object service, Dictionary<String, ?> properties);

    Class<?> loadClass(String className) throws ClassNotFoundException;
}
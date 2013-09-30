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
package org.jboss.gravia.runtime.osgi;

import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.ModuleException;
import org.jboss.gravia.runtime.spi.AbstractModule;
import org.jboss.gravia.runtime.spi.AbstractRuntime;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleReference;

/**
 * [TODO]
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
public final class OSGiModule extends AbstractModule {

    private final Bundle bundle;

    OSGiModule(AbstractRuntime runtime, ClassLoader classLoader, Resource resource) {
        super(runtime, classLoader, resource);
        bundle = ((BundleReference) classLoader).getBundle();
    }

    @Override
    public long getModuleId() {
        return bundle.getBundleId();
    }

    @Override
    public ModuleContext getModuleContext() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void start() throws ModuleException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void stop() throws ModuleException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void uninstall() {
        throw new UnsupportedOperationException();
    }
}

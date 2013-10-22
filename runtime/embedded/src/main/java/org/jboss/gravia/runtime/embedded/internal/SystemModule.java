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
package org.jboss.gravia.runtime.embedded.internal;

import java.io.File;

import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.ModuleException;
import org.jboss.gravia.runtime.spi.AbstractModule;
import org.jboss.gravia.runtime.spi.AbstractRuntime;

/**
 * The system module
 *
 * @author thomas.diesler@jboss.com
 * @since 22-Oct-2013
 */
public final class SystemModule extends AbstractModule {

    protected SystemModule(AbstractRuntime runtime, ClassLoader classLoader, Resource resource) {
        super(runtime, classLoader, resource, null);
    }

    @Override
    public long getModuleId() {
        return 0;
    }

    @Override
    public State getState() {
        return State.ACTIVE;
    }

    @Override
    public ModuleContext getModuleContext() {
        return new EmbeddedModuleContext(this);
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

    @Override
    public File getDataFile(String filename) {
        AbstractRuntime runtime = adapt(AbstractRuntime.class);
        RuntimeStorageHandler storageHandler = runtime.adapt(RuntimeStorageHandler.class);
        return storageHandler.getDataFile(this, filename);
    }

    @Override
    protected void setState(State newState) {
    }
}

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

import java.io.File;
import java.util.Dictionary;

import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.ModuleException;
import org.jboss.gravia.runtime.embedded.spi.BundleAdaptor;
import org.jboss.gravia.runtime.spi.AbstractModule;
import org.jboss.gravia.runtime.spi.AbstractRuntime;
import org.osgi.framework.Bundle;

/**
 * The system module
 *
 * @author thomas.diesler@jboss.com
 * @since 22-Oct-2013
 */
final class SystemModule extends AbstractModule {

    SystemModule(AbstractRuntime runtime, ClassLoader classLoader, Resource resource, Dictionary<String, String> headers) {
        super(runtime, classLoader, resource, headers);
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
    protected Bundle getBundleAdaptor(Module module) {
        return new BundleAdaptor(this);
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

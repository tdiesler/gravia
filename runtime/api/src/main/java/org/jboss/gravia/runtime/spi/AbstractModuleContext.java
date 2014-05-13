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

import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.gravia.runtime.Filter;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.utils.IllegalArgumentAssertion;

/**
 * The abstract base implementation for a {@link ModuleContext}
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
public abstract class AbstractModuleContext implements ModuleContext {

    private final AtomicBoolean destroyed = new AtomicBoolean();
    private final Module module;

    protected AbstractModuleContext(Module module) {
        IllegalArgumentAssertion.assertNotNull(module, "module");
        this.module = module;
    }

    protected void destroy() {
        destroyed.set(true);
    }

    protected boolean isDestroyed() {
        return destroyed.get();
    }

    @Override
    public Module getModule() {
        return module;
    }

    @Override
    public Filter createFilter(String filter) {
        return FilterFactory.createFilter(filter);
    }

    protected void assertNotDestroyed() {
        if (destroyed.get())
            throw new IllegalStateException("Invalid ModuleContext for: " + module);
    }

    public String toString() {
        return "ModuleContext[" + module.getIdentity() + "]";
    }
}

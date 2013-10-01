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

import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.gravia.runtime.Filter;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;

/**
 * [TODO]
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
public abstract class AbstractModuleContext implements ModuleContext {

    private final AtomicBoolean destroyed = new AtomicBoolean();
    private final Module module;

    protected AbstractModuleContext(Module module) {
        this.module = module;
    }

    protected void destroy() {
        destroyed.set(true);
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
}

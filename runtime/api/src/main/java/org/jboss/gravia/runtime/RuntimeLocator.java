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

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.gravia.runtime.spi.PropertiesProvider;
import org.jboss.gravia.runtime.spi.RuntimeFactory;
import org.jboss.gravia.runtime.util.DefaultPropertiesProvider;

/**
 * Locates the single Runtime instance
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 *
 * @ThreadSafe
 */
public final class RuntimeLocator {

    private static AtomicReference<Runtime> runtimeReference = new AtomicReference<Runtime>();

    // Hide ctor
    private RuntimeLocator() {
    }

    /**
     * Returns the global runtime instance or {@code null} if the runtime has not been initialized.
     */
    public static Runtime getRuntime() {
        return runtimeReference.get();
    }

    /**
     * Sets the global runtime instance.
     * This method is not intended to be called by client code.
     * The runtime instance is provided by the target environment.
     */
    public static void setRuntime(Runtime runtime) {
        runtimeReference.set(runtime);
    }

    @Deprecated
    public static Runtime locateRuntime(PropertiesProvider props) {
        Runtime runtime = runtimeReference.get();
        if (runtime == null) {
            ServiceLoader<RuntimeFactory> loader = ServiceLoader.load(RuntimeFactory.class, RuntimeLocator.class.getClassLoader());
            Iterator<RuntimeFactory> iterator = loader.iterator();
            if (iterator.hasNext()) {
                RuntimeFactory factory = iterator.next();
                DefaultPropertiesProvider propertiesProvider = new DefaultPropertiesProvider();
                runtime = factory.createRuntime(props != null ? props : propertiesProvider);
                runtime.init();
                runtimeReference.set(runtime);
            }
        }
        return runtime;
    }
}

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
import org.jboss.gravia.utils.NotNullException;

/**
 * Locates the a Runtime instance
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
     * Returns the default runtime instance or {@code null} if it has not been created.
     */
    public static Runtime getRuntime() {
        return runtimeReference.get();
    }

    /**
     * Returns the default runtime instance.
     * @throws IllegalStateException If the Runtime has not been created.
     */
    public static Runtime getRequiredRuntime() {
        Runtime runtime = runtimeReference.get();
        if (runtime == null)
            throw new IllegalStateException("Runtime not available");
        return runtime;
    }

    /**
     * Create the default runtime instance from the given factory and properties.
     * @throws IllegalStateException If a runtime has already been created
     */
    public static Runtime createRuntime(RuntimeFactory factory, PropertiesProvider props) {
        NotNullException.assertValue(factory, "factory");
        NotNullException.assertValue(props, "props");
        synchronized (runtimeReference) {

            // Check that the runtime has not already been created
            Runtime runtime = runtimeReference.get();
            if (runtime != null)
                throw new IllegalStateException("Runtime already created: " + runtime);

            // Create the {@link Runtime}
            runtime = factory.createRuntime(props);
            runtimeReference.set(runtime);

            return runtime;
        }
    }

    /**
     * Create the default runtime instance from the given properties.
     * <p>
     * The {@link RuntimeFactory} is determined
     * <ol>
     * <li> Fully qualified class name as property under the key 'org.jboss.gravia.runtime.spi.RuntimeFactory'
     * <li> From a {@link ServiceLoader} of type {@link RuntimeFactory}
     * </ol>
     */
    public static Runtime createRuntime(PropertiesProvider props) {
        NotNullException.assertValue(props, "props");

        RuntimeFactory factory = null;
        String className = (String) props.getProperty(RuntimeFactory.class.getName());
        if (className != null) {
            try {
                factory = (RuntimeFactory) Class.forName(className).newInstance();
            } catch (Exception ex) {
                throw new IllegalStateException("Cannot load runtime factory: " + className, ex);
            }
        } else {
            ServiceLoader<RuntimeFactory> loader = ServiceLoader.load(RuntimeFactory.class, RuntimeLocator.class.getClassLoader());
            Iterator<RuntimeFactory> iterator = loader.iterator();
            if (iterator.hasNext()) {
                factory = iterator.next();
            }
        }

        return createRuntime(factory, props);
    }

    /**
     * Release the default runtime instance.
     */
    public static void releaseRuntime() {
        runtimeReference.set(null);
    }
}

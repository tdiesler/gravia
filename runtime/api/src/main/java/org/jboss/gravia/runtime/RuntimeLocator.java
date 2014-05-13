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
package org.jboss.gravia.runtime;

import static org.jboss.gravia.runtime.spi.RuntimeLogger.LOGGER;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.gravia.runtime.spi.PropertiesProvider;
import org.jboss.gravia.runtime.spi.RuntimeFactory;
import org.jboss.gravia.utils.ArgumentAssertion;

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
        if (runtime == null) {
            throw new IllegalStateException("Runtime not available from: " + RuntimeLocator.class.getClassLoader());
        }
        return runtime;
    }

    /**
     * Create the default runtime instance from the given factory and properties.
     * @throws IllegalStateException If a runtime has already been created
     */
    public static Runtime createRuntime(RuntimeFactory factory, PropertiesProvider props) {
        ArgumentAssertion.assertNotNull(factory, "factory");
        ArgumentAssertion.assertNotNull(props, "props");
        synchronized (runtimeReference) {

            // Check that the runtime has not already been created
            Runtime runtime = runtimeReference.get();
            if (runtime != null)
                throw new IllegalStateException("Runtime already created: " + runtime);

            // Create the {@link Runtime}
            runtime = factory.createRuntime(props);
            runtimeReference.set(runtime);

            LOGGER.info("Runtime created: {}", runtime);
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
        ArgumentAssertion.assertNotNull(props, "props");

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
        Runtime runtime = runtimeReference.getAndSet(null);
        if (runtime != null) {
            LOGGER.info("Runtime released: {}", runtime);
        }
    }
}
